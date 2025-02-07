package org.apereo.cas.configuration.model.support.jdbc.authn;

import org.apereo.cas.configuration.model.core.authentication.AuthenticationHandlerStates;
import org.apereo.cas.configuration.model.core.authentication.PasswordEncoderProperties;
import org.apereo.cas.configuration.model.core.authentication.PrincipalTransformationProperties;
import org.apereo.cas.configuration.model.support.jpa.AbstractJpaProperties;
import org.apereo.cas.configuration.support.RegularExpressionCapable;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

/**
 * This is {@link BaseJdbcAuthenticationProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@RequiresModule(name = "cas-server-support-jdbc-authentication")
@Getter
@Setter
@Accessors(chain = true)
public abstract class BaseJdbcAuthenticationProperties extends AbstractJpaProperties {
    @Serial
    private static final long serialVersionUID = 8460723293967413501L;

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
     * Principal transformation settings for this authentication.
     */
    @NestedConfigurationProperty
    private PrincipalTransformationProperties principalTransformation = new PrincipalTransformationProperties();

    /**
     * Password encoding strategies for this authentication.
     */
    @NestedConfigurationProperty
    private PasswordEncoderProperties passwordEncoder = new PasswordEncoderProperties();

    /**
     * Name of the authentication handler.
     */
    private String name;

    /**
     * Order of the authentication handler in the chain.
     */
    private int order = Integer.MAX_VALUE;

    /**
     * Define the scope and state of this authentication handler
     * and the lifecycle in which it can be invoked or activated.
     */
    private AuthenticationHandlerStates state = AuthenticationHandlerStates.ACTIVE;

    /**
     * List of column names to fetch as user attributes.
     * This is only effective in scenarios where the JDBC authentication method
     * is able to execute a SQL query against a database table and return results.
     * Authentication methods that merely check for the user account's existence
     * or verify the user with just a simple bind are not able to fetch attributes.
     * <p>Attributes name are separated by a comma and may use a "directed list" syntax where the allowed
     * syntax would be {@code column-name->cas-attribute}.
     */
    private List<String> principalAttributeList = new ArrayList<>();
}
