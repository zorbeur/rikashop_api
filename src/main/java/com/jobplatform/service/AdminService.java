package com.jobplatform.service;

import com.jobplatform.domain.*;
import com.jobplatform.repository.ApplicationRepository;
import com.jobplatform.repository.JobOfferRepository;
import com.jobplatform.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final JobOfferRepository jobOfferRepository;
    private final ApplicationRepository applicationRepository;

    public AdminService(UserRepository userRepository, JobOfferRepository jobOfferRepository, ApplicationRepository applicationRepository) {
        this.userRepository = userRepository;
        this.jobOfferRepository = jobOfferRepository;
        this.applicationRepository = applicationRepository;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User updateUserRole(UUID userId, Role role) {
        User user = userRepository.findById(userId).orElseThrow();
        user.setRole(role);
        return userRepository.save(user);
    }

    public void deleteUser(UUID userId) {
        userRepository.deleteById(userId);
    }

    public Map<String, Object> statistics() {
        Map<String, Object> stats = new HashMap<>();
        long totalUsers = userRepository.count();
        long totalOffers = jobOfferRepository.count();
        long totalApplications = applicationRepository.count();
        long pendingApps = applicationRepository.countByStatus(ApplicationStatus.PENDING);
        stats.put("totalUsers", totalUsers);
        stats.put("totalOffers", totalOffers);
        stats.put("totalApplications", totalApplications);
        stats.put("pendingApplications", pendingApps);
        return stats;
    }

    public List<Application> allApplications() {
        return applicationRepository.findAll();
    }
}
