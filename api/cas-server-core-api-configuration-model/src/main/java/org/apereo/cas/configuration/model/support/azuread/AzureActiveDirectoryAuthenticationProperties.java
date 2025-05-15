package org.apereo.cas.configuration.model.support.azuread;

import org.apereo.cas.configuration.model.core.authentication.AuthenticationHandlerStates;
import org.apereo.cas.configuration.model.core.authentication.PasswordEncoderProperties;
import org.apereo.cas.configuration.model.core.authentication.PrincipalTransformationProperties;
import org.apereo.cas.configuration.support.ExpressionLanguageCapable;
import org.apereo.cas.configuration.support.RegularExpressionCapable;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serial;
import java.io.Serializable;

/**
 * This is {@link AzureActiveDirectoryAuthenticationProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@RequiresModule(name = "cas-server-support-azuread-authentication")
@Getter
@Setter
@Accessors(chain = true)
public class AzureActiveDirectoryAuthenticationProperties implements Serializable {
    @Serial
    private static final long serialVersionUID = -21355975558426360L;

    /**
     * Enable authentication against Azure active directory.
     */
    private boolean enabled = true;

    /**
     * The name of the authentication handler.
     */
    private String name;

    /**
     * The order of this authentication handler in the chain.
     */
    private int order = Integer.MAX_VALUE;

    /**
     * Password encoding properties.
     */
    @NestedConfigurationProperty
    private PasswordEncoderProperties passwordEncoder = new PasswordEncoderProperties();

    /**
     * Principal transformation properties.
     */
    @NestedConfigurationProperty
    private PrincipalTransformationProperties principalTransformation = new PrincipalTransformationProperties();

    /**
     * Client id of the application.
     */
    @RequiredProperty
    @ExpressionLanguageCapable
    private String clientId;

    /**
     * Client secret of the registered app in microsoft azure portal.
     */
    @RequiredProperty
    @ExpressionLanguageCapable
    private String clientSecret;

    /**
     * This URL of the security token service that CAS goes to for acquiring
     * tokens for resources and users
     * This URL allows CAS to establish what is called an 'authority'.
     * You can think of the authority as the
     * directory issuing the identities/tokens. The login URL here is then composed of
     * {@code https://<instance>/<tenant>}, where 'instance' is the Azure AD host
     * (such as {@code https://login.microsoftonline.com}) and 'tenant' is the domain name
     * (such as {@code contoso.onmicrosoft.com}) or tenant ID of the directory.
     * Examples of authority URL are:
     *
     * <ul>
     *     <li>{@code https://login.microsoftonline.com/f31e6716-26e8-4651-b323-2563936b4163}: for a single tenant application defined in the tenant</li>
     *     <li>{@code https://login.microsoftonline.com/contoso.onmicrosoft.com}: This representation is like the previous one, but uses the tenant domain name instead of the tenant Id.</li>
     *     <li>{@code https://login.microsoftonline.de/contoso.de}: also uses a domain name, but in this case the Azure AD tenant admins have set a custom domain for their tenant, and the
     *     instance URL here is for the German national cloud.</li>
     *     <li>{@code https://login.microsoftonline.com/common}: in the case of a multi-tenant application, that is an application available in several Azure AD tenants.</li>
     *     <li>It can finally be an Active Directory Federation Services (ADFS) URL, which is recognized
     *     with the convention that the URL should contain adfs like {@code https://contoso.com/adfs}.</li>
     * </ul>
     */
    private String loginUrl = "https://login.microsoftonline.com/common/";

    /**
     * Resource url for the graph API to fetch attributes.
     */
    private String resource = "https://graph.microsoft.com/";

    /**
     * A number of authentication handlers are allowed to determine whether they can operate on the provided credential
     * and as such lend themselves to be tried and tested during the authentication handler selection phase.
     * The credential criteria may be one of the following options:<ul>
     * <li>1) A regular expression pattern that is tested against the credential identifier.</li>
     * <li>2) A fully qualified class name of your own design that implements {@code Predicate}.</li>
     * <li>3) Path to an external Groovy script that implements the same interface.</li>
     * </ul>
     */
    @RegularExpressionCapable
    private String credentialCriteria;

    /**
     * The microsoft tenant id.
     */
    @RequiredProperty
    @ExpressionLanguageCapable
    private String tenant;

    /**
     * Scope used when fetching access tokens.
     * Multiple scopes may be separated using a comma.
     */
    private String scope = "openid,email,profile,address";

    /**
     * Define the scope and state of this authentication handler
     * and the lifecycle in which it can be invoked or activated.
     */
    private AuthenticationHandlerStates state = AuthenticationHandlerStates.ACTIVE;

    /**
     * Comma-separated attributes and user properties to fetch from microsoft graph.
     * If attributes are specified here, they would be the only ones requested and
     * fetched.
     * If this field is left blank, a default set of attributes are fetched and
     * returned.
     */
    private String attributes;
}
