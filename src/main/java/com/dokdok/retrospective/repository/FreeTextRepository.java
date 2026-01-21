package com.dokdok.retrospective.repository;

import com.dokdok.retrospective.dto.projection.FreeTextProjection;
import com.dokdok.retrospective.entity.RetrospectiveFreeText;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FreeTextRepository extends JpaRepository<RetrospectiveFreeText, Long> {

    List<RetrospectiveFreeText> findByPersonalMeetingRetrospective_Id(Long retrospectiveId);

    @Query("""
            SELECT new com.dokdok.retrospective.dto.response.FreeTextProjection(
                        ft.personalMeetingRetrospective.id,
                        ft.title,
                        ft.content
            )
            FROM RetrospectiveFreeText ft
            JOIN ft.personalMeetingRetrospective pmr
            WHERE ft.personalMeetingRetrospective.id IN :retrospectiveIds
            """)
    List<FreeTextProjection> findByRetrospectiveIds(List<Long> retrospectiveIds);
}
