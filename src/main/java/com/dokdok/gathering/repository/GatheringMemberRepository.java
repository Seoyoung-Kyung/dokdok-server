package com.dokdok.gathering.repository;

import com.dokdok.gathering.entity.GatheringMember;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GatheringMemberRepository extends JpaRepository<GatheringMember, Long> {

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
