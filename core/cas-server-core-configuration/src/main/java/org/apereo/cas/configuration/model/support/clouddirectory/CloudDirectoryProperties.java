package org.apereo.cas.configuration.model.support.clouddirectory;

import org.apereo.cas.configuration.model.core.authentication.PasswordEncoderProperties;
import org.apereo.cas.configuration.model.core.authentication.PrincipalTransformationProperties;
import org.apereo.cas.configuration.support.RequiresModule;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.core.io.Resource;

import java.io.Serializable;

/**
 * This is {@link CloudDirectoryProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiresModule(name = "cas-server-support-cloud-directory-authentication")
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

    public PrincipalTransformationProperties getPrincipalTransformation() {
        return principalTransformation;
    }

    public void setPrincipalTransformation(final PrincipalTransformationProperties principalTransformation) {
        this.principalTransformation = principalTransformation;
    }
    
    public int getOrder() {
        return order;
    }

    public void setOrder(final int order) {
        this.order = order;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public PasswordEncoderProperties getPasswordEncoder() {
        return passwordEncoder;
    }

    public void setPasswordEncoder(final PasswordEncoderProperties passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }
    
    public String getPasswordAttributeName() {
        return passwordAttributeName;
    }

    public void setPasswordAttributeName(final String passwordAttributeName) {
        this.passwordAttributeName = passwordAttributeName;
    }

    public String getUsernameIndexPath() {
        return usernameIndexPath;
    }

    public void setUsernameIndexPath(final String usernameIndexPath) {
        this.usernameIndexPath = usernameIndexPath;
    }

    public String getUsernameAttributeName() {
        return usernameAttributeName;
    }

    public void setUsernameAttributeName(final String usernameAttributeName) {
        this.usernameAttributeName = usernameAttributeName;
    }

    public String getDirectoryArn() {
        return directoryArn;
    }

    public void setDirectoryArn(final String directoryArn) {
        this.directoryArn = directoryArn;
    }

    public String getSchemaArn() {
        return schemaArn;
    }

    public void setSchemaArn(final String schemaArn) {
        this.schemaArn = schemaArn;
    }

    public String getFacetName() {
        return facetName;
    }

    public void setFacetName(final String facetName) {
        this.facetName = facetName;
    }

    public Resource getCredentialsPropertiesFile() {
        return credentialsPropertiesFile;
    }

    public void setCredentialsPropertiesFile(final Resource credentialsPropertiesFile) {
        this.credentialsPropertiesFile = credentialsPropertiesFile;
    }

    public String getCredentialAccessKey() {
        return credentialAccessKey;
    }

    public void setCredentialAccessKey(final String credentialAccessKey) {
        this.credentialAccessKey = credentialAccessKey;
    }

    public String getCredentialSecretKey() {
        return credentialSecretKey;
    }

    public void setCredentialSecretKey(final String credentialSecretKey) {
        this.credentialSecretKey = credentialSecretKey;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(final String region) {
        this.region = region;
    }

    public String getProfileName() {
        return profileName;
    }

    public void setProfileName(final String profileName) {
        this.profileName = profileName;
    }

    public String getProfilePath() {
        return profilePath;
    }

    public void setProfilePath(final String profilePath) {
        this.profilePath = profilePath;
    }
}
