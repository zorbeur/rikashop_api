package com.jobplatform.web;

import com.jobplatform.domain.Role;
import com.jobplatform.dto.auth.RegisterRequest;
import com.jobplatform.dto.auth.LoginRequest;
import com.jobplatform.dto.auth.VerifyEmailRequest;
import com.jobplatform.repository.UserRepository;
import com.jobplatform.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;


@Controller
public class AuthWebController {

    private final AuthService authService;
    private final UserRepository userRepository;

    public AuthWebController(AuthService authService, UserRepository userRepository) {
        this.authService = authService;
        this.userRepository = userRepository;
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("roles", Role.values());
        model.addAttribute("form", new RegisterForm(null, null, null, null, null, null));
        return "auth/register";
    }

    public record RegisterForm(String firstName, String lastName, String email, String password, String phone, Role role) {}

    @PostMapping("/register")
    public String registerSubmit(@ModelAttribute("form") @Valid RegisterForm form, Model model) {
        authService.register(new RegisterRequest(form.firstName(), form.lastName(), form.email(), form.password(), form.phone(), form.role()));
        model.addAttribute("message", "Inscription réussie ! Veuillez vérifier votre e-mail.");
        return "auth/success";
    }

    // Simple web login (email/password) showing token on success
    public record LoginForm(String email, String password) {}

    @GetMapping("/login")
    public String loginPage(Model model) {
        model.addAttribute("loginForm", new LoginForm(null, null));
        model.addAttribute("verifyForm", new VerifyForm(null));
        return "auth/login";
    }

    @PostMapping("/login")
    public String loginSubmit(@ModelAttribute("loginForm") @Valid LoginForm form, Model model) {
        var authResponse = authService.login(new LoginRequest(form.email(), form.password()));
        var user = userRepository.findByEmail(form.email()).orElseThrow();
        switch (user.getRole()) {
            case CANDIDAT:
                return "redirect:/dashboard/candidate";
            case RECRUTEUR:
                return "redirect:/dashboard/recruiter";
            case ADMIN:
                return "redirect:/dashboard/admin";
            default:
                model.addAttribute("token", authResponse.accessToken());
                model.addAttribute("message", "Connexion réussie.");
                return "auth/success";
        }
    }

    // Email verification via 8-digit code
    public record VerifyForm(String code) {}

    @GetMapping("/verify-email")
    public String verifyEmailPage(Model model) {
        model.addAttribute("verifyForm", new VerifyForm(null));
        return "auth/login";
    }

    @PostMapping("/verify-email")
    public String verifyEmailSubmit(@ModelAttribute("verifyForm") @Valid VerifyForm form, Model model) {
        authService.verifyEmail(new VerifyEmailRequest(form.code()));
        model.addAttribute("message", "Email vérifié avec succès. Vous pouvez maintenant vous connecter.");
        return "auth/success";
    }

    // Dashboards (server-rendered, public for demo due to stateless JWT)
    @GetMapping("/dashboard/candidate")
    public String dashboardCandidate(Model model) {
        model.addAttribute("title", "Dashboard Candidat");
        return "dashboard/candidate";
    }

    @GetMapping("/dashboard/recruiter")
    public String dashboardRecruiter(Model model) {
        model.addAttribute("title", "Dashboard Recruteur");
        return "dashboard/recruiter";
    }

    @GetMapping("/dashboard/admin")
    public String dashboardAdmin(Model model) {
        model.addAttribute("title", "Dashboard Admin");
        return "dashboard/admin";
    }

}
