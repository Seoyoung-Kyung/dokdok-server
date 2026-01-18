package com.dokdok.retrospective.repository;

import com.dokdok.retrospective.entity.RetrospectiveFreeText;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FreeTextRepository extends JpaRepository<RetrospectiveFreeText, Long> {

    List<RetrospectiveFreeText> findByPersonalMeetingRetrospective_Id(Long retrospectiveId);
}
