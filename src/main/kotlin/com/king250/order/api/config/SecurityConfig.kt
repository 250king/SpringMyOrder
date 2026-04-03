package com.king250.order.api.config

import com.nimbusds.jose.JOSEObjectType
import com.nimbusds.jose.proc.DefaultJOSEObjectTypeVerifier
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.access.expression.DefaultHttpSecurityExpressionHandler
import org.springframework.security.web.access.expression.WebExpressionAuthorizationManager

@Configuration
class SecurityConfig(
    private val context: ApplicationContext,
    private val authProperties: OAuth2ResourceServerProperties
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
                    .requestMatchers("/_/background/**").permitAll()
                    .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                    .requestMatchers("/admin/**").access(expression)
                    .anyRequest().authenticated()
            }
            .oauth2ResourceServer { oauth2 ->
                oauth2.jwt { }
            }
            .csrf {
                it.disable()
            }
        return http.build()
    }

    @Bean
    fun jwtDecoder(): JwtDecoder {
        val nimbusJwtDecoder = NimbusJwtDecoder
            .withIssuerLocation(authProperties.jwt.issuerUri)
            .jwtProcessorCustomizer { processor ->
                processor.jwsTypeVerifier = DefaultJOSEObjectTypeVerifier(
                    JOSEObjectType("at+jwt"),
                    JOSEObjectType.JWT,
                )
            }
            .build()
        return nimbusJwtDecoder
    }
}