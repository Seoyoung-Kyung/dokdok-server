package com.dokdok.book.service;

import com.dokdok.book.dto.request.PreOpinionTimeType;
import com.dokdok.book.dto.response.*;
import com.dokdok.book.entity.PersonalBook;
import com.dokdok.book.repository.ReadingTimelineRepository;
import com.dokdok.book.repository.dto.ReadingTimelineIndexRow;
import com.dokdok.global.response.CursorResponse;
import com.dokdok.global.util.SecurityUtil;
import com.dokdok.retrospective.dto.response.RetrospectiveRecordResponse;
import com.dokdok.retrospective.dto.response.RetrospectiveSummaryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReadingTimelineService {

    private static final int DEFAULT_PAGE_SIZE = 10;

    private final ReadingTimelineRepository readingTimelineRepository;
    private final BookValidator bookValidator;
    private final ReadingTimelineFetchService readingTimelineFetchService;

    public CursorResponse<ReadingTimelineItem, ReadingTimelineCursor> getTimeline(
            Long personalBookId,
            LocalDateTime cursorEventAt,
            ReadingTimelineType cursorType,
            Long cursorSourceId,
            Integer size,
            PreOpinionTimeType preOpinionTime
    ) {
        Long userId = SecurityUtil.getCurrentUserId();
        PersonalBook personalBook = bookValidator.validatePersonalBook(userId, personalBookId);

        List<ReadingTimelineIndexRow> pageRows = fetchIndexRows(
                personalBook, userId, personalBookId, cursorEventAt, cursorType, cursorSourceId, size, preOpinionTime
        );
        if (pageRows == null) {
            return CursorResponse.of(List.of(), resolvePageSize(size), false, null);
        }

        int pageSize = resolvePageSize(size);
        boolean hasNext = pageRows.size() == pageSize + 1;
        List<ReadingTimelineIndexRow> rows = hasNext ? pageRows.subList(0, pageSize) : pageRows;

        List<Long> readingRecordIds = extractIds(rows, ReadingTimelineType.READING_RECORD);
        List<Long> retrospectiveIds = extractIds(rows, ReadingTimelineType.PERSONAL_RETROSPECTIVE);
        List<Long> meetingIds = extractIds(rows, ReadingTimelineType.PRE_OPINION);
        List<Long> meetingSummaryIds = extractIds(rows, ReadingTimelineType.MEETING_RETROSPECTIVE);

        CompletableFuture<Map<Long, PersonalReadingRecordListResponse>> f1 =
                readingTimelineFetchService.fetchReadingRecordsAsync(readingRecordIds, personalBookId, userId);
        CompletableFuture<Map<Long, RetrospectiveRecordResponse>> f2 =
                readingTimelineFetchService.fetchRetrospectivesAsync(retrospectiveIds, userId);
        CompletableFuture<Map<Long, ReadingTimelinePreOpinionResponse>> f3 =
                readingTimelineFetchService.fetchPreOpinionsAsync(meetingIds, userId);
        CompletableFuture<Map<Long, RetrospectiveSummaryResponse>> f4 =
                readingTimelineFetchService.fetchMeetingRetrospectiveSummariesAsync(meetingSummaryIds);

        CompletableFuture.allOf(f1, f2, f3, f4).join();

        return buildResponse(rows, pageSize, hasNext, f1.join(), f2.join(), f3.join(), f4.join());
    }

    public CursorResponse<ReadingTimelineItem, ReadingTimelineCursor> getTimelineSync(
            Long personalBookId,
            LocalDateTime cursorEventAt,
            ReadingTimelineType cursorType,
            Long cursorSourceId,
            Integer size,
            PreOpinionTimeType preOpinionTime
    ) {
        Long userId = SecurityUtil.getCurrentUserId();
        PersonalBook personalBook = bookValidator.validatePersonalBook(userId, personalBookId);

        List<ReadingTimelineIndexRow> pageRows = fetchIndexRows(
                personalBook, userId, personalBookId, cursorEventAt, cursorType, cursorSourceId, size, preOpinionTime
        );
        if (pageRows == null) {
            return CursorResponse.of(List.of(), resolvePageSize(size), false, null);
        }

        int pageSize = resolvePageSize(size);
        boolean hasNext = pageRows.size() == pageSize + 1;
        List<ReadingTimelineIndexRow> rows = hasNext ? pageRows.subList(0, pageSize) : pageRows;

        List<Long> readingRecordIds = extractIds(rows, ReadingTimelineType.READING_RECORD);
        List<Long> retrospectiveIds = extractIds(rows, ReadingTimelineType.PERSONAL_RETROSPECTIVE);
        List<Long> meetingIds = extractIds(rows, ReadingTimelineType.PRE_OPINION);
        List<Long> meetingSummaryIds = extractIds(rows, ReadingTimelineType.MEETING_RETROSPECTIVE);

        Map<Long, PersonalReadingRecordListResponse> readingRecordMap =
                readingTimelineFetchService.fetchReadingRecords(readingRecordIds, personalBookId, userId);
        Map<Long, RetrospectiveRecordResponse> retrospectiveMap =
                readingTimelineFetchService.fetchRetrospectives(retrospectiveIds, userId);
        Map<Long, ReadingTimelinePreOpinionResponse> preOpinionMap =
                readingTimelineFetchService.fetchPreOpinions(meetingIds, userId);
        Map<Long, RetrospectiveSummaryResponse> meetingRetrospectiveMap =
                readingTimelineFetchService.fetchMeetingRetrospectiveSummaries(meetingSummaryIds);

        return buildResponse(rows, pageSize, hasNext,
                readingRecordMap, retrospectiveMap, preOpinionMap, meetingRetrospectiveMap);
    }

    private List<ReadingTimelineIndexRow> fetchIndexRows(
            PersonalBook personalBook,
            Long userId,
            Long personalBookId,
            LocalDateTime cursorEventAt,
            ReadingTimelineType cursorType,
            Long cursorSourceId,
            Integer size,
            PreOpinionTimeType preOpinionTime
    ) {
        Long bookId = personalBook.getBook().getId();
        Long gatheringId = personalBook.getGathering() != null
                ? personalBook.getGathering().getId()
                : null;

        int pageSize = resolvePageSize(size);

        boolean hasCursor = cursorEventAt != null && cursorType != null && cursorSourceId != null;
        LocalDateTime cursorEventAtValue = hasCursor ? cursorEventAt : null;
        Integer cursorTypeOrder = hasCursor ? cursorType.getOrder() : null;
        Long cursorSourceIdValue = hasCursor ? cursorSourceId : null;

        List<ReadingTimelineIndexRow> indexRows = readingTimelineRepository.findTimeline(
                personalBookId,
                userId,
                bookId,
                gatheringId,
                (preOpinionTime != null ? preOpinionTime.name() : PreOpinionTimeType.ANSWER_CREATED.name()),
                cursorEventAtValue,
                cursorTypeOrder,
                cursorSourceIdValue,
                pageSize + 1
        );

        if (indexRows.isEmpty()) {
            return null;
        }
        return indexRows;
    }

    private List<Long> extractIds(List<ReadingTimelineIndexRow> rows, ReadingTimelineType type) {
        return rows.stream()
                .filter(row -> type.name().equals(row.type()))
                .map(ReadingTimelineIndexRow::sourceId)
                .toList();
    }

    private CursorResponse<ReadingTimelineItem, ReadingTimelineCursor> buildResponse(
            List<ReadingTimelineIndexRow> rows,
            int pageSize,
            boolean hasNext,
            Map<Long, PersonalReadingRecordListResponse> readingRecordMap,
            Map<Long, RetrospectiveRecordResponse> retrospectiveMap,
            Map<Long, ReadingTimelinePreOpinionResponse> preOpinionMap,
            Map<Long, RetrospectiveSummaryResponse> meetingRetrospectiveMap
    ) {
        List<ReadingTimelineItem> items = rows.stream()
                .map(row -> {
                    ReadingTimelineType type = ReadingTimelineType.from(row.type());
                    return switch (type) {
                        case READING_RECORD -> ReadingTimelineItem.readingRecord(
                                row.eventAt(),
                                row.sourceId(),
                                readingRecordMap.get(row.sourceId())
                        );
                        case PERSONAL_RETROSPECTIVE -> ReadingTimelineItem.retrospective(
                                row.eventAt(),
                                row.sourceId(),
                                retrospectiveMap.get(row.sourceId())
                        );
                        case PRE_OPINION -> ReadingTimelineItem.preOpinion(
                                row.eventAt(),
                                row.sourceId(),
                                preOpinionMap.get(row.sourceId())
                        );
                        case MEETING_RETROSPECTIVE -> ReadingTimelineItem.meetingRetrospective(
                                row.eventAt(),
                                row.sourceId(),
                                meetingRetrospectiveMap.get(row.sourceId())
                        );
                    };
                })
                .toList();

        ReadingTimelineCursor nextCursor = null;
        if (hasNext) {
            ReadingTimelineIndexRow last = rows.get(rows.size() - 1);
            nextCursor = ReadingTimelineCursor.from(
                    last.eventAt(),
                    ReadingTimelineType.from(last.type()),
                    last.sourceId()
            );
        }

        return CursorResponse.of(items, pageSize, hasNext, nextCursor);
    }

    private int resolvePageSize(Integer size) {
        if (size == null || size < 1) {
            return DEFAULT_PAGE_SIZE;
        }
        return size;
    }
}
