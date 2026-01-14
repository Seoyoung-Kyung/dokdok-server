package com.dokdok.topic.dto.response;

import com.dokdok.topic.entity.TopicMessage;

public record TopicLikeResponse(
        Long topicId,
        Boolean liked,
        int newCount
) {

    public static TopicLikeResponse from(Long topicId, TopicMessage result, int newCount) {
        return new TopicLikeResponse(
                topicId,
                result == TopicMessage.LIKE_SUCCESS,
                newCount
        );
    }

}
