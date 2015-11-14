package org.jasig.cas.authentication;

import org.jasig.cas.authentication.handler.support.AbstractUsernamePasswordAuthenticationHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.FailedLoginException;
import javax.validation.constraints.NotNull;
import java.security.GeneralSecurityException;
import java.util.Collections;
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
 * <p>
 * Note that this class makes an unmodifiable copy of whatever map is provided
 * to it.
 *
 * @author Scott Battaglia
 * @author Marvin S. Addison
 *
 * @since 3.0.0
 */
@Component("acceptUsersAuthenticationHandler")
public class AcceptUsersAuthenticationHandler extends AbstractUsernamePasswordAuthenticationHandler {

    /** The list of users we will accept. */
    @NotNull
    @Value("#{T(java.util.Collections).singletonMap('casuser', 'Mellon')}")
    private Map<String, String> users;

    @Override
    protected final HandlerResult authenticateUsernamePasswordInternal(final UsernamePasswordCredential credential)
            throws GeneralSecurityException, PreventedException {

        final String username = credential.getUsername();
        final String cachedPassword = this.users.get(username);

        if (cachedPassword == null) {
           logger.debug("{} was not found in the map.", username);
           throw new AccountNotFoundException(username + " not found in backing map.");
        }

        final String encodedPassword = this.getPasswordEncoder().encode(credential.getPassword());
        if (!cachedPassword.equals(encodedPassword)) {
            throw new FailedLoginException();
        }
        return createHandlerResult(credential, this.principalFactory.createPrincipal(username), null);
    }

    /**
     * @param users The users to set.
     */
    public final void setUsers(final Map<String, String> users) {
        this.users = Collections.unmodifiableMap(users);
    }
}
