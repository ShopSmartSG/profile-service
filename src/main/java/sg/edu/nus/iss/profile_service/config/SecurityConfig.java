package sg.edu.nus.iss.profile_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@Profile("!test")  // Not active during tests
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .headers(headers -> headers
                .xssProtection(xss -> xss.disable())  // Modern browsers have built-in XSS protection
                .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'"))
                .frameOptions(frame -> frame.deny())
                .httpStrictTransportSecurity(hsts -> hsts.includeSubDomains(true))
            )
            .csrf(csrf -> csrf.disable())  // Since behind reverse proxy
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());  // Allow all requests since auth is handled by proxy
            
        return http.build();
    }
} 