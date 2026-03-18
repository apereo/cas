package org.apereo.cas.configuration.model.support.pac4j.oidc;

import org.apereo.cas.configuration.model.SpringResourceProperties;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * This is {@link Pac4jOidcClientFederationProperties}.
 *
 * @author Jerome LELEU
 * @since 8.0.0
 */
@RequiresModule(name = "cas-server-support-pac4j-oidc")
@Getter
@Setter
@Accessors(chain = true)
public class Pac4jOidcClientFederationProperties implements Serializable {
    @Serial
    private static final long serialVersionUID = 5192010226236750446L;

    /**
     * Whether the federation is enabled.
     */
    private boolean enabled;

    /**
     * Specific JWKS for the federation.
     */
    @NestedConfigurationProperty
    private SpringResourceProperties jwks;

    /**
     * Validity in days of the RP (client) entity statement.
     */
    private int validityInDays = 90;

    /**
     * Application type for the RP (client) entity statement.
     */
    private String applicationType = "web";

    /**
     * Response types for the RP (client) entity statement.
     */
    private List<String> responseTypes = new ArrayList<>(List.of("code"));

    /**
     * Grant types for the RP (client) entity statement.
     */
    private List<String> grantTypes = new ArrayList<>(List.of("authorization_code"));

    /**
     * Scopes for the RP (client) entity statement.
     */
    private List<String> scopes = new ArrayList<>(List.of("openid", "email", "profile"));

    /**
     * Registration types for the RP (client) entity statement.
     */
    private List<String> clientRegistrationTypes = new ArrayList<>(List.of("explicit", "automatic"));

    /**
     * The trust anchors to use for federation.
     */
    private Map<String, String> trustAnchors = new LinkedHashMap<>();

    /**
     * The target OIDC provider in federation.
     */
    private String targetOp;

    /**
     * The contact name for this client.
     */
    private String contactName;

    /**
     * The contact emails for this client.
     */
    private List<String> contactEmails = new ArrayList<>();
}
