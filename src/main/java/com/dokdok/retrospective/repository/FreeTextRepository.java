package com.dokdok.retrospective.repository;

import com.dokdok.retrospective.entity.RetrospectiveFreeText;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FreeTextRepository extends JpaRepository<RetrospectiveFreeText, Long> {

    List<RetrospectiveFreeText> findByPersonalMeetingRetrospective_Id(Long retrospectiveId);

    @Query("""
            SELECT ft
            FROM RetrospectiveFreeText ft
            WHERE ft.personalMeetingRetrospective.id IN :retrospectiveIds
            """)
    List<RetrospectiveFreeText> findByRetrospectiveIds(List<Long> retrospectiveIds);
}
