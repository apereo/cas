package org.apereo.cas.adaptors.generic;

import org.apereo.cas.authentication.AuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.handler.support.AbstractUsernamePasswordAuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalFactory;

import lombok.val;

import javax.security.auth.login.FailedLoginException;
import java.util.ArrayList;
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
    private final Set<String> users;

    public RejectUsersAuthenticationHandler(final String name,
                                            final PrincipalFactory principalFactory,
                                            final Set<String> rejectedUsers) {
        super(name, principalFactory, null);
        this.users = rejectedUsers;
    }

    @Override
    protected AuthenticationHandlerExecutionResult authenticateUsernamePasswordInternal(
        final UsernamePasswordCredential credential, final String originalPassword) throws Throwable {
        val username = credential.getUsername();
        if (this.users.contains(username)) {
            throw new FailedLoginException();
        }
        val principal = principalFactory.createPrincipal(username);
        return createHandlerResult(credential, principal, new ArrayList<>());
    }
}
