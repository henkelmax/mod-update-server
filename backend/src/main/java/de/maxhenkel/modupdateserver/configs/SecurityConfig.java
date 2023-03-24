package de.maxhenkel.modupdateserver.configs;

import de.maxhenkel.modupdateserver.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    public static final String WEB_UI_ROLE = "WEB_UI";
    public static final String ANONYMOUS_ROLE = "ANONYMOUS";

    @Bean
    public SecurityFilterChain configure(HttpSecurity http) throws Exception {
        http.csrf().disable()
                .anonymous()
                .authorities(ANONYMOUS_ROLE)
                .and()
                .authorizeHttpRequests(
                        (requests) -> requests
                                .requestMatchers(HttpMethod.GET, "/index.html", "/")
                                .hasRole(WEB_UI_ROLE)
                                .requestMatchers("/**")
                                .permitAll().anyRequest()
                                .hasRole(ANONYMOUS_ROLE))
                .httpBasic()
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        return http.build();
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth
                .inMemoryAuthentication()
                .withUser(Utils.getEnv("LOGIN_USERNAME", "admin"))
                .password("{noop}%s".formatted(Utils.getEnv("LOGIN_PASSWORD", "admin")))
                .roles(WEB_UI_ROLE);
    }


}
