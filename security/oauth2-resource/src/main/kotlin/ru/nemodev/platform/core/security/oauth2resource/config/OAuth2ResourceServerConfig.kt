package ru.nemodev.platform.core.security.oauth2resource.config

import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.core.convert.converter.Converter
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter
import org.springframework.security.web.SecurityFilterChain
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import ru.nemodev.platform.core.extensions.toRSAPublicKey

@AutoConfiguration
@EnableConfigurationProperties(SecurityOAuth2ResourceProperties::class)
@ConditionalOnProperty(prefix = "platform.security.oauth2-resource", name = ["enabled"], havingValue = "true", matchIfMissing = true)
@EnableWebSecurity
class OAuth2ResourceServerConfig(
    private val properties: SecurityOAuth2ResourceProperties
) {

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    fun securityFilterChain(httpSecurity: HttpSecurity): SecurityFilterChain {
        return httpSecurity
            .csrf { it.disable() }
            .cors {
                it.configurationSource { configurationSource ->
                    UrlBasedCorsConfigurationSource().apply {
                        registerCorsConfiguration(
                            "/**",
                            CorsConfiguration().apply {
                                allowedOrigins = listOf("*")
                                allowedHeaders = listOf("*")
                                allowedMethods = listOf("*")
                                allowCredentials = false
                            }
                        )
                    }.getCorsConfiguration(configurationSource)
                }
            }
            .authorizeHttpRequests {
                properties.authPaths.forEach { authPath ->
                    addPathMatchers(it, authPath)
                }
                it.anyRequest().permitAll()
            }
            .oauth2ResourceServer {
                it.jwt(Customizer.withDefaults())
            }
            .build()
    }

    private fun addPathMatchers(
        authorize: AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry,
        authPath: SecurityOAuth2ResourceProperties.AuthPath
    ) {
        if (!authPath.roles.isNullOrEmpty()) {
            if (!authPath.methods.isNullOrEmpty()) {
                authPath.methods.forEach { httpMethod ->
                    authorize.requestMatchers(httpMethod, authPath.path).hasAnyRole(*authPath.roles.toTypedArray())
                }
            } else {
                authorize.requestMatchers(authPath.path).hasAnyRole(*authPath.roles.toTypedArray())
            }
        } else {
            if (!authPath.methods.isNullOrEmpty()) {
                authPath.methods.forEach { httpMethod ->
                    authorize.requestMatchers(httpMethod, authPath.path).authenticated()
                }
            } else {
                authorize.requestMatchers(authPath.path).authenticated()
            }
        }
    }

    @Bean
    fun jwtDecoder(): JwtDecoder {
        return NimbusJwtDecoder.withPublicKey(properties.rsaKey.publicKey.toRSAPublicKey()).build()
    }

    @Bean
    fun jwtGrantedAuthoritiesConverter(): Converter<Jwt, Collection<GrantedAuthority>> {
        return JwtGrantedAuthoritiesConverter().apply {
            setAuthorityPrefix("ROLE_")
            setAuthoritiesClaimDelimiter(" ")
        }
    }

    @Bean
    fun jwtAuthenticationConverter(
        jwtGrantedAuthoritiesConverter: Converter<Jwt, Collection<GrantedAuthority>>
    ): JwtAuthenticationConverter {
        return JwtAuthenticationConverter().apply {
            setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter)
        }
    }
}
