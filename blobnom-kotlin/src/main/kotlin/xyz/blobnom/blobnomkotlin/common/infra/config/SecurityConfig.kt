package xyz.blobnom.blobnomkotlin.common.infra.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import xyz.blobnom.blobnomkotlin.auth.infra.JwtAuthenticationFilter
import xyz.blobnom.blobnomkotlin.auth.infra.JwtTokenProvider

@Configuration
class SecurityConfig {
    @Bean
    fun securityFilterChain(http: HttpSecurity, jwtTokenProvider: JwtTokenProvider): SecurityFilterChain {
        http {
            cors { configurationSource = corsConfigurationSource() }

            authorizeHttpRequests {
                authorize("/rooms/create", authenticated)
                authorize("/rooms/solved", authenticated)
                authorize("/rooms/join/*", authenticated)
                authorize("/rooms/leave/*", authenticated)
                authorize("/rooms/delete/*", authenticated)

                authorize(anyRequest, permitAll)
            }

            csrf { disable() }
            formLogin {}
            httpBasic {}
            addFilterBefore<UsernamePasswordAuthenticationFilter>(JwtAuthenticationFilter(jwtTokenProvider))
        }
        return http.build()
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration()
        configuration.allowedOrigins =
            listOf("http://localhost:5173", "http://localhost:3000", "https://blobnom.xyz", "https://dev.blobnom.xyz")
        configuration.allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
        configuration.allowedHeaders = listOf("*")
        configuration.allowCredentials = true

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun authenticationManager(authenticationConfiguration: AuthenticationConfiguration): AuthenticationManager =
        authenticationConfiguration.authenticationManager
}