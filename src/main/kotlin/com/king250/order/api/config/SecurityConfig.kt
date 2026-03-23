package com.king250.order.api.config

import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.access.expression.DefaultHttpSecurityExpressionHandler
import org.springframework.security.web.access.expression.WebExpressionAuthorizationManager

@Configuration
class SecurityConfig(
    private val context: ApplicationContext
) {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        val handler = DefaultHttpSecurityExpressionHandler().apply {
            setApplicationContext(context)
        }
        val expression= WebExpressionAuthorizationManager("@auth.isAdmin()").apply {
            setExpressionHandler(handler)
        }
        http
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers("/error/**").permitAll()
                    .requestMatchers("/webhook/**").permitAll()
                    .requestMatchers("/admin/**").access(expression)
                    .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                    .anyRequest().authenticated()
            }
            .oauth2ResourceServer { oauth2 ->
                oauth2.jwt { }
            }
        return http.build()
    }
}