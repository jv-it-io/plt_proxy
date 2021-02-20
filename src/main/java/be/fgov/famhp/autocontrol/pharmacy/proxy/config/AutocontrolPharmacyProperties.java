package be.fgov.famhp.autocontrol.pharmacy.proxy.config;

import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Properties specific to Autocontrol Pharmacy Proxy.
 * <p>
 * Properties are configured in the {@code application.yml} file.
 * See {@link io.github.jhipster.config.JHipsterProperties} for a good example.
 */
@ConfigurationProperties(prefix = "autocontrol", ignoreUnknownFields = false)
public class AutocontrolPharmacyProperties {

    MongoProperties platoBackoffice = new MongoProperties();

    public MongoProperties getPlatoBackoffice() {
        return platoBackoffice;
    }

    public void setPlatoBackoffice(MongoProperties platoBackoffice) {
        this.platoBackoffice = platoBackoffice;
    }

    private final Batch batch = new Batch();

    public Batch getBatch() {
        return batch;
    }



    public static class Batch {

        private boolean enabled;


        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

    }

    private final CertificateSecurity security = new CertificateSecurity();

    public CertificateSecurity getSecurity() {
        return security;
    }

    public static class CertificateSecurity{

        private String keystoreFile;
        private String keystorePassword;
        private String keyAlias;
        private String keyPassword;

        public String getKeystoreFile() {
            return keystoreFile;
        }

        public void setKeystoreFile(String keystoreFile) {
            this.keystoreFile = keystoreFile;
        }

        public String getKeystorePassword() {
            return keystorePassword;
        }

        public void setKeystorePassword(String keystorePassword) {
            this.keystorePassword = keystorePassword;
        }

        public String getKeyAlias() {
            return keyAlias;
        }

        public void setKeyAlias(String keyAlias) {
            this.keyAlias = keyAlias;
        }

        public String getKeyPassword() {
            return keyPassword;
        }

        public void setKeyPassword(String keyPassword) {
            this.keyPassword = keyPassword;
        }
    }
}
