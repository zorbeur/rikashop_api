package com.jobplatform.controller;

import com.jobplatform.domain.Application;
import com.jobplatform.domain.JobOffer;
import com.jobplatform.domain.User;
import com.jobplatform.repository.ApplicationRepository;
import com.jobplatform.repository.JobOfferRepository;
import com.jobplatform.repository.UserRepository;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;
    private final JobOfferRepository jobOfferRepository;
    private final ApplicationRepository applicationRepository;

    public UserController(UserRepository userRepository, JobOfferRepository jobOfferRepository, ApplicationRepository applicationRepository) {
        this.userRepository = userRepository;
        this.jobOfferRepository = jobOfferRepository;
        this.applicationRepository = applicationRepository;
    }

    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<User> profile(Authentication auth) {
        return ResponseEntity.ok(userRepository.findByEmail(auth.getName()).orElseThrow());
    }

    public record UpdateProfileRequest(@NotBlank String firstName, @NotBlank String lastName, @Email String email, String phone) {}

    @PutMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<User> updateProfile(@RequestBody UpdateProfileRequest req, Authentication auth) {
        User user = userRepository.findByEmail(auth.getName()).orElseThrow();
        user.setFirstName(req.firstName());
        user.setLastName(req.lastName());
        user.setPhone(req.phone());
        // Keep email the same if changing would conflict
        if (!user.getEmail().equalsIgnoreCase(req.email()) && !userRepository.existsByEmail(req.email())) {
            user.setEmail(req.email().toLowerCase());
        }
        return ResponseEntity.ok(userRepository.save(user));
    }

    @GetMapping("/dashboard")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> dashboard(Authentication auth) {
        User user = userRepository.findByEmail(auth.getName()).orElseThrow();
        Map<String, Object> data = new HashMap<>();
        switch (user.getRole()) {
            case CANDIDAT -> {
                List<Application> myApps = applicationRepository.findByCandidate(user);
                data.put("applicationsCount", myApps.size());
            }
            case RECRUTEUR -> {
                List<JobOffer> myOffers = jobOfferRepository.findAll().stream()
                        .filter(o -> o.getRecruiter() != null && o.getRecruiter().getId().equals(user.getId()))
                        .toList();
                data.put("offersCount", myOffers.size());
            }
            case ADMIN -> {
                data.put("users", userRepository.count());
                data.put("offers", jobOfferRepository.count());
                data.put("applications", applicationRepository.count());
            }
        }
        return ResponseEntity.ok(data);
    }
}
