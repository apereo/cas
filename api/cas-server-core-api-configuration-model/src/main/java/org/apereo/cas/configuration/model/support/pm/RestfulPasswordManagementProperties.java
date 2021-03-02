package org.apereo.cas.configuration.model.support.pm;

import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

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
@JsonFilter("RestfulPasswordManagementProperties")
public class RestfulPasswordManagementProperties implements Serializable {

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
}
