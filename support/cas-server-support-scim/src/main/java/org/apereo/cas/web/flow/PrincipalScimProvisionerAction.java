package org.apereo.cas.web.flow;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.api.PrincipalProvisioner;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.web.support.WebUtils;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link PrincipalScimProvisionerAction}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
@RequiredArgsConstructor
public class PrincipalScimProvisionerAction extends AbstractAction {
    private final PrincipalProvisioner scimProvisioner;

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        final Credential c = WebUtils.getCredential(requestContext);
        if (c == null) {
            LOGGER.warn("No credential found in the request context to provision");
            return success();
        }
        final Authentication authentication = WebUtils.getAuthentication(requestContext);
        if (authentication == null) {
            LOGGER.warn("No authentication found in the request context to provision");
            return success();
        }
        final Principal p = authentication.getPrincipal();
        LOGGER.debug("Starting to provision principal [{}]", p);
        final boolean res = this.scimProvisioner.create(authentication, p, c);
        if (res) {
            LOGGER.debug("Provisioning of principal [{}] executed successfully", p);
        } else {
            LOGGER.warn("Provisioning of principal [{}] has failed", p);
        }
        return success();
    }
}
