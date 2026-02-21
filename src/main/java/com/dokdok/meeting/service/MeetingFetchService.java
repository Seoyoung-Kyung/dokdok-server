package com.dokdok.meeting.service;

import com.dokdok.gathering.service.GatheringValidator;
import com.dokdok.meeting.dto.MeetingDetailResponse;
import com.dokdok.meeting.entity.Meeting;
import com.dokdok.meeting.entity.MeetingMember;
import com.dokdok.meeting.repository.MeetingMemberRepository;
import com.dokdok.storage.service.StorageService;
import com.dokdok.topic.entity.TopicStatus;
import com.dokdok.topic.repository.TopicRepository;
import com.dokdok.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class MeetingFetchService {

    private final MeetingValidator meetingValidator;
    private final GatheringValidator gatheringValidator;
    private final MeetingMemberRepository meetingMemberRepository;
    private final TopicRepository topicRepository;
    private final StorageService storageService;

    // @Async self-invocation: 같은 클래스 내 직접 호출은 Spring 프록시를 우회하므로
    // @Lazy 자기 주입으로 프록시를 통해 호출한다.
    @Autowired
    @Lazy
    private MeetingFetchService self;

    /**
     * 비동기 약속 상세 조회 — MeetingService.findMeeting()과 동일 흐름,
     * presigned URL 빌드만 병렬 처리
     */
    @Transactional(readOnly = true)
    public MeetingDetailResponse findMeetingAsync(Long meetingId, Long userId) {
        Meeting meeting = meetingValidator.findMeetingOrThrow(meetingId);
        gatheringValidator.validateMembership(meeting.getGathering().getId(), userId);

        List<MeetingMember> meetingMembers =
                meetingMemberRepository.findAllByMeetingId(meetingId);
        Map<Long, String> profileImageUrlMap = buildProfileImageUrlMapAsync(meetingMembers);

        LocalDateTime confirmedTopicDate = topicRepository
                .findConfirmedTopicDateByMeetingId(meetingId, TopicStatus.CONFIRMED);
        boolean confirmedTopic = confirmedTopicDate != null;

        return MeetingDetailResponse.from(
                meeting,
                meetingMembers,
                userId,
                confirmedTopic,
                confirmedTopicDate,
                profileImageUrlMap
        );
    }
    /**
     * 멤버 전체 presigned URL 병렬 생성
     * self.fetchPresignedUrlAsync()로 호출해 Spring 프록시를 통과시킨다.
     */
    public Map<Long, String> buildProfileImageUrlMapAsync(List<MeetingMember> members) {
        Map<Long, CompletableFuture<String>> futures = new LinkedHashMap<>();
        members.forEach(member -> {
            User user = member.getUser();
            if (user == null || user.getId() == null) {
                return;
            }
            futures.put(user.getId(),
                    self.fetchPresignedUrlAsync(user.getProfileImageUrl()));
        });

        CompletableFuture.allOf(futures.values().toArray(new CompletableFuture[0])).join();

        Map<Long, String> result = new HashMap<>();
        futures.forEach((uid, f) -> result.put(uid, f.join()));
        return result;
    }

    /**
     * 단건 MinIO presigned URL 조회 — meetingExecutor 스레드에서 실행
     */
    @Async("meetingExecutor")
    public CompletableFuture<String> fetchPresignedUrlAsync(String profileImageUrl) {
        return CompletableFuture.completedFuture(
                storageService.getPresignedProfileImage(profileImageUrl)
        );
    }
}
