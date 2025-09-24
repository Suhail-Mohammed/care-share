package retouch.project.care.share.service;


import retouch.project.care.share.dto.RegisterRequest;
import retouch.project.care.share.entitiy.User;
import retouch.project.care.share.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User register(RegisterRequest request) {
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        return userRepository.save(user);
    }


    public String initiatePasswordReset(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String token = UUID.randomUUID().toString();
        user.setResetToken(token);
        user.setResetTokenExpiry(LocalDateTime.now().plusMinutes(30)); // valid 30 mins
        userRepository.save(user);

        // TODO: send token by email (e.g. via JavaMailSender)
        // Example: http://localhost:8080/reset-password.html?token=" + token

        return "Password reset link has been sent to your email.";
    }

    public String resetPassword(String token, String newPassword) {
        User user = userRepository.findAll().stream()
                .filter(u -> token.equals(u.getResetToken()) &&
                        u.getResetTokenExpiry().isAfter(LocalDateTime.now()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Invalid or expired token"));

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        userRepository.save(user);

        return "Password has been reset successfully.";
    }
}

