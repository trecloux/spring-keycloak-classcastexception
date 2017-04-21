package com.axoninsight;

import org.keycloak.adapters.KeycloakConfigResolver;
import org.keycloak.adapters.springboot.KeycloakSpringBootConfigResolver;
import org.keycloak.adapters.springboot.KeycloakSpringBootConfiguration;
import org.keycloak.adapters.springboot.KeycloakSpringBootProperties;
import org.keycloak.adapters.springsecurity.KeycloakSecurityComponents;
import org.keycloak.adapters.springsecurity.config.KeycloakWebSecurityConfigurerAdapter;
import org.keycloak.adapters.springsecurity.filter.KeycloakAuthenticatedActionsFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.authentication.session.NullAuthenticatedSessionStrategy;
import org.springframework.security.web.authentication.session.RegisterSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionFixationProtectionStrategy;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

@SpringBootApplication(exclude = KeycloakSpringBootConfiguration.class)
public class SampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(SampleApplication.class, args);
    }


    @EnableWebSecurity
    @Configuration
    @ComponentScan(basePackageClasses = KeycloakSecurityComponents.class)
    @EnableConfigurationProperties(KeycloakSpringBootProperties.class)
    public class SecurityConfig extends KeycloakWebSecurityConfigurerAdapter {

        @Autowired
        public void configureGlobal(AuthenticationManagerBuilder authenticationManagerBuilder) {
            authenticationManagerBuilder.authenticationProvider(keycloakAuthenticationProvider());
        }

        @Override
        protected SessionAuthenticationStrategy sessionAuthenticationStrategy() {
            return new SessionFixationProtectionStrategy();
        }

        @Bean
        public KeycloakAuthenticatedActionsFilter getKeycloakAuthenticatedActionsFilter() {
            return new KeycloakAuthenticatedActionsFilter();
        }

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http
                .addFilterBefore(keycloakPreAuthActionsFilter(), LogoutFilter.class)
                .addFilterBefore(keycloakAuthenticationProcessingFilter(), BasicAuthenticationFilter.class)
                .addFilterBefore(getKeycloakAuthenticatedActionsFilter(), BasicAuthenticationFilter.class)
                .sessionManagement()
                    .sessionAuthenticationStrategy(sessionAuthenticationStrategy())
                .and()
                .exceptionHandling().authenticationEntryPoint(authenticationEntryPoint())
                .and()
                .authorizeRequests()
                        .anyRequest().authenticated()
            ;
        }

        @Bean
        public KeycloakConfigResolver configResolver(KeycloakSpringBootProperties keycloakProperties) {
            // Little hack to trigger initialization of KeycloakSpringBootConfigResolver.adapterConfig
            new KeycloakSpringBootConfiguration().setKeycloakSpringBootProperties(keycloakProperties);
            return new KeycloakSpringBootConfigResolver();
        }
    }
}
