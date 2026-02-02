package com.dokdok.stt.service;

import com.dokdok.ai.client.AiSttClient;
import com.dokdok.ai.dto.SttRequest;
import com.dokdok.ai.dto.SttResponse;
import com.dokdok.global.exception.GlobalErrorCode;
import com.dokdok.global.exception.GlobalException;
import com.dokdok.global.util.SecurityUtil;
import com.dokdok.meeting.entity.Meeting;
import com.dokdok.meeting.service.MeetingValidator;
import com.dokdok.stt.dto.SttJobResponse;
import com.dokdok.stt.entity.SttJob;
import com.dokdok.stt.entity.SttJobStatus;
import com.dokdok.stt.entity.SttSummary;
import com.dokdok.stt.repository.SttJobRepository;
import com.dokdok.stt.repository.SttSummaryRepository;
import com.dokdok.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SttJobService {

    private static final long MAX_FILE_SIZE = 50L * 1024L * 1024L;
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "mp3", "aac", "ac3", "ogg", "flac", "wav", "m4a"
    );

    private final MeetingValidator meetingValidator;
    private final SttJobRepository sttJobRepository;
    private final SttSummaryRepository sttSummaryRepository;
    private final AiSttClient aiSttClient;

    @Value("${stt.temp-dir:}")
    private String tempDirProperty;

    @Transactional
    public SttJobResponse createJob(Long meetingId, MultipartFile file) {
        Long userId = SecurityUtil.getCurrentUserId();
        meetingValidator.validateMeeting(meetingId);
        meetingValidator.validateMeetingMember(meetingId, userId);

        validateFile(file);

        Meeting meeting = meetingValidator.findMeetingOrThrow(meetingId);
        User user = SecurityUtil.getCurrentUserEntity();

        Path tempFilePath = saveToTemp(file);
        SttJob job = SttJob.builder()
                .meeting(meeting)
                .user(user)
                .originalFilename(file.getOriginalFilename())
                .contentType(file.getContentType())
                .fileSize(file.getSize())
                .tempFilePath(tempFilePath.toString())
                .status(SttJobStatus.PROCESSING)
                .build();
        sttJobRepository.save(job);

        SttSummary summary = null;
        try {
            SttResponse response = aiSttClient.requestStt(
                    new SttRequest(job.getId(), tempFilePath.toString(), "ko-KR")
            );
            if (response == null) {
                job.markFailed("STT response is empty");
            } else if ("FAILED".equalsIgnoreCase(response.status())) {
                job.markFailed(response.errorMessage() == null ? "STT failed" : response.errorMessage());
            } else if (isEmptyResponse(response)) {
                job.markFailed("STT response is empty");
            } else {
                job.markDone(response.text());
                summary = saveSummary(job, response);
            }
        } catch (WebClientResponseException e) {
            job.markFailed("AI STT error: " + e.getStatusCode());
            log.error("AI STT request failed: {}", e.getMessage(), e);
        } catch (Exception e) {
            job.markFailed("AI STT error");
            log.error("AI STT request failed", e);
        } finally {
            deleteTempFile(tempFilePath);
        }

        return SttJobResponse.from(job, summary);
    }

    @Transactional(readOnly = true)
    public SttJobResponse getJob(Long meetingId, Long jobId) {
        Long userId = SecurityUtil.getCurrentUserId();
        meetingValidator.validateMeeting(meetingId);
        meetingValidator.validateMeetingMember(meetingId, userId);

        SttJob job = sttJobRepository.findByIdAndMeetingId(jobId, meetingId)
                .orElseThrow(() -> new GlobalException(GlobalErrorCode.INVALID_INPUT_VALUE));

        SttSummary summary = sttSummaryRepository.findBySttJobId(jobId).orElse(null);
        return SttJobResponse.from(job, summary);
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new GlobalException(GlobalErrorCode.INVALID_INPUT_VALUE);
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new GlobalException(GlobalErrorCode.FILE_SIZE_EXCEEDED);
        }
        String originalFilename = file.getOriginalFilename();
        String extension = extractExtension(originalFilename);
        if (extension == null || !ALLOWED_EXTENSIONS.contains(extension)) {
            throw new GlobalException(GlobalErrorCode.INVALID_FILE_TYPE);
        }
    }

    private String extractExtension(String filename) {
        if (filename == null) {
            return null;
        }
        int lastDot = filename.lastIndexOf('.');
        if (lastDot < 0 || lastDot == filename.length() - 1) {
            return null;
        }
        return filename.substring(lastDot + 1).toLowerCase();
    }

    private Path saveToTemp(MultipartFile file) {
        String tempRoot = tempDirProperty == null || tempDirProperty.isBlank()
                ? System.getProperty("java.io.tmpdir")
                : tempDirProperty;
        Path tempDir = Paths.get(tempRoot, "dokdok-stt");
        try {
            Files.createDirectories(tempDir);
            String safeName = UUID.randomUUID() + "_" + sanitizeFilename(file.getOriginalFilename());
            Path tempFilePath = tempDir.resolve(safeName);
            file.transferTo(tempFilePath.toFile());
            return tempFilePath;
        } catch (IOException e) {
            throw new GlobalException(GlobalErrorCode.FILE_UPLOAD_FAILED, e);
        }
    }

    private String sanitizeFilename(String filename) {
        if (filename == null) {
            return "audio";
        }
        return filename.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    private void deleteTempFile(Path path) {
        if (path == null) {
            return;
        }
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            log.warn("Failed to delete temp STT file: {}", path, e);
        }
    }

    private SttSummary saveSummary(SttJob job, SttResponse response) {
        List<String> highlights = response.mainPoints() != null ? response.mainPoints() : response.highlights();
        if (response.summary() == null
                && (highlights == null || highlights.isEmpty())
                && (response.keywords() == null || response.keywords().isEmpty())
                && response.text() == null) {
            return null;
        }
        SttSummary summary = SttSummary.builder()
                .sttJob(job)
                .summary(response.summary())
                .highlights(highlights)
                .keywords(response.keywords())
                .build();
        return sttSummaryRepository.save(summary);
    }

    private boolean isEmptyResponse(SttResponse response) {
        boolean noText = response.text() == null || response.text().isBlank();
        boolean noSummary = response.summary() == null || response.summary().isBlank();
        boolean noHighlights = (response.mainPoints() == null || response.mainPoints().isEmpty())
                && (response.highlights() == null || response.highlights().isEmpty());
        boolean noKeywords = response.keywords() == null || response.keywords().isEmpty();
        return noText && noSummary && noHighlights && noKeywords;
    }
}
