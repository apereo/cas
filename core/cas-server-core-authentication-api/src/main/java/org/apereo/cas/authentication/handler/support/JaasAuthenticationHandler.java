package org.apereo.cas.authentication.handler.support;

import org.apereo.cas.authentication.HandlerResult;
import org.apereo.cas.authentication.UsernamePasswordCredential;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.services.ServicesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.Configuration;
import javax.security.auth.login.LoginContext;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Set;

/**
 * JAAS Authentication Handler for CAAS. This is a simple bridge from CAS'
 * authentication to JAAS.
 * <p>
 * Using the JAAS Authentication Handler requires you to configure the
 * appropriate JAAS modules. You can specify the location of a jass.conf file
 * using the following VM parameter:
 * <pre>
 * -Djava.security.auth.login.config=$PATH_TO_JAAS_CONF/jaas.conf
 * </pre>
 * <p>
 * This example jaas.conf would try Kerberos based authentication, then try LDAP
 * authentication:
 * <pre>
 * CAS {
 *   com.sun.security.auth.module.Krb5LoginModule sufficient
 *     client=TRUE
 *     debug=FALSE
 *     useTicketCache=FALSE;
 *   edu.uconn.netid.jaas.LDAPLoginModule sufficient
 *     java.naming.provider.url="ldap://ldapserver.my.edu:389/dc=my,dc=edu"
 *     java.naming.security.principal="uid=jaasauth,dc=my,dc=edu"
 *     java.naming.security.credentials="password"
 *     Attribute="uid"
 *     startTLS="true";
 * };
 * </pre>
 *
 * @author <a href="mailto:dotmatt@uconn.edu">Matthew J. Smith</a>
 * @author Marvin S. Addison
 * @author Misagh Moayyed
 * @see javax.security.auth.callback.CallbackHandler
 * @see javax.security.auth.callback.PasswordCallback
 * @see javax.security.auth.callback.NameCallback
 * @since 3.0.0
 */
