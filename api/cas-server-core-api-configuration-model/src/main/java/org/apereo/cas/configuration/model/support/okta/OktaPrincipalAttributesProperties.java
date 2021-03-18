package org.apereo.cas.configuration.model.support.okta;

import org.apereo.cas.configuration.model.SpringResourceProperties;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This is {@link OktaPrincipalAttributesProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@RequiresModule(name = "cas-server-support-okta-authentication")
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("OktaPrincipalAttributesProperties")
public class OktaPrincipalAttributesProperties extends BaseOktaProperties {
    private static final long serialVersionUID = -6573755681498251678L;

    /**
     * Username attribute to fetch attributes by.
     */
    @RequiredProperty
    private String usernameAttribute = "username";

    /**
     * A value can be assigned to this field to uniquely identify this resolver.
     */
    private String id;

    /**
     * Okta allows you to interact with Okta APIs using scoped OAuth 2.0 access tokens. Each access token
     * enables the bearer to perform specific actions on specific Okta endpoints, with that
     * ability controlled by which scopes the access token contains. Scopes are only used
     * when using client id and private-key.
     */
    @RequiredProperty
    private List<String> scopes = Stream.of("okta.users.read", "okta.apps.read").collect(Collectors.toList());

    /**
     * Okta client id used in combination with the private key.
     */
    @RequiredProperty
    private String clientId;

    /**
     * Private key resource used for oauth20 api calls
     * with a client id. When using this approach, you won't need an API Token because
     * the Okta SDK will request an access token for you.
     */
    @NestedConfigurationProperty
    private SpringResourceProperties privateKey = new SpringResourceProperties();

    /**
     * Okta API token.
     */
    private String apiToken;
}
