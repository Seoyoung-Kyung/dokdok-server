package com.dokdok.topic.service;

import com.dokdok.book.entity.BookReview;
import com.dokdok.book.entity.BookReviewKeyword;
import com.dokdok.book.entity.KeywordType;
import com.dokdok.book.repository.BookReviewKeywordRepository;
import com.dokdok.book.repository.BookReviewRepository;
import com.dokdok.gathering.service.GatheringValidator;
import com.dokdok.global.util.SecurityUtil;
import com.dokdok.meeting.entity.MeetingMember;
import com.dokdok.meeting.repository.MeetingMemberRepository;
import com.dokdok.meeting.service.MeetingValidator;
import com.dokdok.storage.service.StorageService;
import com.dokdok.topic.dto.response.PreOpinionResponse;
import com.dokdok.topic.dto.response.PreOpinionResponse.BookReviewInfo;
import com.dokdok.topic.repository.TopicAnswerRepository;
import com.dokdok.topic.repository.TopicRepository;
import com.dokdok.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PreOpinionService {

    private final GatheringValidator gatheringValidator;
    private final MeetingValidator meetingValidator;
    private final TopicValidator topicValidator;
    private final TopicRepository topicRepository;
    private final MeetingMemberRepository meetingMemberRepository;
    private final TopicAnswerRepository topicAnswerRepository;
    private final BookReviewRepository bookReviewRepository;
    private final BookReviewKeywordRepository bookReviewKeywordRepository;
    private final StorageService storageService;

    @Transactional(readOnly = true)
    public PreOpinionResponse findPreOpinions(Long gatheringId, Long meetingId) {
        Long userId = SecurityUtil.getCurrentUserId();
        validateAccess(gatheringId, meetingId, userId);

        List<PreOpinionResponse.TopicInfo> topicInfos = buildTopicInfos(meetingId);
        List<MeetingMember> meetingMembers = meetingMemberRepository.findAllByMeetingId(meetingId);

        List<PreOpinionResponse.MemberPreOpinion> preOpinionData = buildPreOpinionData(meetingId, meetingMembers);

        return new PreOpinionResponse(topicInfos, preOpinionData);
    }

    private void validateAccess(Long gatheringId, Long meetingId, Long userId) {
        gatheringValidator.validateGathering(gatheringId);
        meetingValidator.validateMeetingInGathering(meetingId, gatheringId);
        meetingValidator.validateMeetingMember(meetingId, userId);
        topicValidator.validateUserHasWrittenAnswer(meetingId, userId);
    }

    private List<PreOpinionResponse.TopicInfo> buildTopicInfos(Long meetingId) {
        return topicRepository.findConfirmedTopics(meetingId).stream()
                .map(PreOpinionResponse.TopicInfo::from)
                .toList();
    }

    private List<PreOpinionResponse.MemberInfo> buildMemberInfos(List<MeetingMember> meetingMembers) {
        return meetingMembers.stream()
                .map(mm -> {
                    User user = mm.getUser();
                    String presignedUrl = storageService.getPresignedProfileImage(user.getProfileImageUrl());
                    return PreOpinionResponse.MemberInfo.of(user.getId(), user.getNickname(), presignedUrl);
                })
                .toList();
    }

    private List<PreOpinionResponse.MemberPreOpinion> buildPreOpinionData(Long meetingId, List<MeetingMember> meetingMembers) {
        List<Long> userIds = meetingMembers.stream()
                .map(mm -> mm.getUser().getId())
                .toList();

        List<PreOpinionResponse.MemberInfo> memberInfos = buildMemberInfos(meetingMembers);

        // 모든 멤버의 책 평가 일괄 조회
        Map<Long, BookReview> bookReviewByUserId = bookReviewRepository.findByUserIdIn(userIds).stream()
                .collect(Collectors.toMap(
                        br -> br.getUser().getId(),
                        br -> br,
                        (existing, replacement) -> existing
                ));

        // 책 평가별 키워드 일괄 조회
        List<Long> bookReviewIds = bookReviewByUserId.values().stream()
                .map(BookReview::getId)
                .toList();
        Map<Long, List<BookReviewKeyword>> keywordsByReviewId = bookReviewKeywordRepository
                .findByBookReviewIds(bookReviewIds).stream()
                .collect(Collectors.groupingBy(k -> k.getBookReview().getId()));

        // 주제 답변 일괄 조회
        Map<Long, List<PreOpinionResponse.TopicOpinion>> topicAnswersByUserId =
                topicAnswerRepository.findByMeetingId(meetingId).stream()
                        .collect(Collectors.groupingBy(
                                ta -> ta.getUser().getId(),
                                Collectors.mapping(
                                        PreOpinionResponse.TopicOpinion::of,
                                        Collectors.toList()
                                )
                        ));


        return meetingMembers.stream()
                .map(mm -> {
                    User user = mm.getUser();
                    Long memberId = user.getId();

                    String presignedUrl = storageService.getPresignedProfileImage(user.getProfileImageUrl());
                    PreOpinionResponse.MemberInfo memberInfo
                            = PreOpinionResponse.MemberInfo.of(memberId, user.getNickname(), presignedUrl);

                    BookReview review = bookReviewByUserId.get(memberId);
                    BookReviewInfo bookReviewInfo = review != null
                            ? toBookReviewInfo(review, keywordsByReviewId)
                            : null;

                    List<PreOpinionResponse.TopicOpinion> topicAnswers = topicAnswersByUserId.getOrDefault(memberId, List.of());

                    return new PreOpinionResponse.MemberPreOpinion(memberInfo, bookReviewInfo, topicAnswers);
                })
                .toList();
    }

    private BookReviewInfo toBookReviewInfo(
            BookReview bookReview,
            Map<Long, List<BookReviewKeyword>> keywordsByReviewId) {
        List<BookReviewKeyword> reviewKeywords =
                keywordsByReviewId.getOrDefault(bookReview.getId(), List.of());
        Map<KeywordType, List<String>> keywordMap = toKeywordMap(reviewKeywords);

        return BookReviewInfo.of(
                bookReview.getRating(),
                keywordMap.getOrDefault(KeywordType.BOOK, List.of()),
                keywordMap.getOrDefault(KeywordType.IMPRESSION, List.of())
        );
    }

    private Map<KeywordType, List<String>> toKeywordMap(List<BookReviewKeyword> keywords) {
        return keywords.stream()
                .collect(Collectors.groupingBy(
                        k -> k.getKeyword().getKeywordType(),
                        Collectors.mapping(k -> k.getKeyword().getKeywordName(), Collectors.toList())
                ));
    }
}
