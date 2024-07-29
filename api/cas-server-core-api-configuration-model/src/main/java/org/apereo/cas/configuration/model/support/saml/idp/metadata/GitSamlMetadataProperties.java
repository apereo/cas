package org.apereo.cas.configuration.model.support.saml.idp.metadata;

import org.apereo.cas.configuration.model.core.util.EncryptionJwtCryptoProperties;
import org.apereo.cas.configuration.model.core.util.EncryptionJwtSigningJwtCryptographyProperties;
import org.apereo.cas.configuration.model.core.util.SigningJwtCryptoProperties;
import org.apereo.cas.configuration.model.support.git.services.BaseGitProperties;
import org.apereo.cas.configuration.model.support.quartz.SchedulingProperties;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.io.FileUtils;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.core.io.FileSystemResource;
import java.io.File;
import java.io.Serial;

/**
 * Configuration properties class for git metadata management.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@RequiresModule(name = "cas-server-support-saml-idp-metadata-git")
@Getter
@Setter
@Accessors(chain = true)

public class GitSamlMetadataProperties extends BaseGitProperties {
    @Serial
    private static final long serialVersionUID = 4194689836396653458L;

    /**
     * Whether identity provider metadata artifacts
     * are expected to be found in the database.
     */
    private boolean idpMetadataEnabled;

    /**
     * Crypto settings that sign/encrypt the metadata records.
     */
    @NestedConfigurationProperty
    private EncryptionJwtSigningJwtCryptographyProperties crypto = new EncryptionJwtSigningJwtCryptographyProperties();

    /**
     * Scheduler settings to indicate how often the git repository is instructed to pull.
     */
    @NestedConfigurationProperty
    private SchedulingProperties schedule = new SchedulingProperties();

    public GitSamlMetadataProperties() {
        getCloneDirectory().setLocation(new FileSystemResource(new File(FileUtils.getTempDirectory(), "cas-saml-metadata")));
        crypto.getEncryption().setKeySize(EncryptionJwtCryptoProperties.DEFAULT_STRINGABLE_ENCRYPTION_KEY_SIZE);
        crypto.getSigning().setKeySize(SigningJwtCryptoProperties.DEFAULT_STRINGABLE_SIGNING_KEY_SIZE);
        schedule.setEnabled(false);
    }
}
