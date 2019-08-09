package org.apereo.cas.authentication.handler.support.jaas;

import org.apereo.cas.authentication.AuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.handler.support.AbstractUsernamePasswordAuthenticationHandler;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.services.ServicesManager;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.Configuration;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginContext;
import java.io.File;
import java.security.GeneralSecurityException;
import java.security.URIParameter;
import java.util.Arrays;

/**
 * JAAS Authentication Handler for CAS. This is a simple bridge from CAS'
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
@Slf4j
@Setter
public class JaasAuthenticationHandler extends AbstractUsernamePasswordAuthenticationHandler {

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

    private String loginConfigType;

    private File loginConfigurationFile;

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
    }

    @Override
    protected AuthenticationHandlerExecutionResult authenticateUsernamePasswordInternal(final UsernamePasswordCredential credential,
                                                                                        final String originalPassword) throws GeneralSecurityException {
        if (StringUtils.isNotBlank(this.kerberosKdcSystemProperty)) {
            LOGGER.debug("Configured kerberos system property [{}] to [{}]", SYS_PROP_KERB5_KDC, this.kerberosKdcSystemProperty);
            System.setProperty(SYS_PROP_KERB5_KDC, this.kerberosKdcSystemProperty);
        }
        if (StringUtils.isNotBlank(this.kerberosRealmSystemProperty)) {
            LOGGER.debug("Setting kerberos system property [{}] to [{}]", SYS_PROP_KRB5_REALM, this.kerberosRealmSystemProperty);
            System.setProperty(SYS_PROP_KRB5_REALM, this.kerberosRealmSystemProperty);
        }

        val principal = authenticateAndGetPrincipal(credential);
        val strategy = getPasswordPolicyHandlingStrategy();
        if (principal != null && strategy != null) {
            LOGGER.debug("Attempting to examine and handle password policy via [{}]", strategy.getClass().getSimpleName());
            val messageList = strategy.handle(principal, getPasswordPolicyConfiguration());
            return createHandlerResult(credential, principal, messageList);
        }
        throw new FailedLoginException("Unable to authenticate " + credential.getId());
    }

    /**
     * Authenticate and get principal principal.
     *
     * @param credential the credential
     * @return the principal
     * @throws GeneralSecurityException the general security exception
     */
    protected Principal authenticateAndGetPrincipal(final UsernamePasswordCredential credential) throws GeneralSecurityException {
        val lc = getLoginContext(credential);
        try {
            lc.login();
            val principals = lc.getSubject().getPrincipals();
            LOGGER.debug("JAAS principals extracted from subject are [{}]", principals);
            if (principals != null && !principals.isEmpty()) {
                val secPrincipal = principals.iterator().next();
                LOGGER.debug("JAAS principal detected from subject login context is [{}]", secPrincipal.getName());
                return this.principalFactory.createPrincipal(secPrincipal.getName());
            }
        } finally {
            if (lc != null) {
                lc.logout();
            }
        }
        return null;
    }

    /**
     * Gets login context.
     *
     * @param credential the credential
     * @return the login context
     * @throws GeneralSecurityException the general security exception
     */
    protected LoginContext getLoginContext(final UsernamePasswordCredential credential) throws GeneralSecurityException {
        val callbackHandler = new UsernamePasswordCallbackHandler(credential.getUsername(), credential.getPassword());
        if (this.loginConfigurationFile != null && StringUtils.isNotBlank(this.loginConfigType)
            && this.loginConfigurationFile.exists() && this.loginConfigurationFile.canRead()) {
            final Configuration.Parameters parameters = new URIParameter(loginConfigurationFile.toURI());
            val loginConfig = Configuration.getInstance(this.loginConfigType, parameters);
            return new LoginContext(this.realm, null, callbackHandler, loginConfig);
        }
        return new LoginContext(this.realm, callbackHandler);
    }

    /**
     * A simple JAAS CallbackHandler which accepts a Name String and Password
     * String in the constructor. Only NameCallbacks and PasswordCallbacks are
     * accepted in the callback array. This code based loosely on example given
     * in Sun's javadoc for CallbackHandler interface.
     */
    @RequiredArgsConstructor
    protected static class UsernamePasswordCallbackHandler implements CallbackHandler {
        private final String userName;
        private final String password;

        @Override
        public void handle(final Callback[] callbacks) {
            Arrays.stream(callbacks)
                .filter(callback -> {
                    if (callback.getClass().equals(NameCallback.class)) {
                        ((NameCallback) callback).setName(this.userName);
                        return false;
                    }
                    if (callback.getClass().equals(PasswordCallback.class)) {
                        ((PasswordCallback) callback).setPassword(this.password.toCharArray());
                        return false;
                    }
                    return true;
                })
                .findFirst()
                .ifPresent(callback -> {
                    throw new IllegalArgumentException(new UnsupportedCallbackException(callback, "Unrecognized Callback"));
                });
        }
    }
}
