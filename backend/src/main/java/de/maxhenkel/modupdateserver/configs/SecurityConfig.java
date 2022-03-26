package de.maxhenkel.modupdateserver.configs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    public static final String WEB_UI_ROLE = "WEB_UI";
    public static final String ANONYMOUS_ROLE = "ANONYMOUS";

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .anonymous()
                .authorities(ANONYMOUS_ROLE)
                .and()
                .authorizeRequests()

                .antMatchers(HttpMethod.GET, "/index.html", "/")
                .hasRole(WEB_UI_ROLE)

                .antMatchers("/**")
                .permitAll().anyRequest()
                .hasRole(ANONYMOUS_ROLE)

                .and()
                .httpBasic()
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth
                .inMemoryAuthentication()
                .withUser(getEnv("LOGIN_USERNAME", "admin"))
                .password("{noop}%s".formatted(getEnv("LOGIN_PASSWORD", "admin")))
                .roles(WEB_UI_ROLE);
    }

    private String getEnv(String env, String def) {
        String var = System.getenv(env);
        return var == null ? def : var;
    }

}
