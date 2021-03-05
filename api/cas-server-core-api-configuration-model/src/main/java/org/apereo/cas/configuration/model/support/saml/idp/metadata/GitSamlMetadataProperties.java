package org.apereo.cas.configuration.model.support.saml.idp.metadata;

import org.apereo.cas.configuration.model.core.util.EncryptionJwtSigningJwtCryptographyProperties;
import org.apereo.cas.configuration.model.support.git.services.BaseGitProperties;
import org.apereo.cas.configuration.support.RequiresModule;
import org.apereo.cas.util.crypto.CipherExecutor;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.io.FileUtils;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.core.io.FileSystemResource;

import java.io.File;

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
@JsonFilter("GitSamlMetadataProperties")
public class GitSamlMetadataProperties extends BaseGitProperties {
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

    public GitSamlMetadataProperties() {
        getCloneDirectory().setLocation(new FileSystemResource(new File(FileUtils.getTempDirectory(), "cas-saml-metadata")));
        crypto.getEncryption().setKeySize(CipherExecutor.DEFAULT_STRINGABLE_ENCRYPTION_KEY_SIZE);
        crypto.getSigning().setKeySize(CipherExecutor.DEFAULT_STRINGABLE_SIGNING_KEY_SIZE);
    }
}
