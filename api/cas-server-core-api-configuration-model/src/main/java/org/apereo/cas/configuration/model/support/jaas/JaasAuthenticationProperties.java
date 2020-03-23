package org.apereo.cas.configuration.model.support.jaas;

import org.apereo.cas.configuration.model.core.authentication.PasswordEncoderProperties;
import org.apereo.cas.configuration.model.core.authentication.PasswordPolicyProperties;
import org.apereo.cas.configuration.model.core.authentication.PersonDirectoryPrincipalResolverProperties;
import org.apereo.cas.configuration.model.core.authentication.PrincipalTransformationProperties;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serializable;

/**
 * This is {@link JaasAuthenticationProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Getter
@Setter
@Accessors(chain = true)
@RequiresModule(name = "cas-server-core-authentication", automated = true)
public class JaasAuthenticationProperties implements Serializable {

    private static final long serialVersionUID = 4643338626978471986L;

    /**
     * JAAS realm to use.
     */
    @RequiredProperty
    private String realm;

    /**
     * Typically, the default realm and the KDC for that realm are indicated in the Kerberos {@code krb5.conf} configuration file.
     * However, if you like, you can instead specify the realm value by setting this following system property value.
     * <p>If you set the realm property, you SHOULD also configure the kerberos KDC system property.
     * <p>Also note that if you set these properties, then no cross-realm authentication is possible unless
     * a {@code krb5.conf} file is also provided from which the additional information required for cross-realm authentication
     * may be obtained.
     * <p>If you set values for these properties, then they override the default realm and KDC values specified
     * in {@code krb5.conf} (if such a file is found). The {@code krb5.conf} file is still consulted if values for items
     * other than the default realm and KDC are needed. If no {@code krb5.conf} file is found,
     * then the default values used for these items are implementation-specific.
     *
     * @see <a href="http://docs.oracle.com/javase/7/docs/technotes/guides/security/jgss/tutorials/KerberosReq.html">
     * Oracle documentation</a>
     */
    private String kerberosRealmSystemProperty;

    /**
     * Typically, the default realm and the KDC for that realm are indicated in the Kerberos {@code krb5.conf} configuration file.
     * However, if you like, you can instead specify the realm value by setting this following system property value.
     * <p>If you set the realm property, you SHOULD also configure the kerberos KDC system property.
     * <p>Also note that if you set these properties, then no cross-realm authentication is possible unless
     * a {@code krb5.conf} file is also provided from which the additional information required for cross-realm authentication
     * may be obtained.
     * <p>If you set values for these properties, then they override the default realm and KDC values specified
     * in {@code krb5.conf} (if such a file is found). The {@code krb5.conf} file is still consulted if values for items
     * other than the default realm and KDC are needed. If no {@code krb5.conf} file is found,
     * then the default values used for these items are implementation-specific.
     *
     * @see <a href="http://docs.oracle.com/javase/7/docs/technotes/guides/security/jgss/tutorials/KerberosReq.html">
     * Oracle documentation</a>
     */
    private String kerberosKdcSystemProperty;

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
     * Typically set to {@code JavaLoginConfig} which is the default Configuration implementation
     * from the SUN provider. This type accepts a URI/path to a configuration file as a valid parameter type specified via {@link #loginConfigurationFile}.
     * If this parameter is not specified, then the configuration information is loaded from the sources described
     * in the ConfigFile class specification. If this parameter is specified, the configuration information is loaded solely from the specified URI.
     */
    private String loginConfigType;

    /**
     * Path to the location of configuration file (i.e. jaas.conf) that contains the realms and login modules.
     */
    private String loginConfigurationFile;

    /**
     * Principal transformation settings.
     */
    @NestedConfigurationProperty
    private PrincipalTransformationProperties principalTransformation = new PrincipalTransformationProperties();

    /**
     * Password policy settings.
     */
    @NestedConfigurationProperty
    private PasswordPolicyProperties passwordPolicy = new PasswordPolicyProperties();

    /**
     * Password encoder settings for JAAS authentication.
     */
    @NestedConfigurationProperty
    private PasswordEncoderProperties passwordEncoder = new PasswordEncoderProperties();

    /**
     * Principal construction settings.
     */
    @NestedConfigurationProperty
    private PersonDirectoryPrincipalResolverProperties principal = new PersonDirectoryPrincipalResolverProperties();

    /**
     * Name of the authentication handler.
     */
    private String name;

    /**
     * Order of the authentication handler in the chain.
     */
    private int order = Integer.MAX_VALUE;
}
