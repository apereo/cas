package org.apereo.cas.configuration.model.core.audit;

import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This is {@link AuditEngineProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-core-audit", automated = true)
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("AuditEngineProperties")
public class AuditEngineProperties implements Serializable {

    private static final long serialVersionUID = 3946106584608417663L;

    /**
     * Whether auditing functionality should be enabled.
     */
    private boolean enabled = true;

    /**
     * Retrieve audit records from storage, starting from now
     * and going back the indicated number of days in history.
     */
    private int numberOfDaysInHistory = 30;

    /**
     * Whether ticket validation events in the audit log should include
     * information about the assertion that is validated; things such as
     * the principal id and attributes released.
     */
    private boolean includeValidationAssertion;

    /**
     * Application code to use in the audit logs.
     * <p>
     * This is a unique code that acts as the identifier for the application.
     * In case audit logs are aggregated in a central location. This makes it easy
     * to identify the application and filter results based on the code.
     */
    private String appCode = "CAS";

    /**
     * Request header to use identify the server address.
     */
    private String alternateServerAddrHeaderName;

    /**
     * Request header to use identify the client address.
     * <p>
     * If the application is sitting behind a load balancer,
     * the client address typically ends up being the load balancer
     * address itself. A common example for a header here would be
     * {@code X-Forwarded-For} to glean the client address
     * from the request, assuming the load balancer is configured correctly
     * to pass that header along.
     */
    private String alternateClientAddrHeaderName;

    /**
     * Determines whether a local DNS lookup should be made to query for the CAS server address.
     * <p>
     * By default, the server is address is determined from the request. Aside from special headers,
     * this option allows one to query DNS to look up the server address of the CAS server processing requests.
     */
    private boolean useServerHostAddress;
    
    /**
     * Indicates whether catastrophic audit failures should be logged
     * or whether errors should bubble up and thrown back.
     */
    private boolean ignoreAuditFailures;

    /**
     * Indicate a list of supported audit actions that should be recognized,
     * processed and recorded by CAS audit managers. Each supported action
     * can be treated as a regular expression to match against built-in
     * CAS actions.
     */
    private List<String> supportedActions = Stream.of("*").collect(Collectors.toList());

    /**
     * Indicate a list of supported audit actions that should be excluded,
     * filtered and ignored by CAS audit managers. Each supported action
     * can be treated as a regular expression to match against built-in
     * CAS actions.
     */
    private List<String> excludedActions = new ArrayList<>();

    /**
     * The audit format to use in the logs.
     */
    private AuditFormatTypes auditFormat = AuditFormatTypes.DEFAULT;

    /**
     * The audit format types.
     */
    public enum AuditFormatTypes {
        /**
         * Default audit format.
         */
        DEFAULT,
        /**
         * Output audit logs as JSON strings
         * where necessary/possible.
         */
        JSON
    }
}
