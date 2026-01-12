package com.dokdok.gathering.repository;

import com.dokdok.gathering.entity.GatheringMember;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface GatheringMemberRepository extends JpaRepository<GatheringMember, Long> {
  
    int countByGatheringIdAndRemovedAtIsNull(Long gatheringId);

    /**
     * 사용자가 특정 모임의 활성 멤버인지 확인
     */
    boolean existsByGatheringIdAndUserIdAndRemovedAtIsNull(Long gatheringId, Long userId);

    /**
     * 사용자의 활성 모임 목록 조회
     */
    @Query("SELECT gm FROM GatheringMember gm " +
            "JOIN FETCH gm.gathering g " +
            "WHERE gm.user.id = :userId " +
            "AND  gm.removedAt IS NULL")
    Page<GatheringMember> findActiveGatheringsByUserId(
            @Param("userId") Long userId,
            Pageable pageable
    );

    /**
     * 특정 모임의 멤버 수 조회
     */
    @Query("SELECT count(gm) FROM GatheringMember gm " +
            "WHERE gm.gathering.id = :gatheringId " +
            "AND gm.removedAt IS NULL")
    Integer countActiveMembers(@Param("gatheringId") Long gatheringId);

}