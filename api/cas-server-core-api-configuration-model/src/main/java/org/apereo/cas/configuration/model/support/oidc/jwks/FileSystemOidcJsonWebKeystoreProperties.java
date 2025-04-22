package org.apereo.cas.configuration.model.support.oidc.jwks;

import org.apereo.cas.configuration.support.ExpressionLanguageCapable;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serial;
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
public class FileSystemOidcJsonWebKeystoreProperties implements Serializable {
    @Serial
    private static final long serialVersionUID = 1659099897056632658L;

    /**
     * Path to the JWKS file resource used to handle signing/encryption of authentication tokens.
     * Contents of the keystore may be encrypted using the same encryption and security mechanism available
     * for all other CAS configuration settings.
     * The setting value here may also be defined in a raw format; that is, you may pass the actual contents of the keystore
     * verbatim to this setting and CAS would load the keystore as an in-memory resource. This is relevant in scenarios where
     * the setting source is external to CAS and has no support for file systems where the value is loaded on the fly from the
     * source into this setting.
     * Note that if the keystore files does not exist at the specified path, one will be generated for you.
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
