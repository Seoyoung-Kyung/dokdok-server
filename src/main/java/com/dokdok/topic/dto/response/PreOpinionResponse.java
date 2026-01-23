package com.dokdok.topic.dto.response;

import com.dokdok.topic.entity.TopicAnswer;
import com.dokdok.topic.entity.TopicType;

import java.math.BigDecimal;
import java.util.List;

public record PreOpinionResponse(
        UserInfo userInfo,
        BookReviewInfo BookReviewInfo,
        List<TopicAnswerInfo> topicAnswers
) {

    public record UserInfo(
            Long userId,
            String nickname,
            String profileImage
    ) {
        public static UserInfo of(Long userId, String nickname, String profileImage) {
            return new UserInfo(
                    userId,
                    nickname,
                    profileImage
            );
        }
    }

    public record BookReviewInfo(
            Long userId,
            BigDecimal rating,
            List<String> bookKeyword,
            List<String> impressionKeyword
    ) {
        public static BookReviewInfo of(
                Long userId,
                BigDecimal rating,
                List<String> bookKeyword,
                List<String> impressionKeyword
        ) {
            return new BookReviewInfo(
                    userId,
                    rating,
                    bookKeyword,
                    impressionKeyword
            );
        }
    }

    public record TopicAnswerInfo(
            Long userId,
            Long topicId,
            String title,
            String topicDescription,
            TopicType topicType,
            String content
    ) {
        public static TopicAnswerInfo of(
                TopicAnswer topicAnswer
        ) {
            return new TopicAnswerInfo(
                    topicAnswer.getUser().getId(),
                    topicAnswer.getTopic().getId(),
                    topicAnswer.getTopic().getTitle(),
                    topicAnswer.getTopic().getDescription(),
                    topicAnswer.getTopic().getTopicType(),
                    topicAnswer.getContent()
            );
        }
    }

    public static PreOpinionResponse from(
            UserInfo userInfo,
            BookReviewInfo BookReviewInfo,
            List<TopicAnswerInfo> topicAnswers
    ) {
        return new PreOpinionResponse(
                userInfo,
                BookReviewInfo,
                topicAnswers
        );
    }
}