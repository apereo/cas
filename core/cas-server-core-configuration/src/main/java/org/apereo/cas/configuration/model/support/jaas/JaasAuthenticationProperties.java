package org.apereo.cas.configuration.model.support.jaas;

import org.apereo.cas.configuration.model.core.authentication.PasswordEncoderProperties;
import org.apereo.cas.configuration.model.core.authentication.PrincipalTransformationProperties;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serializable;

/**
 * This is {@link JaasAuthenticationProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
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
     * <p>If you set the realm property, you SHOULD also configure the {@link #setKerberosKdcSystemProperty(String)}.
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
     * <p>If you set the realm property, you SHOULD also configure the {@link #setKerberosKdcSystemProperty(String)}.
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
     * Principal transformation settings.
     */
    @NestedConfigurationProperty
    private PrincipalTransformationProperties principalTransformation = new PrincipalTransformationProperties();

    /**
     * Password encoder settings for JAAS authentication.
     */
    @NestedConfigurationProperty
    private PasswordEncoderProperties passwordEncoder = new PasswordEncoderProperties();

    /**
     * Name of the authentication handler.
     */
    private String name;

    public String getCredentialCriteria() {
        return credentialCriteria;
    }

    public void setCredentialCriteria(final String credentialCriteria) {
        this.credentialCriteria = credentialCriteria;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public PrincipalTransformationProperties getPrincipalTransformation() {
        return principalTransformation;
    }

    public void setPrincipalTransformation(final PrincipalTransformationProperties principalTransformation) {
        this.principalTransformation = principalTransformation;
    }

    public PasswordEncoderProperties getPasswordEncoder() {
        return passwordEncoder;
    }

    public void setPasswordEncoder(final PasswordEncoderProperties passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public String getRealm() {
        return realm;
    }

    public void setRealm(final String realm) {
        this.realm = realm;
    }

    public String getKerberosRealmSystemProperty() {
        return kerberosRealmSystemProperty;
    }

    public void setKerberosRealmSystemProperty(final String kerberosRealmSystemProperty) {
        this.kerberosRealmSystemProperty = kerberosRealmSystemProperty;
    }

    public String getKerberosKdcSystemProperty() {
        return kerberosKdcSystemProperty;
    }

    public void setKerberosKdcSystemProperty(final String kerberosKdcSystemProperty) {
        this.kerberosKdcSystemProperty = kerberosKdcSystemProperty;
    }
}
