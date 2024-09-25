package com.example.application.security;

import com.example.application.views.login.LoginView;
import com.vaadin.flow.spring.security.VaadinWebSecurity;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

@EnableWebSecurity
@EnableMethodSecurity(jsr250Enabled = true)
@Configuration
public class SecurityConfig extends VaadinWebSecurity {
  @Override
  protected void configure(HttpSecurity http) throws Exception {
    super.configure(http);
    setLoginView(http, LoginView.class);
  }

  @Bean
  public UserDetailsService users() {
    final UserDetails alice =
        User.builder()
            .username("alice")
            // password = password with this hash, don't tell anybody :-)
            .password("{bcrypt}$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmNSJZ.0FxO/BTk76klW")
            .roles(Roles.USER)
            .build();
    final UserDetails bob =
        User.builder()
            .username("bob")
            // password = password with this hash, don't tell anybody :-)
            .password("{bcrypt}$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmNSJZ.0FxO/BTk76klW")
            .roles(Roles.USER)
            .build();
    final UserDetails admin =
        User.builder()
            .username("admin")
            // password = password with this hash, don't tell anybody :-)
            .password("{bcrypt}$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmNSJZ.0FxO/BTk76klW")
            .roles(Roles.ADMIN, Roles.USER)
            .build();
    return new InMemoryUserDetailsManager(alice, bob, admin);
  }
}
