package be.fgov.famhp.autocontrol.pharmacy.proxy.config;

import be.fgov.fagg.common.config.FaggSecurityAutoConfiguration;
import be.fgov.fagg.common.security.FaggAuthoritiesConstants;
import be.fgov.famhp.autocontrol.pharmacy.proxy.security.oauth2.AudienceValidator;
import be.fgov.famhp.autocontrol.pharmacy.proxy.security.oauth2.JwtGrantedAuthorityConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.zalando.problem.spring.web.advice.security.SecurityProblemSupport;

@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
@Import({SecurityProblemSupport.class, FaggSecurityAutoConfiguration.class})
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {
    @Value("${spring.security.oauth2.client.provider.oidc.issuer-uri}")
    private String issuerUri;

    private final SecurityProblemSupport problemSupport;
    private final JwtAuthenticationConverter jwtAuthenticationConverter;
    private final BearerTokenResolver bearerTokenResolver;

    public SecurityConfiguration(SecurityProblemSupport problemSupport, JwtAuthenticationConverter jwtAuthenticationConverter, BearerTokenResolver bearerTokenResolver) {
        this.problemSupport = problemSupport;
        this.jwtAuthenticationConverter = jwtAuthenticationConverter;
        this.bearerTokenResolver = bearerTokenResolver;
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        // @formatter:off
        http
            .csrf()
            .disable()
            .exceptionHandling()
                .authenticationEntryPoint(problemSupport)
                .accessDeniedHandler(problemSupport)
        .and()
            .headers()
            .contentSecurityPolicy("default-src 'self'; frame-src 'self' data:; script-src 'self' 'unsafe-inline' 'unsafe-eval' https://storage.googleapis.com; style-src 'self' 'unsafe-inline'; img-src 'self' data:; font-src 'self' data:")
        .and()
            .referrerPolicy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)
        .and()
            .featurePolicy("geolocation 'none'; midi 'none'; sync-xhr 'none'; microphone 'none'; camera 'none'; magnetometer 'none'; gyroscope 'none'; speaker 'none'; fullscreen 'self'; payment 'none'")
        .and()
            .frameOptions()
            .deny()
        .and()
            .sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        .and()
            .authorizeRequests()
            .antMatchers("/api/auth-info").permitAll()
            .antMatchers("/api/**").permitAll()
            .antMatchers("/management/health").permitAll()
            .antMatchers("/management/info").permitAll()
            .antMatchers("/management/prometheus").permitAll()
            .antMatchers("/management/mongodata").permitAll()
            .antMatchers("/management/security").permitAll()
            .antMatchers("/management/security-check").authenticated()
            .antMatchers("/management/**").hasAuthority(FaggAuthoritiesConstants.REGISTRY_ADMIN)
        .and()
            .oauth2ResourceServer()
            .bearerTokenResolver(bearerTokenResolver)
                .jwt()
                .jwtAuthenticationConverter(jwtAuthenticationConverter)
                .and()
            .and()
                .oauth2Client();
        // @formatter:on
    }

    Converter<Jwt, AbstractAuthenticationToken> authenticationConverter() {
        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(new JwtGrantedAuthorityConverter());
        return jwtAuthenticationConverter;
    }


}
