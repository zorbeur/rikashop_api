package com.jobplatform.controller;

import com.jobplatform.domain.Application;
import com.jobplatform.domain.ApplicationStatus;
import com.jobplatform.service.ApplicationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @PostMapping
    @PreAuthorize("hasRole('CANDIDAT')")
    public ResponseEntity<Application> apply(@RequestParam UUID offerId,
                                             @RequestParam(required = false) String coverLetter,
                                             @RequestParam(required = false, name = "resume") MultipartFile resume,
                                             Authentication auth) throws IOException {
        Application app = applicationService.apply(offerId, coverLetter, resume, auth.getName());
        return ResponseEntity.ok(app);
    }

    @GetMapping("/my-applications")
    @PreAuthorize("hasRole('CANDIDAT')")
    public ResponseEntity<List<Application>> myApplications(Authentication auth) {
        return ResponseEntity.ok(applicationService.myApplications(auth.getName()));
    }

    @GetMapping("/offer/{offerId}")
    @PreAuthorize("hasAnyRole('RECRUTEUR','ADMIN')")
    public ResponseEntity<List<Application>> forOffer(@PathVariable UUID offerId, Authentication auth) {
        return ResponseEntity.ok(applicationService.forOffer(offerId, auth.getName()));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('RECRUTEUR','ADMIN')")
    public ResponseEntity<Application> updateStatus(@PathVariable UUID id, @RequestParam ApplicationStatus status, Authentication auth) {
        return ResponseEntity.ok(applicationService.updateStatus(id, status, auth.getName()));
    }
}
