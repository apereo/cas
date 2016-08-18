package org.apereo.cas.adaptors.generic;

import org.apereo.cas.authentication.HandlerResult;
import org.apereo.cas.authentication.PreventedException;
import org.apereo.cas.authentication.UsernamePasswordCredential;
import org.apereo.cas.authentication.handler.support.AbstractUsernamePasswordAuthenticationHandler;

import javax.security.auth.login.FailedLoginException;
import java.security.GeneralSecurityException;
import java.util.HashSet;
import java.util.Set;

/**
 * AuthenticationHandler which fails to authenticate a user purporting to be one
 * of the blocked usernames, and blindly authenticates all other users.
 * <p>
 * Note that RejectUsersAuthenticationHandler throws an exception when the user
 * is found in the map. This is done to indicate that this is an extreme case
 * and any AuthenticationManager checking the RejectUsersAuthenticationHandler
 * should not continue checking other Authentication Handlers on the failure of
 * RejectUsersAuthenticationHandler to authenticate someone.
 *
 * @author Scott Battaglia
 * @since 3.0.0
 */
public class RejectUsersAuthenticationHandler extends AbstractUsernamePasswordAuthenticationHandler {
    
    /**
     * The collection of users to reject.
     */
    private Set<String> users = new HashSet<>();
    
    @Override
    protected HandlerResult authenticateUsernamePasswordInternal(final UsernamePasswordCredential credential)
            throws GeneralSecurityException, PreventedException {

        final String username = credential.getUsername();
        if (this.users.contains(username)) {
            throw new FailedLoginException();
        }

        return createHandlerResult(credential, this.principalFactory.createPrincipal(username), null);
    }

    /**
     * Set the Collection of usernames which we will fail to authenticate.
     *
     * @param users The Collection of usernames we should not authenticate.
     */
    public void setUsers(final Set<String> users) {
        this.users = users;
    }

    public Set<String> getUsers() {
        return users;
    }
}
