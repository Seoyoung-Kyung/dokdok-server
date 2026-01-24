package com.dokdok.gathering.dto.response;

import com.dokdok.gathering.entity.GatheringMember;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record MyGatheringListResponse(
        List<GatheringListItemResponse> items,
        Integer pageSize,
        Boolean hasNext,
        NextCursor nextCursor
) {
    @Builder
    public record NextCursor(
            LocalDateTime joinedAt,
            Long gatheringMemberId
    ){}

    public static MyGatheringListResponse from(
            List<GatheringListItemResponse> items,
            int pageSize,
            boolean hasNext,
            GatheringMember lastMember
    ) {
        NextCursor cursor = hasNext && lastMember != null
                ? NextCursor.builder()
                .joinedAt(lastMember.getJoinedAt())
                .gatheringMemberId(lastMember.getId())
                .build()
                : null;

        return MyGatheringListResponse.builder()
                .items(items)
                .pageSize(pageSize)
                .hasNext(hasNext)
                .nextCursor(cursor)
                .build();
    }
}
