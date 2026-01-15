package org.apereo.cas.configuration.model.support.generic;

import module java.base;
import org.apereo.cas.configuration.model.core.authentication.PasswordEncoderProperties;
import org.apereo.cas.configuration.model.core.authentication.PrincipalTransformationProperties;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.core.io.Resource;

/**
 * This is {@link FileAuthenticationProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-support-generic")
@Getter
@Setter
@Accessors(chain = true)
public class FileAuthenticationProperties implements Serializable {

    @Serial
    private static final long serialVersionUID = 4031366217090049241L;

    /**
     * File resource where user accounts are kept.
     */
    @RequiredProperty
    private transient Resource filename;

    /**
     * Separator character that distinguishes between usernames and passwords in the file.
     */
    private String separator = "::";

    /**
     * Password encoder properties.
     */
    @NestedConfigurationProperty
    private PasswordEncoderProperties passwordEncoder = new PasswordEncoderProperties();

    /**
     * Principal transformation settings for this authentication.
     */
    @NestedConfigurationProperty
    private PrincipalTransformationProperties principalTransformation = new PrincipalTransformationProperties();

    /**
     * Authentication handler name used to verify credentials in the file.
     */
    private String name;
}
