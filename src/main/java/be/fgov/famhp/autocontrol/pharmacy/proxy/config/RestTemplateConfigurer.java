package be.fgov.famhp.autocontrol.pharmacy.proxy.config;

import be.fgov.famhp.autocontrol.pharmacy.proxy.security.interceptors.RequestResponseLoggingInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsAccessTokenProvider;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

@Configuration
@ConditionalOnProperty(prefix = "security.oauth2.client", value = "grant-type", havingValue = "client_credentials")
public class RestTemplateConfigurer {

    private static final Logger log = LoggerFactory.getLogger(RestTemplateConfigurer.class);

    @Autowired
    private DefaultResourceLoader resourceLoader;

    @Autowired
    AutocontrolPharmacyProperties autocontrolPharmacyProperties;

    @Bean
    public OAuth2RestTemplate oauth2RestTemplate(OAuth2ProtectedResourceDetails resourceDetails) {

        OAuth2RestTemplate oAuth2RestTemplate = new OAuth2RestTemplate(resourceDetails);

        // using BufferingClientHttpRequestFactory allows for multiple reads of the response body (buffers streams in memory)
        // using HttpComponentsClientHttpRequestFactory replace default http client by apache one (more advanced options: PATCH, http config ...)
        ClientHttpRequestFactory factory = new BufferingClientHttpRequestFactory(new HttpComponentsClientHttpRequestFactory());
        oAuth2RestTemplate.setRequestFactory(factory);
        List<ClientHttpRequestInterceptor> interceptors = oAuth2RestTemplate.getInterceptors();
        if (CollectionUtils.isEmpty(interceptors)) {
            interceptors = new ArrayList<>();
        }
        interceptors.add(new RequestResponseLoggingInterceptor());
        oAuth2RestTemplate.setInterceptors(interceptors);

        ClientCredentialsAccessTokenProvider accessTokenProvider = new ClientCredentialsAccessTokenProvider();
        accessTokenProvider.setAuthenticationHandler(jwtClientAuthenticationHandler());
        oAuth2RestTemplate.setAccessTokenProvider(accessTokenProvider);
        oAuth2RestTemplate.setRetryBadAccessTokens(false);

        return oAuth2RestTemplate;

    }

    private JwtClientAuthenticationHandler jwtClientAuthenticationHandler() {
        Resource keyStore = resourceLoader.getResource(autocontrolPharmacyProperties.getSecurity().getKeystoreFile());
        JwtClientAuthenticationHandler authenticationHandler = new JwtClientAuthenticationHandler(keyStore);
        authenticationHandler.setKeystorePassword(autocontrolPharmacyProperties.getSecurity().getKeystorePassword());
        authenticationHandler.setKeyAlias(autocontrolPharmacyProperties.getSecurity().getKeyAlias());
        authenticationHandler.setKeyPassword(autocontrolPharmacyProperties.getSecurity().getKeyPassword());
        authenticationHandler.afterPropertiesSet();
        return authenticationHandler;
    }
}
