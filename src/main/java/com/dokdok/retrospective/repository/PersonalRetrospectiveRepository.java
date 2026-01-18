package com.dokdok.retrospective.repository;

import org.springframework.stereotype.Repository;

import com.dokdok.retrospective.entity.PersonalMeetingRetrospective;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


@Repository
public interface PersonalRetrospectiveRepository extends JpaRepository<PersonalMeetingRetrospective, Long> {

    boolean existsByMeetingIdAndUserId(Long meetingId, Long userId);

}