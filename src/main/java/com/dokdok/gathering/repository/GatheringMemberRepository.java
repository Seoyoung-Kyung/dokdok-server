package com.dokdok.gathering.repository;

import com.dokdok.gathering.entity.GatheringMember;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

@Repository
public interface GatheringMemberRepository extends JpaRepository<GatheringMember, Long> {

    /**
     * 사용자가 특정 모임의 활성 멤버인지 확인
     */
    boolean existsByGatheringIdAndUserId(Long gatheringId, Long userId);

    /**
     * 특정 모임의 활성 멤버 수 조회
     */
    int countByGatheringIdAndRemovedAtIsNull(Long gatheringId);

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
     * 특정 유저가 특정 모임의 멤버인지 확인 (Gathering fetch join)
     */
    @Query("SELECT gm FROM GatheringMember gm " +
            "JOIN FETCH gm.gathering g " +
            "WHERE gm.gathering.id = :gatheringId " +
            "AND gm.user.id = :userId " +
            "AND gm.removedAt IS NULL")
    Optional<GatheringMember> findByGatheringIdAndUserId(
            @Param("gatheringId") Long gatheringId,
            @Param("userId") Long userId
    );

    /**
     * 특정 모임의 모든 활성 멤버 조회 (User 정보 포함)
     */
    @Query("SELECT gm FROM GatheringMember gm " +
            "JOIN FETCH gm.user u " +
            "JOIN FETCH gm.gathering g " +
            "WHERE gm.gathering.id = :gatheringId " +
            "AND gm.removedAt IS NULL")
    List<GatheringMember> findAllMembersByGatheringId(@Param("gatheringId") Long gatheringId);

    /**
     * 특정 모임의 ACTIVE 상태 멤버 수 조회
     */
    @Query("SELECT count(gm) FROM GatheringMember gm " +
            "WHERE gm.gathering.id = :gatheringId " +
            "AND gm.memberStatus = 'ACTIVE' " +
            "AND gm.removedAt IS NULL")
    int countActiveMembersByStatus(@Param("gatheringId") Long gatheringId);
}
