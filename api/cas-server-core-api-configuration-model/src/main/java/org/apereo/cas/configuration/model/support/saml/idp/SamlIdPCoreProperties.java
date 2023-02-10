package org.apereo.cas.configuration.model.support.saml.idp;

import org.apereo.cas.configuration.model.core.web.session.SessionStorageTypes;
import org.apereo.cas.configuration.model.support.replication.SessionReplicationProperties;
import org.apereo.cas.configuration.support.ExpressionLanguageCapable;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serial;
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
    @Serial
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
     * mainly for, i.e. MFA purposes.
     * <p>
     * Example might be {@code urn:oasis:names:tc:SAML:2.0:ac:classes:SomeClassName->mfa-duo}.
     * <p>
     * In delegated authentication scenarios, this can also be a mapping of authentication context class refs,
     * when CAS is proxying/delegating authentication to an external SAML2 identity provider. The requested authentication context
     * as submitted by the service provider is first received by CAS, and then gets mapped to
     * a context class that is passed onto the external identity provider. For example, you might have a scenario
     * where a SAML2 service provider would submit {@code https://refeds.org/profile/mfa} to CAS, and CAS would
     * translate that to {@code http://schemas.microsoft.com/claims/multipleauthn} to ultimate route the
     * authentication request to Azure. If no mapping is found, the original context is passed as is.
     * <p>
     * Example might be {@code https://refeds.org/profile/mfa->http://schemas.microsoft.com/claims/multipleauthn}.
     */
    private List<String> authenticationContextClassMappings = new ArrayList<>(0);


    /**
     * A mapping of attribute names to their friendly names, defined globally.
     * Example might be {@code urn:oid:1.3.6.1.4.1.5923.1.1.1.6->eduPersonPrincipalName}.
     */
    private List<String> attributeFriendlyNames = new ArrayList<>(0);

    /**
     * Control settings for session replication.
     */
    @NestedConfigurationProperty
    private SessionReplicationProperties sessionReplication = new SessionReplicationProperties();
}
