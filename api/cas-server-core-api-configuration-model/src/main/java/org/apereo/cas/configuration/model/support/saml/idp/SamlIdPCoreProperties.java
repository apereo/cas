package org.apereo.cas.configuration.model.support.saml.idp;

import org.apereo.cas.configuration.support.ExpressionLanguageCapable;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * This is {@link SamlIdPCoreProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@RequiresModule(name = "cas-server-support-saml-idp")
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("SamlIdPCoreProperties")
public class SamlIdPCoreProperties implements Serializable {
    private static final long serialVersionUID = -1848175783676789852L;

    /**
     * Indicates whether attribute query profile is enabled.
     * Enabling this setting would allow CAS to record SAML
     * responses and have them be made available later for attribute lookups.
     */
    private boolean attributeQueryProfileEnabled;

    /**
     * Indicates whether saml requests, and other session data,
     * collected as part of SAML flows and requests
     * that are kept by the container http session, local storage, or should be replicated
     * across the cluster.
     */
    private SessionStorageTypes sessionStorageType = SessionStorageTypes.HTTP;

    /**
     * The SAML entity id for the deployment.
     */
    @RequiredProperty
    @ExpressionLanguageCapable
    private String entityId = "https://cas.example.org/idp";

    /**
     * A mapping of authentication context class refs.
     * This is where specific authentication context classes
     * are reference and mapped to providers that CAS may support
     * mainly for MFA purposes.
     * <p>
     * Example might be {@code urn:oasis:names:tc:SAML:2.0:ac:classes:SomeClassName->mfa-duo}.
     */
    private List<String> authenticationContextClassMappings = new ArrayList<>(0);

    /**
     * A mapping of attribute names to their friendly names, defined globally.
     * Example might be {@code urn:oid:1.3.6.1.4.1.5923.1.1.1.6->eduPersonPrincipalName}.
     */
    private List<String> attributeFriendlyNames = new ArrayList<>(0);

    /**
     * Define session storage types.
     */
    public enum SessionStorageTypes {
        /**
         * Saml requests, and other session data collected as part of SAML flows and requests
         * are kept in the http servlet session that is local to the server.
         */
        HTTP,
        /**
         * Saml requests, and other session data collected as part of SAML flows and requests
         * are kept in the client browser's session storage, signed and encrypted. SAML2 interactions
         * require client-side read/write operations to restore the session from the browser.
         */
        BROWSER_SESSION_STORAGE,
        /**
         * Saml requests, and other session data collected as part of SAML flows and requests
         * are tracked as CAS tickets in the registry and replicated across the entire cluster
         * as tickets.
         */
        TICKET_REGISTRY
    }
}
