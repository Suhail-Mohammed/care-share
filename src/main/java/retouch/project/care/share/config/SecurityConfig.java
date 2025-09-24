package retouch.project.care.share.config;


import retouch.project.care.share.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.core.userdetails.UserDetailsService;

@Configuration
public class SecurityConfig {

    private final UserRepository userRepository;

    public SecurityConfig(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> userRepository.findByEmail(username)
                .map(user -> org.springframework.security.core.userdetails.User
                        .withUsername(user.getEmail())
                        .password(user.getPassword())
                        .roles(user.getRole())
                        .build())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService());
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    // ✅ The important part
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable()) // Disable CSRF for Postman testing
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**", "/register.html", "/login.html","/forgot-password.html").permitAll() // allow register & login
                        .anyRequest().authenticated()               // protect other APIs
                )
                .formLogin(form -> form.disable())  // we don’t want Spring’s default login page
                .httpBasic(httpBasic -> httpBasic.disable()); // disable basic auth popup

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
