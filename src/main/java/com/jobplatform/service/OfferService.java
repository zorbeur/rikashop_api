package com.jobplatform.service;

import com.jobplatform.domain.JobOffer;
import com.jobplatform.domain.JobStatus;
import com.jobplatform.domain.User;
import com.jobplatform.repository.JobOfferRepository;
import com.jobplatform.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@Service
public class OfferService {

    private final JobOfferRepository jobOfferRepository;
    private final UserRepository userRepository;

    public OfferService(JobOfferRepository jobOfferRepository, UserRepository userRepository) {
        this.jobOfferRepository = jobOfferRepository;
        this.userRepository = userRepository;
    }

    public List<JobOffer> publicList() {
        return jobOfferRepository.findByStatus(JobStatus.ACTIVE);
    }

    public JobOffer get(UUID id) {
        return jobOfferRepository.findById(id).orElseThrow();
    }

    public JobOffer create(JobOffer offer, Authentication authentication) {
        User recruiter = userRepository.findByEmail(authentication.getName()).orElseThrow();
        offer.setRecruiter(recruiter);
        offer.setStatus(JobStatus.ACTIVE);
        offer.setPublicationDate(LocalDateTime.now());
        return jobOfferRepository.save(offer);
    }

    public JobOffer update(UUID id, JobOffer updated, Authentication authentication) {
        JobOffer existing = get(id);
        if (!isAdmin(authentication) && !existing.getRecruiter().getEmail().equals(authentication.getName())) {
            throw new SecurityException("Not owner of offer");
        }
        existing.setTitle(updated.getTitle());
        existing.setDescription(updated.getDescription());
        existing.setRequirements(updated.getRequirements());
        existing.setLocation(updated.getLocation());
        existing.setSalaryRange(updated.getSalaryRange());
        existing.setContractType(updated.getContractType());
        existing.setStatus(updated.getStatus());
        existing.setExpirationDate(updated.getExpirationDate());
        return jobOfferRepository.save(existing);
    }

    public void delete(UUID id, Authentication authentication) {
        JobOffer existing = get(id);
        if (!isAdmin(authentication) && !existing.getRecruiter().getEmail().equals(authentication.getName())) {
            throw new SecurityException("Not owner of offer");
        }
        jobOfferRepository.delete(existing);
    }

    public List<JobOffer> myOffers(Authentication authentication) {
        String email = authentication.getName();
        return jobOfferRepository.findAll().stream()
                .filter(o -> o.getRecruiter() != null && email.equals(o.getRecruiter().getEmail()))
                .toList();
    }

    public List<JobOffer> search(String query, String location, String contractType) {
        Stream<JobOffer> stream = jobOfferRepository.findByStatus(JobStatus.ACTIVE).stream();
        if (query != null && !query.isBlank()) {
            String q = query.toLowerCase();
            stream = stream.filter(o ->
                    (o.getTitle() != null && o.getTitle().toLowerCase().contains(q)) ||
                    (o.getDescription() != null && o.getDescription().toLowerCase().contains(q)) ||
                    (o.getRequirements() != null && o.getRequirements().toLowerCase().contains(q))
            );
        }
        if (location != null && !location.isBlank()) {
            String loc = location.toLowerCase();
            stream = stream.filter(o -> o.getLocation() != null && o.getLocation().toLowerCase().contains(loc));
        }
        if (contractType != null && !contractType.isBlank()) {
            String ct = contractType.toLowerCase();
            stream = stream.filter(o -> o.getContractType() != null && o.getContractType().toLowerCase().contains(ct));
        }
        return stream.toList();
    }

    private boolean isAdmin(Authentication authentication) {
        if (authentication == null) return false;
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            if ("ROLE_ADMIN".equals(authority.getAuthority())) {
                return true;
            }
        }
        return false;
    }
}
