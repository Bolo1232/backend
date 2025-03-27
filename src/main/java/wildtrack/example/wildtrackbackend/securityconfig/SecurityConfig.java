package wildtrack.example.wildtrackbackend.securityconfig;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf().disable() // Disable CSRF since you're using JWT
                .cors().and() // Enable CORS
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/files/images/**",
                                "/api/users/register",
                                "/api/**",
                                "/api/login",
                                "/api/verify-token",
                                "/api/users/uploads/**",
                                "/api/library-hours/**",
                                "/api/books/**",
                                "/api/users/**",
                                "/api/students/**",
                                "/api/teachers/**",
                                "/api/nas-students/**",
                                "/api/booklog/**")
                        .permitAll() // Allow unauthenticated access to these endpoints
                        .anyRequest().authenticated() // All other requests require authentication
                )
                .httpBasic(); // Keep Basic Authentication for now

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Add ALL your possible Vercel domains
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:5173",
                "http://localhost:5174",
                "https://wild-track.vercel.app",
                "https://wild-track-ejhubs-projects.vercel.app",
                "https://wild-track-mi1773661-ejhubs-projects.vercel.app" // Add this new domain

        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Accept", "X-Requested-With"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // Use BCrypt for password encoding
    }
}
