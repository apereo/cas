package org.apereo.cas.util.spring;

import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.CredentialMetadata;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.ticket.TransientSessionTicket;
import org.apereo.cas.util.RandomUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedGrantedAuthoritiesWebAuthenticationDetails;
import jakarta.servlet.http.HttpServletRequest;
import java.io.Serial;
import java.util.stream.Collectors;

/**
 * This is {@link SecurityContextUtils}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@UtilityClass
public class SecurityContextUtils {

    /**
     * Create security context.
     *
     * @param transientTicket the transient ticket
     * @param request         the request
     * @return the security context
     */
    public static SecurityContext createSecurityContext(final TransientSessionTicket transientTicket,
                                                        final HttpServletRequest request) {
        val principal = transientTicket.getProperty(Principal.class.getName(), Principal.class);
        return createSecurityContext(principal, request);
    }

    /**
     * Create security context.
     *
     * @param principal the principal
     * @param request   the request
     * @return the security context
     */
    public static SecurityContext createSecurityContext(final Principal principal, final HttpServletRequest request) {
        val authorities = principal.getAttributes().keySet().stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        val user = new User(principal.getId(), RandomUtils.generateSecureRandomId(), authorities);
        val authenticationToken = new PreAuthenticatedAuthenticationToken(user,
            new SecurityContextCredential(principal.getId()), authorities);
        authenticationToken.setAuthenticated(true);
        authenticationToken.setDetails(new PreAuthenticatedGrantedAuthoritiesWebAuthenticationDetails(request, authorities));
        val context = new SecurityContextImpl(authenticationToken);
        context.setAuthentication(authenticationToken);
        return context;
    }

    @Getter
    @RequiredArgsConstructor
    private static final class SecurityContextCredential implements Credential {

        @Serial
        private static final long serialVersionUID = -6075800625583285084L;

        private final String id;
        private CredentialMetadata credentialMetadata;
    }
}
