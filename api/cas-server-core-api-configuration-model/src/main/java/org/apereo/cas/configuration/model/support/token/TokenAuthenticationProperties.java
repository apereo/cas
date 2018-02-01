package org.apereo.cas.configuration.model.support.token;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.configuration.model.core.authentication.PrincipalTransformationProperties;
import org.apereo.cas.configuration.model.core.util.EncryptionOptionalSigningJwtCryptographyProperties;
import org.apereo.cas.configuration.support.RequiresModule;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

/**
 * This is {@link TokenAuthenticationProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-support-token-webflow")
@Slf4j
@Getter
@Setter
public class TokenAuthenticationProperties implements Serializable {

    private static final long serialVersionUID = 6016124091895278265L;

    /**
     * Principal transformation settings.
     */
    @NestedConfigurationProperty
    private PrincipalTransformationProperties principalTransformation = new PrincipalTransformationProperties();

    /**
     * Crypto settings.
     */
    @NestedConfigurationProperty
    private EncryptionOptionalSigningJwtCryptographyProperties crypto = new EncryptionOptionalSigningJwtCryptographyProperties();

    /**
     * Name of the authentication handler.
     */
    private String name;
}
