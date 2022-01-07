package org.apereo.cas.configuration.model.support.oidc.jwks;

import org.apereo.cas.configuration.support.ExpressionLanguageCapable;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * This is {@link FileSystemOidcJsonWebKeystoreProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@RequiresModule(name = "cas-server-support-oidc")
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("FileSystemOidcJsonWebKeystoreProperties")
public class FileSystemOidcJsonWebKeystoreProperties implements Serializable {
    private static final long serialVersionUID = 1659099897056632658L;

    /**
     * Path to the JWKS file resource used to handle signing/encryption of authentication tokens.
     */
    @RequiredProperty
    @ExpressionLanguageCapable
    private String jwksFile = "file:/etc/cas/config/keystore.jwks";

    /**
     * Flag indicating whether a background watcher thread is enabled
     * for the purposes of live reloading of keystore data file changes
     * from disk.
     */
    private boolean watcherEnabled = true;
}
