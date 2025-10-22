package com.jobplatform.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String firstName;
    private String lastName;

    @Column(unique = true, nullable = false)
    private String email;

    private String password;
    private String phone;

    @Enumerated(EnumType.STRING)
    private Role role;

    private Boolean isEnabled = false;
    private Boolean isVerified = false;
    private String verificationToken;
    private LocalDateTime tokenExpiry;

    private String refreshToken;
    private LocalDateTime refreshTokenExpiry;

    private String passwordResetToken;
    private LocalDateTime passwordResetExpiry;

    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "recruiter")
    private List<JobOffer> jobOffers;

    @OneToMany(mappedBy = "candidate")
    private List<Application> applications;

    @PrePersist
    public void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
