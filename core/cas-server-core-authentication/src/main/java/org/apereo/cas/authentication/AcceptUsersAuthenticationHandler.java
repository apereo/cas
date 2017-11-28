package org.apereo.cas.authentication;

import org.apache.commons.codec.binary.StringUtils;
import org.apereo.cas.authentication.handler.support.AbstractUsernamePasswordAuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.services.ServicesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.FailedLoginException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handler that contains a list of valid users and passwords. Useful if there is
 * a small list of users that we wish to allow. An example use case may be if
 * there are existing handlers that make calls to LDAP, etc. but there is a need
 * for additional users we don't want in LDAP. With the chain of command
 * processing of handlers, this handler could be added to check before LDAP and
 * provide the list of additional users. The list of acceptable users is stored
 * in a map. The key of the map is the username and the password is the object
 * retrieved from doing map.get(KEY).
 *
 * @author Scott Battaglia
 * @author Marvin S. Addison
 * @since 3.0.0
 */
public class AcceptUsersAuthenticationHandler extends AbstractUsernamePasswordAuthenticationHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(AcceptUsersAuthenticationHandler.class);

    /**
     * The list of users we will accept.
     */
    private Map<String, String> users;

    /**
     * Instantiates a new Accept users authentication handler.
     *
     * @param name the name
     */
    public AcceptUsersAuthenticationHandler(final String name) {
        this(name, null, null, null, new HashMap<>());
    }

    /**
     * Instantiates a new Accept users authentication handler.
     *
     * @param name             the name
     * @param servicesManager  the services manager
     * @param principalFactory the principal factory
     * @param order            the order
     * @param users            the users
     */
    public AcceptUsersAuthenticationHandler(final String name, final ServicesManager servicesManager, final PrincipalFactory principalFactory,
                                            final Integer order, final Map<String, String> users) {
        super(name, servicesManager, principalFactory, order);
        this.users = users;
    }

    @Override
    protected HandlerResult authenticateUsernamePasswordInternal(final UsernamePasswordCredential credential,
                                                                 final String originalPassword) throws GeneralSecurityException {
        if (this.users == null || this.users.isEmpty()) {
            throw new FailedLoginException("No user can be accepted because none is defined");
        }
        final String username = credential.getUsername();
        final String cachedPassword = this.users.get(username);

        if (cachedPassword == null) {
            LOGGER.debug("[{}] was not found in the map.", username);
            throw new AccountNotFoundException(username + " not found in backing map.");
        }

        if (!StringUtils.equals(credential.getPassword(), cachedPassword)) {
            throw new FailedLoginException();
        }
        final List<MessageDescriptor> list = new ArrayList<>();
        return createHandlerResult(credential, this.principalFactory.createPrincipal(username), list);
    }

    /**
     * @param users The users to set.
     */
    public void setUsers(final Map<String, String> users) {
        this.users = new HashMap<>(users);
    }
}
