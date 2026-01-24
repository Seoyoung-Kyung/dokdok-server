package com.dokdok.topic.dto.response;

import com.dokdok.topic.entity.Topic;
import com.dokdok.topic.entity.TopicAnswer;

import java.math.BigDecimal;
import java.util.List;

public record PreOpinionResponse(
        List<TopicInfo> topics,
        List<MemberPreOpinion> members
) {

    public record TopicInfo(
            Long topicId,
            String topicName,
            String topicDescription,
            String topicType
    ) {
        public static TopicInfo from(Topic topic) {
            return new TopicInfo(
                    topic.getId(),
                    topic.getTitle(),
                    topic.getDescription(),
                    topic.getTopicType().getDisplayName()
            );
        }
    }

    public record MemberPreOpinion(
            MemberInfo memberInfo,
            BookReviewInfo bookReview,
            List<TopicOpinion> topicOpinions
    ) { }

    public record MemberInfo(
            Long memberId,
            String nickname,
            String profileImage
    ) {
        public static MemberInfo of(Long memberId, String nickname, String profileImage) {
            return new MemberInfo(memberId, nickname, profileImage);
        }
    }

    public record BookReviewInfo(
            BigDecimal rating,
            List<String> bookKeywords,
            List<String> impressionKeywords
    ) {
        public static BookReviewInfo of(
                BigDecimal rating,
                List<String> bookKeywords,
                List<String> impressionKeywords
        ) {
            return new BookReviewInfo(rating, bookKeywords, impressionKeywords);
        }
    }

    public record TopicOpinion(
            Long topicId,
            String content
    ) {
        public static TopicOpinion of(TopicAnswer topicAnswer) {
            return new TopicOpinion(
                    topicAnswer.getTopic().getId(),
                    topicAnswer.getContent()
            );
        }
    }

    public static PreOpinionResponse of(
            List<TopicInfo> topics,
            List<MemberPreOpinion> members
    ) {
        return new PreOpinionResponse(topics, members);
    }
}