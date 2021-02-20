package be.fgov.famhp.autocontrol.pharmacy.proxy.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.RsaSigner;
import org.springframework.security.jwt.crypto.sign.Signer;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.client.token.auth.ClientAuthenticationHandler;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory;
import org.springframework.util.Assert;
import org.springframework.util.MultiValueMap;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.interfaces.RSAPrivateKey;
import java.util.UUID;

public class JwtClientAuthenticationHandler implements ClientAuthenticationHandler, InitializingBean {

    public static final String CLIENT_ASSERTION_TYPE = "client_assertion_type";
    public static final String CLIENT_ASSERTION = "client_assertion";
    public static final String CLIENT_ASSERTION_TYPE_JWT = "urn:ietf:params:oauth:client-assertion-type:jwt-bearer";

    public static final int DEFAULT_EXPIRATION = 60;
    public static final int DEFAULT_TOLERANCE = 30;

    private Resource keystore;
    private String keystorePassword;
    private String keyAlias;
    private String keyPassword;
    private Signer signer;
    private int expiration = DEFAULT_EXPIRATION;
    private int nbfTolerance = DEFAULT_TOLERANCE;

    private ObjectMapper objectMapper = new ObjectMapper();

    public JwtClientAuthenticationHandler(Resource keystore) {
        this.keystore = keystore;
    }

    @Override
    public void authenticateTokenRequest(OAuth2ProtectedResourceDetails resource, MultiValueMap<String, String> form, HttpHeaders headers) {
        if (resource.isAuthenticationRequired()) {
            int currentTimeSeconds = (int) (System.currentTimeMillis() / 1000);
            JwtGrant grant = newGrant(resource);
            Jwt jwt = null;
            try {
                jwt = JwtHelper.encode(objectMapper.writeValueAsString(grant), signer);
            } catch (JsonProcessingException e) {
                throw new IllegalStateException(e);
            }
            form.set(CLIENT_ASSERTION_TYPE, CLIENT_ASSERTION_TYPE_JWT);
            form.set(CLIENT_ASSERTION, jwt.getEncoded());
        }
    }

    private JwtGrant newGrant(OAuth2ProtectedResourceDetails resource) {
        int currentTimeSeconds = (int) (System.currentTimeMillis() / 1000);
        JwtGrant jwtGrant = new JwtGrant();
        jwtGrant.setIssuer(resource.getClientId());
        jwtGrant.setSubject(resource.getClientId());
        jwtGrant.setAudience(resource.getAccessTokenUri());
        jwtGrant.setExpires(currentTimeSeconds + expiration);
        jwtGrant.setNotBefore(currentTimeSeconds - nbfTolerance);
        jwtGrant.setIssuedAt(currentTimeSeconds);
        jwtGrant.setJwtId(UUID.randomUUID().toString());
        return jwtGrant;
    }

    public void setExpiration(int expiration) {
        this.expiration = expiration;
    }

    public void setNbfTolerance(int nbfTolerance) {
        this.nbfTolerance = nbfTolerance;
    }

    public void setKeystore(Resource keystore) {
        this.keystore = keystore;
    }

    public void setKeystorePassword(String keystorePassword) {
        this.keystorePassword = keystorePassword;
    }

    public void setKeyAlias(String keyAlias) {
        this.keyAlias = keyAlias;
    }

    public void setKeyPassword(String keyPassword) {
        this.keyPassword = keyPassword;
    }

    public void afterPropertiesSet() {
        if (keystore == null) {
            throw new IllegalArgumentException("keystore property is required");
        }
        JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
        KeyStoreKeyFactory keyStoreKeyFactory =
            new KeyStoreKeyFactory(keystore, keystorePassword != null ? keystorePassword.toCharArray() : null);
        if (keyAlias != null) {
            KeyPair keyPair = keyStoreKeyFactory.getKeyPair(keyAlias, keyPassword != null ? keyPassword.toCharArray() : null);
            PrivateKey privateKey = keyPair.getPrivate();
            converter.setKeyPair(keyPair);
            Assert.state(privateKey instanceof RSAPrivateKey, "KeyPair must be an RSA ");
            signer = new RsaSigner((RSAPrivateKey) privateKey);
        }
    }

    private static class JwtGrant {

        @JsonProperty("iss")
        private String issuer;

        @JsonProperty("sub")
        private String subject;

        @JsonProperty("aud")
        private String audience;

        @JsonProperty("exp")
        private int expires;

        @JsonProperty("nbf")
        private int notBefore;

        @JsonProperty("iat")
        private int issuedAt;

        @JsonProperty("jti")
        private String jwtId;

        public String getIssuer() {
            return issuer;
        }

        public void setIssuer(String issuer) {
            this.issuer = issuer;
        }

        public String getSubject() {
            return subject;
        }

        public void setSubject(String subject) {
            this.subject = subject;
        }

        public String getAudience() {
            return audience;
        }

        public void setAudience(String audience) {
            this.audience = audience;
        }

        public int getExpires() {
            return expires;
        }

        public void setExpires(int expires) {
            this.expires = expires;
        }

        public int getNotBefore() {
            return notBefore;
        }

        public void setNotBefore(int notBefore) {
            this.notBefore = notBefore;
        }

        public int getIssuedAt() {
            return issuedAt;
        }

        public void setIssuedAt(int issuedAt) {
            this.issuedAt = issuedAt;
        }

        public String getJwtId() {
            return jwtId;
        }

        public void setJwtId(String jwtId) {
            this.jwtId = jwtId;
        }
    }
}