public class JaasAuthenticationHandler extends AbstractUsernamePasswordAuthenticationHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(JaasAuthenticationHandler.class);

    /**
     * System property key to specify kerb5 realm.
     */
    private static final String SYS_PROP_KRB5_REALM = "java.security.krb5.realm";

    /**
     * System property key to specify kerb5 kdc.
     */
    private static final String SYS_PROP_KERB5_KDC = "java.security.krb5.kdc";

    /**
     * The realm that contains the login module information.
     */
    private String realm = "CAS";

    /**
     * System property value to overwrite the realm in krb5 config.
     */
    private String kerberosRealmSystemProperty;

    /**
     * System property value to overwrite the kdc in krb5 config.
     */
    private String kerberosKdcSystemProperty;

    /**
     * Instantiates a new Jaas authentication handler,
     * and attempts to load/verify the configuration.
     *
     * @param name             the name
     * @param servicesManager  the services manager
     * @param principalFactory the principal factory
     * @param order            the order
     */
    public JaasAuthenticationHandler(final String name, final ServicesManager servicesManager, final PrincipalFactory principalFactory, final Integer order) {
        super(name, servicesManager, principalFactory, order);
        Assert.notNull(Configuration.getConfiguration(),
                "Static Configuration cannot be null. Did you remember to specify \"java.security.auth.login.config\"?");
    }

    @Override
    protected HandlerResult authenticateUsernamePasswordInternal(final UsernamePasswordCredential credential, final String originalPassword)
            throws GeneralSecurityException {

        if (this.kerberosKdcSystemProperty != null) {
            LOGGER.debug("Configured kerberos system property [{}] to [{}]", SYS_PROP_KERB5_KDC, this.kerberosKdcSystemProperty);
            System.setProperty(SYS_PROP_KERB5_KDC, this.kerberosKdcSystemProperty);
        }
        if (this.kerberosRealmSystemProperty != null) {
            LOGGER.debug("Setting kerberos system property [{}] to [{}]", SYS_PROP_KRB5_REALM, this.kerberosRealmSystemProperty);
            System.setProperty(SYS_PROP_KRB5_REALM, this.kerberosRealmSystemProperty);
        }

        final String username = credential.getUsername();
        final String password = credential.getPassword();

        Principal principal = null;
        final LoginContext lc = new LoginContext(this.realm, new UsernamePasswordCallbackHandler(username, password));
        try {
            LOGGER.debug("Attempting authentication for: [{}]", username);
            lc.login();
            final Set<java.security.Principal> principals = lc.getSubject().getPrincipals();
            if (principals != null && !principals.isEmpty()) {
                final java.security.Principal secPrincipal = principals.iterator().next();
                principal = this.principalFactory.createPrincipal(secPrincipal.getName());
            }
        } finally {
            lc.logout();
        }

        return createHandlerResult(credential, principal, null);
    }

    public void setRealm(final String realm) {
        this.realm = realm;
    }

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
     * @param kerberosRealmSystemProperty system property to indicate realm.
     * @see <a href="http://docs.oracle.com/javase/7/docs/technotes/guides/security/jgss/tutorials/KerberosReq.html">
     * Oracle documentation</a>
     * @since 4.1.0
     */
    public void setKerberosRealmSystemProperty(final String kerberosRealmSystemProperty) {
        this.kerberosRealmSystemProperty = kerberosRealmSystemProperty;
    }

    /**
     * Typically, the default realm and the KDC for that realm are indicated in the Kerberos {@code krb5.conf} configuration file.
     * However, if you like, you can instead specify the kdc value by setting this system property value.
     * <p>If you set the realm property, you SHOULD also configure the {@link #setKerberosRealmSystemProperty(String)}.
     * <p>Also note that if you set these properties, then no cross-realm authentication is possible unless
     * a {@code krb5.conf} file is also provided from which the additional information required for cross-realm authentication
     * may be obtained.
     * <p>If you set values for these properties, then they override the default realm and KDC values specified
     * in {@code krb5.conf} (if such a file is found). The {@code krb5.conf} file is still consulted if values for items
     * other than the default realm and KDC are needed. If no {@code krb5.conf} file is found,
     * then the default values used for these items are implementation-specific.
     *
     * @param kerberosKdcSystemProperty system property to indicate kdc
     * @see <a href="http://docs.oracle.com/javase/7/docs/technotes/guides/security/jgss/tutorials/KerberosReq.html">
     * Oracle documentation</a>
     * @since 4.1.0
     */
    public void setKerberosKdcSystemProperty(final String kerberosKdcSystemProperty) {
        this.kerberosKdcSystemProperty = kerberosKdcSystemProperty;
    }

    /**
     * A simple JAAS CallbackHandler which accepts a Name String and Password
     * String in the constructor. Only NameCallbacks and PasswordCallbacks are
     * accepted in the callback array. This code based loosely on example given
     * in Sun's javadoc for CallbackHandler interface.
     */
    protected static class UsernamePasswordCallbackHandler implements CallbackHandler {

        /**
         * The username of the principal we are trying to authenticate.
         */
        private final String userName;

        /**
         * The password of the principal we are trying to authenticate.
         */
        private final String password;

        /**
         * Constructor accepts name and password to be used for authentication.
         *
         * @param userName name to be used for authentication
         * @param password Password to be used for authentication
         */
        protected UsernamePasswordCallbackHandler(final String userName, final String password) {
            this.userName = userName;
            this.password = password;
        }

        @Override
        public void handle(final Callback[] callbacks) {
            Arrays.stream(callbacks).filter(callback -> {
                if (callback.getClass().equals(NameCallback.class)) {
                    ((NameCallback) callback).setName(this.userName);
                    return false;
                }
                if (callback.getClass().equals(PasswordCallback.class)) {
                    ((PasswordCallback) callback).setPassword(this.password.toCharArray());
                    return false;
                }
                return true;
            }).findFirst().ifPresent(callback -> {
                throw new IllegalArgumentException(new UnsupportedCallbackException(callback, "Unrecognized Callback"));
            });
        }
    }
}
