package com.dokdok.gathering.repository;

import com.dokdok.gathering.entity.Gathering;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GatheringRepository extends JpaRepository<Gathering, Long> {
}
