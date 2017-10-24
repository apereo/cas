package org.apereo.cas.configuration.model.support.generic;

import org.apereo.cas.configuration.model.core.authentication.PasswordEncoderProperties;
import org.apereo.cas.configuration.model.core.authentication.PrincipalTransformationProperties;
import org.apereo.cas.configuration.support.RequiresModule;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.core.io.Resource;

import java.io.Serializable;

/**
 * This is {@link FileAuthenticationProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-support-generic")
public class FileAuthenticationProperties implements Serializable {

    private static final long serialVersionUID = 4031366217090049241L;
    /**
     * File resource where user accounts are kept.
     */
    private Resource filename;
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
     * Authentication hanler name used to verify credentials in the file.
     */
    private String name;

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public PrincipalTransformationProperties getPrincipalTransformation() {
        return principalTransformation;
    }

    public void setPrincipalTransformation(final PrincipalTransformationProperties principalTransformation) {
        this.principalTransformation = principalTransformation;
    }

    public PasswordEncoderProperties getPasswordEncoder() {
        return passwordEncoder;
    }

    public void setPasswordEncoder(final PasswordEncoderProperties passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public Resource getFilename() {
        return filename;
    }

    public void setFilename(final Resource filename) {
        this.filename = filename;
    }

    public String getSeparator() {
        return separator;
    }

    public void setSeparator(final String separator) {
        this.separator = separator;
    }
}
