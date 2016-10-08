package org.apereo.cas.authentication;

import org.apache.commons.codec.binary.StringUtils;
import org.apereo.cas.authentication.handler.support.AbstractUsernamePasswordAuthenticationHandler;

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

    /**
     * The list of users we will accept.
     */
    private Map<String, String> users;

    /**
     * Instantiates a new Accept users authentication handler.
     */
    public AcceptUsersAuthenticationHandler() {
        this(new HashMap<>());
    }
    
    /**
     * Instantiates a new Accept users authentication handler.
     *
     * @param users the users
     */
    public AcceptUsersAuthenticationHandler(final Map<String, String> users) {
        this.users = users;
    }

    @Override
    protected HandlerResult authenticateUsernamePasswordInternal(final UsernamePasswordCredential credential)
            throws GeneralSecurityException, PreventedException {
        if (this.users == null || this.users.isEmpty()) {
            throw new FailedLoginException("No user can be accepted because none is defined");
        }
        final String username = credential.getUsername();
        final String cachedPassword = this.users.get(username);

        if (cachedPassword == null) {
            logger.debug("{} was not found in the map.", username);
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
