package retouch.project.care.share.service;

import retouch.project.care.share.dto.RegisterRequest;
import retouch.project.care.share.entitiy.User;
import retouch.project.care.share.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }



    public User register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("User already exists with this email");
        }
        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        return userRepository.save(user);
    }


    public String initiatePasswordReset(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            System.out.println("Password reset requested for non-existent email: " + email);
            return "If an account with that email exists, a password reset link has been sent.";
        }

        User user = userOpt.get();
        String token = UUID.randomUUID().toString();
        user.setResetToken(token);
        user.setResetTokenExpiry(LocalDateTime.now().plusMinutes(30));
        userRepository.save(user);

        // Send email with reset link
        emailService.sendPasswordResetEmail(user.getEmail(), token);

        return "If an account with that email exists, a password reset link has been sent.";
    }

    public String resetPassword(String token, String newPassword) {
        if (newPassword == null || newPassword.length() < 6) {
            throw new RuntimeException("Password must be at least 6 characters long");
        }

        Optional<User> userOpt = userRepository.findAll().stream()
                .filter(u -> token.equals(u.getResetToken())
                        && u.getResetTokenExpiry() != null
                        && u.getResetTokenExpiry().isAfter(LocalDateTime.now()))
                .findFirst();

        if (userOpt.isEmpty()) {
            throw new RuntimeException("Invalid or expired reset token");
        }

        User user = userOpt.get();
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        userRepository.save(user);

        return "Password has been reset successfully.";
    }

    // Utility method for efficiency (optional)
    public Optional<User> findByResetToken(String token) {
        return userRepository.findAll().stream()
                .filter(u -> token.equals(u.getResetToken())
                        && u.getResetTokenExpiry() != null
                        && u.getResetTokenExpiry().isAfter(LocalDateTime.now()))
                .findFirst();
    }
}
