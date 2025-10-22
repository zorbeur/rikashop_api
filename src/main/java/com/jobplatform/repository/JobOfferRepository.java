package com.jobplatform.repository;

import com.jobplatform.domain.JobOffer;
import com.jobplatform.domain.JobStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface JobOfferRepository extends JpaRepository<JobOffer, UUID> {
    List<JobOffer> findByStatus(JobStatus status);
}
