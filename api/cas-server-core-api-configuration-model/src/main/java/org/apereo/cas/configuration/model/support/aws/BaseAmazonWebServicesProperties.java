package org.apereo.cas.configuration.model.support.aws;

import org.apereo.cas.configuration.support.RequiredProperty;

import lombok.Getter;
import lombok.Setter;
import org.springframework.core.io.Resource;

import java.io.Serializable;

/**
 * This is {@link BaseAmazonWebServicesProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Getter
@Setter
public abstract class BaseAmazonWebServicesProperties implements Serializable {
    private static final long serialVersionUID = 6426637051495147084L;
    /**
     * Authenticate and bind into the instance via a credentials properties file.
     */
    @RequiredProperty
    private transient Resource credentialsPropertiesFile;

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
     * EC2 region override.
     */
    private String regionOverride;

    /**
     * Service name pattern.
     */
    private String serviceNameIntern;

    /**
     * AWS custom endpoint.
     */
    @RequiredProperty
    private String endpoint;

}
