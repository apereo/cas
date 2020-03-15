package org.apereo.cas.configuration.model.support.ldap;

import org.apereo.cas.configuration.model.core.authentication.PasswordEncoderProperties;
import org.apereo.cas.configuration.model.core.authentication.PrincipalTransformationProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * This is {@link LdapAuthenticationProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-support-ldap")
@Getter
@Setter
@Accessors(chain = true)
public class LdapAuthenticationProperties extends AbstractLdapAuthenticationProperties {

    private static final long serialVersionUID = -5357843463521189892L;

    /**
     * Password policy settings.
     */
    @NestedConfigurationProperty
    private LdapPasswordPolicyProperties passwordPolicy = new LdapPasswordPolicyProperties();

    /**
     * Principal transformation settings.
     */
    @NestedConfigurationProperty
    private PrincipalTransformationProperties principalTransformation = new PrincipalTransformationProperties();

    /**
     * Password encoder settings for LDAP authentication.
     */
    @NestedConfigurationProperty
    private PasswordEncoderProperties passwordEncoder = new PasswordEncoderProperties();

    /**
     * A number of authentication handlers are allowed to determine whether they can operate on the provided credential
     * and as such lend themselves to be tried and tested during the authentication handler selection phase.
     * The credential criteria may be one of the following options:<ul>
     * <li>1) A regular expression pattern that is tested against the credential identifier.</li>
     * <li>2) A fully qualified class name of your own design that implements {@code Predicate<Credential>}.</li>
     * <li>3) Path to an external Groovy script that implements the same interface.</li>
     * </ul>
     */
    private String credentialCriteria;

    /**
     * The attribute to use as the principal identifier built during and upon a successful authentication attempt.
     */
    private String principalAttributeId;

    /**
     * Name of attribute to be used for principal's DN.
     */
    private String principalDnAttributeName = "principalLdapDn";

    /**
     * List of attributes to retrieve from LDAP.
     * Attributes can be virtually remapped to multiple names.
     * Example {@code cn:commonName,givenName,eduPersonTargettedId:SOME_IDENTIFIER}
     */
    private List principalAttributeList = new ArrayList<>(0);

    /**
     * Sets a flag that determines whether multiple values are allowed for the {@link #principalAttributeId}.
     * This flag only has an effect if {@link #principalAttributeId} is configured. If multiple values are detected
     * when the flag is false, the first value is used and a warning is logged. If multiple values are detected
     * when the flag is true, an exception is raised.
     */
    private boolean allowMultiplePrincipalAttributeValues;

    /**
     * List of additional attributes to retrieve, if any.
     */
    private List additionalAttributes = new ArrayList<>(0);

    /**
     * Flag to indicate whether CAS should block authentication
     * if a specific/configured principal id attribute is not found.
     */
    private boolean allowMissingPrincipalAttributeValue = true;

    /**
     * When entry DN should be called as an attribute and stored into the principal.
     */
    private boolean collectDnAttribute;

    /**
     * Order of the authentication handler in the chain.
     */
    private Integer order;
}
