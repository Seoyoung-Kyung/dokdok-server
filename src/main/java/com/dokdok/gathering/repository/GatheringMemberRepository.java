package com.dokdok.gathering.repository;

import com.dokdok.gathering.entity.GatheringMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GatheringMemberRepository extends JpaRepository<GatheringMember, Long> {

    /**
     * 사용자가 특정 모임의 활성 멤버인지 확인
     */
    boolean existsByGatheringIdAndUserIdAndRemovedAtIsNull(Long gatheringId, Long userId);
}