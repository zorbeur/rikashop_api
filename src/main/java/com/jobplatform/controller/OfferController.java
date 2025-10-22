package com.jobplatform.controller;

import com.jobplatform.domain.JobOffer;
import com.jobplatform.service.OfferService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/offers")
public class OfferController {

    private final OfferService offerService;

    public OfferController(OfferService offerService) {
        this.offerService = offerService;
    }

    // Public endpoints
    @GetMapping
    public ResponseEntity<List<JobOffer>> list() {
        return ResponseEntity.ok(offerService.publicList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobOffer> get(@PathVariable UUID id) {
        return ResponseEntity.ok(offerService.get(id));
    }

    // Authenticated (Recruiter/Admin)
    @PostMapping
    @PreAuthorize("hasAnyRole('RECRUTEUR','ADMIN')")
    public ResponseEntity<JobOffer> create(@RequestBody JobOffer offer, Authentication auth) {
        return ResponseEntity.ok(offerService.create(offer, auth));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('RECRUTEUR','ADMIN')")
    public ResponseEntity<JobOffer> update(@PathVariable UUID id, @RequestBody JobOffer offer, Authentication auth) {
        return ResponseEntity.ok(offerService.update(id, offer, auth));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('RECRUTEUR','ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable UUID id, Authentication auth) {
        offerService.delete(id, auth);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/my-offers")
    @PreAuthorize("hasRole('RECRUTEUR')")
    public ResponseEntity<List<JobOffer>> myOffers(Authentication auth) {
        return ResponseEntity.ok(offerService.myOffers(auth));
    }

    @GetMapping("/search")
    public ResponseEntity<List<JobOffer>> search(@RequestParam(required = false, name = "q") String query,
                                                 @RequestParam(required = false) String location,
                                                 @RequestParam(required = false) String contractType) {
        return ResponseEntity.ok(offerService.search(query, location, contractType));
    }
}
