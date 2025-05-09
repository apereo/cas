package org.apereo.cas.configuration.model.support.oidc;

import org.apereo.cas.configuration.support.DurationCapable;
import org.apereo.cas.configuration.support.RegularExpressionCapable;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is {@link OidcCoreProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-support-oidc")
@Getter
@Setter
@Accessors(chain = true)
public class OidcCoreProperties implements Serializable {

    @Serial
    private static final long serialVersionUID = 823028615694269276L;

    /**
     * OIDC issuer.
     * All OpenID Connect servers such as CAS are uniquely identified by a URL known as the issuer.
     * This URL serves as the prefix of a service discovery endpoint as specified
     * in the OpenID Connect Discovery standard.
     * <p>
     * This URL must be using the https scheme with no query or fragment component that
     * the identity provider (CAS) asserts as its Issuer Identifier. This also MUST be
     * identical to the {@code iss} claim value in ID Tokens issued from this issuer,
     * unless overridden in very special circumstances as a last resort.
     * <p>
     * CAS primarily supports a single issuer per deployment/host.
     */
    @RequiredProperty
    private String issuer = "http://localhost:8080/cas/oidc";

    /**
     * Defines the regular expression pattern that is matched against the calculated issuer
     * from the request. If the issuer that is extracted from the request does not match
     * the {@link #issuer} defined in the CAS configuration, this pattern acts as a secondary
     * level rule to allow incoming requests to pass through if the match is successful. By default,
     * the pattern is designed to never match anything.
     */
    @RegularExpressionCapable
    private String acceptedIssuersPattern = "a^";

    /**
     * Skew value used to massage the authentication issue instance.
     */
    @DurationCapable
    private String skew = "PT5M";

    /**
     * Mapping of user-defined scopes. Key is the new scope name
     * and value is a comma-separated list of claims mapped to the scope.
     * Such user-defined scopes are also able to override the definition of system scopes.
     * User-defined scopes as well as any and all custom claims that are mapped to the scope
     * must also be defined as scopes and claims supported by CAS in OpenID Connect discovery.
     */
    private Map<String, String> userDefinedScopes = new HashMap<>();

    /**
     * Map fixed claims to CAS attributes.
     * Key is the existing claim name for a scope and value is the new attribute
     * that should take its place and value.
     * Claims associated with a scope (i.e. given_name for profile) are fixed in the OpenID specification.
     * In the event that custom arbitrary attributes should be mapped to claims, mappings can
     * be defined in CAS settings to link a CAS-defined attribute to a fixed given scope. For
     * instance, CAS configuration may allow the value of the attribute {@code sys_given_name} to be
     * mapped and assigned to the claim {@code given_name} without having an impact on the attribute
     * resolution configuration and all other CAS-enabled applications.
     * If mapping is not defined, by default CAS attributes are expected to match claim names.
     */
    private Map<String, String> claimsMap = new HashMap<>();

    /**
     * A mapping of authentication context refs (ACR) values.
     * This is where specific authentication context classes
     * are referenced and mapped to providers that CAS may support
     * mainly for MFA purposes.
     * <p>
     * Example might be {@code acr-value->mfa-duo}.
     * <p>
     * Support for authentication context class references is implemented in form of {@code acr_values} as part of the original
     * authorization request, which is mostly taken into account by
     * the multifactor authentication features of CAS.
     * Once successful, {@code acr} and {@code amr} values are passed back to the relying party as part of the id token.
     */
    private List<String> authenticationContextReferenceMappings = new ArrayList<>();
}
