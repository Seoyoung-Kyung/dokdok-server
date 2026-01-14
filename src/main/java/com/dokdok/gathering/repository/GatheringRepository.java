package com.dokdok.gathering.repository;

import com.dokdok.gathering.entity.Gathering;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GatheringRepository extends JpaRepository<Gathering, Long> {

    boolean existsByInvitationLink(String invitationLink);

    @Query("""
            SELECT g
            FROM Gathering g
            JOIN FETCH g.gatheringLeader
            WHERE g.invitationLink = :invitationLink
            """)
    Optional<Gathering> findGatheringByInvitationLink(@Param("invitationLink") String invitationLink);
}
