package retouch.project.care.share.service;



import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${app.base-url}")
    private String baseUrl;

    public void sendPasswordResetEmail(String toEmail, String token) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(toEmail);
            helper.setSubject("Password Reset - Care & Share");

            String resetUrl = baseUrl + "/reset-password.html?token=" + token;

            String content = "<html><body>" +
                    "<h2>Password Reset Request</h2>" +
                    "<p>You requested to reset your password. Click the link below:</p>" +
                    "<p><a href=\"" + resetUrl + "\">Reset Password</a></p>" +
                    "<p>This link will expire in 30 minutes.</p>" +
                    "<p>If you did not request this, please ignore this email.</p>" +
                    "<br><p>Care & Share Team</p>" +
                    "</body></html>";

            helper.setText(content, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email", e);
        }
    }
}

