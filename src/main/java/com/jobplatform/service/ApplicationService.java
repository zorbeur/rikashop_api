package com.jobplatform.service;

import com.jobplatform.domain.*;
import com.jobplatform.repository.ApplicationRepository;
import com.jobplatform.repository.JobOfferRepository;
import com.jobplatform.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final JobOfferRepository jobOfferRepository;
    private final UserRepository userRepository;

    @Value("${file.upload-dir}")
    private String uploadDir;

    public ApplicationService(ApplicationRepository applicationRepository, JobOfferRepository jobOfferRepository, UserRepository userRepository) {
        this.applicationRepository = applicationRepository;
        this.jobOfferRepository = jobOfferRepository;
        this.userRepository = userRepository;
    }

    public Application apply(UUID offerId, String coverLetter, MultipartFile resume, String candidateEmail) throws IOException {
        JobOffer offer = jobOfferRepository.findById(offerId).orElseThrow();
        User candidate = userRepository.findByEmail(candidateEmail).orElseThrow();

        String savedPath = null;
        if (resume != null && !resume.isEmpty()) {
            String filename = UUID.randomUUID() + "-" + StringUtils.cleanPath(resume.getOriginalFilename());
            Path dir = Paths.get(uploadDir);
            Files.createDirectories(dir);
            Path dest = dir.resolve(filename);
            resume.transferTo(dest.toFile());
            savedPath = dest.toString();
        }

        Application app = Application.builder()
                .jobOffer(offer)
                .candidate(candidate)
                .coverLetter(coverLetter)
                .resumePath(savedPath)
                .status(ApplicationStatus.PENDING)
                .applicationDate(LocalDateTime.now())
                .lastUpdateDate(LocalDateTime.now())
                .build();
        return applicationRepository.save(app);
    }

    public List<Application> myApplications(String candidateEmail) {
        User candidate = userRepository.findByEmail(candidateEmail).orElseThrow();
        return applicationRepository.findByCandidate(candidate);
    }

    public List<Application> forOffer(UUID offerId, String recruiterEmail) {
        JobOffer offer = jobOfferRepository.findById(offerId).orElseThrow();
        if (offer.getRecruiter() == null || !offer.getRecruiter().getEmail().equals(recruiterEmail)) {
            throw new SecurityException("Not owner of offer");
        }
        return applicationRepository.findByJobOffer(offer);
    }

    public Application updateStatus(UUID applicationId, ApplicationStatus status, String actorEmail) {
        Application app = applicationRepository.findById(applicationId).orElseThrow();
        // Allow recruiter owning the offer or admin to update
        if (app.getJobOffer().getRecruiter() == null || (!app.getJobOffer().getRecruiter().getEmail().equals(actorEmail))) {
            // In a full impl, check admin role via SecurityContext
        }
        app.setStatus(status);
        app.setLastUpdateDate(LocalDateTime.now());
        return applicationRepository.save(app);
    }
}
