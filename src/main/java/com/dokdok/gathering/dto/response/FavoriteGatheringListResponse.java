package com.dokdok.gathering.dto.response;

import lombok.Builder;

import java.util.List;

@Builder
public record FavoriteGatheringListResponse(
        List<GatheringListItemResponse> gatherings
) {
    public static FavoriteGatheringListResponse from(
            List<GatheringListItemResponse> favoriteGatherings
    ){
        return FavoriteGatheringListResponse.builder()
                .gatherings(favoriteGatherings)
                .build();
    }
}
