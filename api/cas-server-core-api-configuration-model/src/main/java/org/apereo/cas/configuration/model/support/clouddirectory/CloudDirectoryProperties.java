package org.apereo.cas.configuration.model.support.clouddirectory;

import org.apereo.cas.configuration.model.core.authentication.PasswordEncoderProperties;
import org.apereo.cas.configuration.model.core.authentication.PrincipalTransformationProperties;
import org.apereo.cas.configuration.model.support.aws.BaseAmazonWebServicesProperties;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * This is {@link CloudDirectoryProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiresModule(name = "cas-server-support-cloud-directory-authentication")
@Getter
@Setter
@Accessors(chain = true)
public class CloudDirectoryProperties extends BaseAmazonWebServicesProperties {

    private static final long serialVersionUID = 6725526133973304269L;

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
