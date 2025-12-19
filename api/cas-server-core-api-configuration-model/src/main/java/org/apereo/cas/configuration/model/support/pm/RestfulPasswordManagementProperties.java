package org.apereo.cas.configuration.model.support.pm;

import module java.base;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link RestfulPasswordManagementProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@RequiresModule(name = "cas-server-support-pm-rest")
@Getter
@Setter
@Accessors(chain = true)
public class RestfulPasswordManagementProperties implements CasFeatureModule, Serializable {

    @Serial
    private static final long serialVersionUID = 5262948164099973872L;

    /**
     * Endpoint URL to use when locating email addresses.
     */
    @RequiredProperty
    private String endpointUrlEmail;

    /**
     * Endpoint URL to use when locating phone numbers.
     */
    @RequiredProperty
    private String endpointUrlPhone;

    /**
     * Endpoint URL to use when locating user names.
     */
    @RequiredProperty
    private String endpointUrlUser;

    /**
     * Endpoint URL to use when locating security questions.
     */
    @RequiredProperty
    private String endpointUrlSecurityQuestions;

    /**
     * Endpoint URL to use when unlocking account.
     */
    @RequiredProperty
    private String endpointUrlAccountUnlock;

    /**
     * Endpoint URL to use when updating passwords..
     */
    @RequiredProperty
    private String endpointUrlChange;

    /**
     * Username for Basic-Auth at the password management endpoints.
     */
    @RequiredProperty
    private String endpointUsername;

    /**
     * Password for Basic-Auth at the password management endpoints.
     */
    @RequiredProperty
    private String endpointPassword;

    /**
     * Field name for username field
     * when password change requests are submitted.
     */
    @RequiredProperty
    private String fieldNameUser = "username";

    /**
     * Field name for password field
     * when password change requests are submitted.
     */
    @RequiredProperty
    private String fieldNamePassword = "password";

    /**
     * Field name for oldPassword field
     * when password change requests are submitted.
     */
    @RequiredProperty
    private String fieldNamePasswordOld = "oldPassword";

    /**
     * Additional headers to be included in REST API calls for password management.
     * The map keys are header names and the corresponding values are header values.
     */
    private Map<String, String> headers = new HashMap<>();
}
