package com.dokdok.gathering.controller;

import com.dokdok.gathering.api.GatheringApi;
import com.dokdok.gathering.dto.request.GatheringCreateRequest;
import com.dokdok.gathering.dto.request.JoinGatheringMemberRequest;
import com.dokdok.gathering.dto.response.*;
import com.dokdok.gathering.dto.request.GatheringUpdateRequest;
import com.dokdok.gathering.service.GatheringService;
import com.dokdok.global.response.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/gatherings")
@RequiredArgsConstructor
@Validated
public class GatheringController implements GatheringApi {

    private final GatheringService gatheringService;

    @Override
    @PostMapping
    public ResponseEntity<ApiResponse<GatheringCreateResponse>> createGathering(
            @Valid @RequestBody GatheringCreateRequest request
    ) {
        GatheringCreateResponse response = gatheringService.createGathering(request);
        return ApiResponse.created(response, "모임 생성에 성공하였습니다.");
    }

    @Override
    @GetMapping("/join-request/{invitationLink}")
    public ResponseEntity<ApiResponse<GatheringCreateResponse>> joinGatheringInfo(
            @PathVariable("invitationLink") @NotBlank(message = "초대링크는 필수입니다") String invitationLink
    ) {
        GatheringCreateResponse response = gatheringService.getJoinGatheringInfo(invitationLink);
        return ApiResponse.success(response);
    }

    @Override
    @PostMapping("/join-request/{invitationLink}")
    public ResponseEntity<ApiResponse<GatheringJoinResponse>> joinGathering(
            @PathVariable("invitationLink") @NotBlank(message = "초대링크는 필수입니다") String invitationLink
    ) {
        GatheringJoinResponse response = gatheringService.joinGathering(invitationLink);
        return ApiResponse.success(response);
    }

    @PatchMapping("{gathering-id}/join-requests/{member-id}")
    public ResponseEntity<ApiResponse<Void>> handleJoinRequest(@PathVariable("gathering-id") Long gatheringId,
                                                               @PathVariable("member-id") Long memberId,
                                                               @RequestBody @Valid JoinGatheringMemberRequest request) {

        gatheringService.handleJoinRequest(gatheringId, memberId, request);
        return ApiResponse.success("해당 멤버가 " + request.approve_type().getDescription() + " 되었습니다.");
    }

    @Override
    @GetMapping
    public ResponseEntity<ApiResponse<MyGatheringListResponse>> getMyGatherings(Pageable pageable) {
        MyGatheringListResponse response = gatheringService.getMyGatherings(pageable);

        return ApiResponse.success(response, "모임 가입 요청이 완료되었습니다. 모임장의 승인을 기다려주세요.");
    }

    @Override
    @GetMapping("/{gatheringId}")
    public ResponseEntity<ApiResponse<GatheringDetailResponse>> getGatheringDetail(@PathVariable Long gatheringId){
        GatheringDetailResponse response = gatheringService.getGatheringDetail(gatheringId);

        return ApiResponse.success(response,"모임 상세정보 조회 성공");
    }

    @Override
    @PatchMapping("/{gatheringId}")
    public ResponseEntity<ApiResponse<GatheringUpdateResponse>> updateGathering(
            @PathVariable Long gatheringId,
            @Valid @RequestBody GatheringUpdateRequest request
    ) {
        GatheringUpdateResponse response = gatheringService.updateGathering(gatheringId, request);
        return ApiResponse.updated(response, "모임 정보 수정 성공");
    }

    @Override
    @DeleteMapping("/{gatheringId}")
    public ResponseEntity<ApiResponse<Void>> deleteGathering(@PathVariable Long gatheringId) {
        gatheringService.deleteGathering(gatheringId);
        return ApiResponse.deleted("모임 삭제 성공");
    }

    @DeleteMapping("/{gatheringId}/members/{userId}")
    public ResponseEntity<ApiResponse<Void>> removeMember(
            @PathVariable Long gatheringId,
            @PathVariable Long userId
    ){
        gatheringService.removeMember(gatheringId,userId);
        return ApiResponse.deleted("모임원 강퇴 성공");
    }
}
