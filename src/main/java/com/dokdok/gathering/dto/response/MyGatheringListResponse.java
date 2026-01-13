package com.dokdok.gathering.dto;

import com.dokdok.gathering.entity.GatheringMember;
import lombok.Builder;
import org.springframework.data.domain.Page;

import java.util.List;

@Builder
public record MyGatheringListResponse(
        List<GatheringSimpleResponse> gatherings,
        Integer totalCount,
        Integer currentPage,
        Integer pageSize,
        Integer totalPages
) {
    public static MyGatheringListResponse from(
            List<GatheringSimpleResponse> gatherings,
            Page<GatheringMember> page
    ){
        return MyGatheringListResponse.builder()
                .gatherings(gatherings)
                .totalCount((int)page.getTotalElements())
                .currentPage(page.getNumber())
                .pageSize(page.getSize())
                .totalPages(page.getTotalPages())
                .build();
    }
}
