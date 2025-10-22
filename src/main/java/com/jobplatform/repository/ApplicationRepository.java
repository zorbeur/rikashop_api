package com.jobplatform.repository;

import com.jobplatform.domain.Application;
import com.jobplatform.domain.ApplicationStatus;
import com.jobplatform.domain.JobOffer;
import com.jobplatform.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ApplicationRepository extends JpaRepository<Application, UUID> {
    List<Application> findByCandidate(User candidate);
    List<Application> findByJobOffer(JobOffer jobOffer);
    long countByStatus(ApplicationStatus status);
}
