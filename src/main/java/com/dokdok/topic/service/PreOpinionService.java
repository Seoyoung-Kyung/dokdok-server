package com.dokdok.topic.service;

import com.dokdok.book.entity.BookReview;
import com.dokdok.book.entity.BookReviewKeyword;
import com.dokdok.book.entity.KeywordType;
import com.dokdok.book.repository.BookRepository;
import com.dokdok.book.repository.BookReviewKeywordRepository;
import com.dokdok.book.repository.BookReviewRepository;
import com.dokdok.book.service.BookValidator;
import com.dokdok.gathering.service.GatheringValidator;
import com.dokdok.global.util.SecurityUtil;
import com.dokdok.keyword.entity.Keyword;
import com.dokdok.meeting.service.MeetingValidator;
import com.dokdok.retrospective.dto.response.PersonalRetrospectiveFormResponse;
import com.dokdok.retrospective.entity.PersonalMeetingRetrospective;
import com.dokdok.storage.service.StorageService;
import com.dokdok.topic.dto.response.PreOpinionResponse;
import com.dokdok.topic.entity.TopicAnswer;
import com.dokdok.topic.repository.TopicAnswerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PreOpinionService {

    private final GatheringValidator gatheringValidator;
    private final MeetingValidator meetingValidator;
    private final TopicValidator topicValidator;
    private final TopicAnswerRepository topicAnswerRepository;
    private final BookReviewRepository bookReviewRepository;
    private final BookReviewKeywordRepository bookReviewKeywordRepository;
    private final StorageService storageService;

    @Transactional(readOnly = true)
    public List<PreOpinionResponse> findPreOpinions(
            Long gatheringId,
            Long meetingId
    ) {
        Long userId = SecurityUtil.getCurrentUserId();

        gatheringValidator.validateGathering(gatheringId); // 모임 검증
        meetingValidator.validateMeetingInGathering(meetingId, gatheringId); // 모임에 포함된 약속인지
        meetingValidator.validateMeetingMember(meetingId, userId); // 로그인한 사용자가 약속에 포함된 멤버인지
        topicValidator.validateUserHasWrittenBookReview(meetingId, userId); // 주제 답변을 작성한 멤버인지

        List<BookReview> bookReviews = bookReviewRepository.findByMeetingId(meetingId);

        List<Long> bookReviewIds = bookReviews.stream()
                .map(BookReview::getId)
                .toList();

        List<BookReviewKeyword> keywords = bookReviewKeywordRepository.findByBookReviewIds(bookReviewIds);

        Map<Long, List<BookReviewKeyword>> keywordsByReviewId =
                keywords.stream()
                        .collect(Collectors.groupingBy(
                                k -> k.getBookReview().getId()
                        ));

        Map<Long, PreOpinionResponse.BookReviewInfo> bookReviewInfoByUserId = bookReviews.stream()
                .collect(Collectors.toMap(
                        bookReview -> bookReview.getUser().getId(),
                        bookReview -> {
                            List<BookReviewKeyword> reviewKeywords = keywordsByReviewId
                                    .getOrDefault(bookReview.getId(), List.of());
                            Map<KeywordType, List<String>> keywordMap = toKeywordMap(reviewKeywords);
                            return PreOpinionResponse.BookReviewInfo.of(
                                    bookReview.getUser().getId(),
                                    bookReview.getRating(),
                                    keywordMap.getOrDefault(KeywordType.BOOK, List.of()),
                                    keywordMap.getOrDefault(KeywordType.IMPRESSION, List.of())
                            );
                        }
                ));

        List<TopicAnswer> topicAnswers = topicAnswerRepository.findByMeetingId(meetingId);

        Map<Long, List<PreOpinionResponse.TopicAnswerInfo>> topicAnswersByUserId = topicAnswers.stream()
                .map(PreOpinionResponse.TopicAnswerInfo::of)
                .collect(Collectors.groupingBy(PreOpinionResponse.TopicAnswerInfo::userId));

        Map<Long, PreOpinionResponse.UserInfo> userInfoByUserId = bookReviews.stream()
                .collect(Collectors.toMap(
                        bookReview -> bookReview.getUser().getId(),
                        bookReview -> {
                            String presignedUrl = storageService.getPresignedProfileImage(
                                    bookReview.getUser().getProfileImageUrl()
                            );
                            return PreOpinionResponse.UserInfo.of(
                                    bookReview.getUser().getId(),
                                    bookReview.getUser().getNickname(),
                                    presignedUrl
                            );
                        },
                        (existing, replacement) -> existing // 중복 시 기존 값 유지
                ));

        return userInfoByUserId.keySet().stream()
                .map(uid -> PreOpinionResponse.from(
                        userInfoByUserId.get(uid),
                        bookReviewInfoByUserId.get(uid),
                        topicAnswersByUserId.getOrDefault(uid, List.of())
                ))
                .toList();
    }


    // 타입별로 Map 저장
    private Map<KeywordType, List<String>> toKeywordMap(List<BookReviewKeyword> keywords) {
        return keywords.stream()
                .collect(Collectors.groupingBy(
                        k -> k.getKeyword().getKeywordType(),
                        Collectors.mapping(
                                k -> k.getKeyword().getKeywordName(),
                                Collectors.toList()
                        )

                ));
    }

}
