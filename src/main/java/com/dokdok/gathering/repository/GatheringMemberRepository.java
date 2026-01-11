package com.dokdok.gathering.repository;

import com.dokdok.gathering.entity.GatheringMember;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GatheringMemberRepository extends JpaRepository<GatheringMember, Long> {

    int countByGatheringIdAndRemovedAtIsNull(Long gatheringId);
}
