package org.apereo.cas.web.flow.action;

import org.apereo.cas.authentication.SurrogatePrincipalBuilder;
import org.apereo.cas.authentication.SurrogateUsernamePasswordCredential;
import org.apereo.cas.authentication.UsernamePasswordCredential;
import org.apereo.cas.authentication.surrogate.SurrogateAuthenticationService;
import org.apereo.cas.web.flow.SurrogateWebflowConfigurer;
import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link LoadSurrogatesListAction}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
@RequiredArgsConstructor
public class LoadSurrogatesListAction extends AbstractAction {

    private final SurrogateAuthenticationService surrogateService;
    private final SurrogatePrincipalBuilder surrogatePrincipalBuilder;

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        if (WebUtils.hasRequestSurrogateAuthenticationRequest(requestContext)) {
            WebUtils.removeRequestSurrogateAuthenticationRequest(requestContext);
            LOGGER.debug("Attempting to load surrogates...");
            if (loadSurrogates(requestContext)) {
                return new Event(this, SurrogateWebflowConfigurer.VIEW_ID_SURROGATE_VIEW);
            }
            return error();
        }

        val c = WebUtils.getCredential(requestContext);
        if (c instanceof SurrogateUsernamePasswordCredential) {
            val authenticationResultBuilder = WebUtils.getAuthenticationResultBuilder(requestContext);
            val credential = (SurrogateUsernamePasswordCredential) c;
            val result =
                surrogatePrincipalBuilder.buildSurrogateAuthenticationResult(authenticationResultBuilder, c, credential.getSurrogateUsername());
            if (result.isPresent()) {
                WebUtils.putAuthenticationResultBuilder(result.get(), requestContext);
            }
        }
        return success();
    }

    private boolean loadSurrogates(final RequestContext requestContext) {
        val c = WebUtils.getCredential(requestContext);
        if (c instanceof UsernamePasswordCredential) {
            val username = c.getId();
            LOGGER.debug("Loading eligible accounts for [{}] to proxy", username);
            val surrogates = surrogateService.getEligibleAccountsForSurrogateToProxy(username);
            LOGGER.debug("Surrogate accounts found are [{}]", surrogates);
            if (surrogates != null && !surrogates.isEmpty()) {
                surrogates.add(0, username);
                WebUtils.putSurrogateAuthenticationAccounts(requestContext, surrogates);
                return true;
            }
            LOGGER.debug("No surrogate accounts could be located for [{}]", username);
        } else {
            LOGGER.debug("Current credential in the webflow is not one of [{}]", UsernamePasswordCredential.class.getName());
        }
        return false;
    }
}
