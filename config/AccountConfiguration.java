package account.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
public class AccountConfiguration  {


    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().requestMatchers("/favicon.ico");
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .httpBasic(s -> s.authenticationEntryPoint((request, response, authException) -> {
                        response.sendError(401, authException.getMessage());
                }))
                .anonymous().disable()
                 .exceptionHandling().accessDeniedHandler((request, response, accessDeniedException) -> response.sendError(403, "Access Denied!"));


        http

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.PUT, "/api/admin/user/role/**").hasAnyRole("ADMINISTRATOR")
                        .requestMatchers(HttpMethod.PUT, "/api/admin/user/access/**").hasAnyRole("ADMINISTRATOR")
                        .requestMatchers(HttpMethod.DELETE, "/api/admin/user/**").hasAnyRole("ADMINISTRATOR")
                        .requestMatchers(HttpMethod.GET, "/api/admin/user/**").hasAnyRole("ADMINISTRATOR")
                        .requestMatchers(HttpMethod.PUT, "/api/acct/payments/**").hasAnyRole("ACCOUNTANT")
                        .requestMatchers(HttpMethod.POST, "/api/acct/payments/**").hasAnyRole("ACCOUNTANT")
                        .requestMatchers(HttpMethod.GET, "/api/empl/payment/**").hasAnyRole("USER", "ACCOUNTANT")
                        .requestMatchers(HttpMethod.GET, "/api/security/events/**").hasAnyRole("AUDITOR")
                        .requestMatchers(HttpMethod.POST, "/api/auth/changepass/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/auth/signup/**").permitAll()

                        .requestMatchers(HttpMethod.POST, "/actuator/shutdown").permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/h2-console/**")).permitAll()
                        .requestMatchers("/error/**").permitAll()
                        .anyRequest().denyAll()
                )
                .csrf(AbstractHttpConfigurer::disable)
                .headers(headers -> headers.frameOptions().disable())

        ;

//                .sessionManagement(session -> session
//                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordAuthentication() {
        return new BCryptPasswordEncoder(13);
    }
}
