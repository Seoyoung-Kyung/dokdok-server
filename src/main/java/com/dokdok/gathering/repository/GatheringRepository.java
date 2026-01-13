package com.dokdok.gathering.repository;

import com.dokdok.gathering.entity.Gathering;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GatheringRepository extends JpaRepository<Gathering, Long> {

    boolean existsByInvitationLink(String invitationLink);
}
