package org.apereo.cas.configuration.model.support.throttle;

import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * Configuration properties class for cas.throttle.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@RequiresModule(name = "cas-server-support-throttle")
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("ThrottleCoreProperties")
public class ThrottleCoreProperties implements Serializable {
    /**
     * Default app code for throttling and audits.
     */
    private static final String DEFAULT_APPLICATION_CODE = "CAS";

    private static final long serialVersionUID = -1806129199319966518L;

    /**
     * Username parameter to use in order to extract the username from the request.
     */
    private String usernameParameter;

    /**
     * Application code used to identify this application in the audit logs.
     */
    private String appCode = DEFAULT_APPLICATION_CODE;
}
