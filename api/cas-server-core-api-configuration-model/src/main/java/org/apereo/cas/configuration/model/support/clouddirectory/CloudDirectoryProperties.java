package org.apereo.cas.configuration.model.support.clouddirectory;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.configuration.model.core.authentication.PasswordEncoderProperties;
import org.apereo.cas.configuration.model.core.authentication.PrincipalTransformationProperties;
import org.apereo.cas.configuration.support.RequiresModule;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.core.io.Resource;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

/**
 * This is {@link CloudDirectoryProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiresModule(name = "cas-server-support-cloud-directory-authentication")
@Slf4j
@Getter
@Setter
public class CloudDirectoryProperties implements Serializable {

    private static final long serialVersionUID = 6725526133973304269L;

    /**
     * Authenticate and bind into the instance via a credentials properties file.
     */
    @RequiredProperty
    private Resource credentialsPropertiesFile;

    /**
     * Use access-key provided by AWS to authenticate.
     */
    @RequiredProperty
    private String credentialAccessKey;

    /**
     * Use secret key provided by AWS to authenticate.
     */
    @RequiredProperty
    private String credentialSecretKey;

    /**
     * AWS region used.
     */
    @RequiredProperty
    private String region;

    /**
     * Profile name to use.
     */
    private String profileName;

    /**
     * Profile path.
     */
    private String profilePath;

    /**
     * Directory ARN.
     */
    @RequiredProperty
    private String directoryArn;

    /**
     * Schema ARN.
     */
    @RequiredProperty
    private String schemaArn;

    /**
     * Facet name.
     */
    private String facetName;

    /**
     * Username attribute to choose when locating accounts.
     */
    @RequiredProperty
    private String usernameAttributeName;

    /**
     * Password attribute to choose on the entry to compare.
     */
    @RequiredProperty
    private String passwordAttributeName;

    /**
     * Username index path.
     */
    private String usernameIndexPath;

    /**
     * The name of the authentication handler.
     */
    private String name;

    /**
     * Password encoding properties.
     */
    @NestedConfigurationProperty
    private PasswordEncoderProperties passwordEncoder = new PasswordEncoderProperties();

    /**
     * Principal transformation properties.
     */
    @NestedConfigurationProperty
    private PrincipalTransformationProperties principalTransformation = new PrincipalTransformationProperties();

    /**
     * The order of this authentication handler in the chain.
     */
    private int order = Integer.MAX_VALUE;
}
