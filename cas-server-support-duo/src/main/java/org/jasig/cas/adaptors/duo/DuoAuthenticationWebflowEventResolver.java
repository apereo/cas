package org.jasig.cas.adaptors.duo;

import com.google.common.collect.ImmutableSet;
import org.jasig.cas.authentication.AuthenticationResultBuilder;
import org.jasig.cas.authentication.Credential;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.web.flow.authentication.AbstractCasWebflowEventResolver;
import org.jasig.cas.web.support.WebUtils;
import org.springframework.stereotype.Component;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.Set;

/**
 * This is {@link DuoAuthenticationWebflowEventResolver }.
 *
 * @author Misagh Moayyed
 * @since 4.3.0
 */
@Component("duoAuthenticationWebflowEventResolver")
public class DuoAuthenticationWebflowEventResolver extends AbstractCasWebflowEventResolver {

    @Override
    protected Set<Event> resolveInternal(final RequestContext requestContext) {
        try {
            final Credential credential = getCredentialFromContext(requestContext);
            AuthenticationResultBuilder builder = WebUtils.getAuthenticationResultBuilder(requestContext);
            builder = this.authenticationSystemSupport.handleAuthenticationTransaction(builder, credential);
            final Service service = WebUtils.getService(requestContext);

            return ImmutableSet.of(grantTicketGrantingTicketToAuthenticationResult(requestContext, builder, service));
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
            return ImmutableSet.of(new Event(this, "error"));
        }
    }
}

