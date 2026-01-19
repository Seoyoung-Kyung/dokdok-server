package com.dokdok.retrospective.repository;

import com.dokdok.retrospective.entity.MeetingRetrospective;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RetrospectiveRepository extends JpaRepository<MeetingRetrospective, Long> {

    List<MeetingRetrospective> findAllByMeetingId(Long meetingId);

    Optional<MeetingRetrospective> findByIdAndMeetingId(Long meetingRetrospectiveId, Long meetingId);

}
