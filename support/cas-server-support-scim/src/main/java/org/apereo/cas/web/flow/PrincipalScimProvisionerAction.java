package org.apereo.cas.web.flow;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.UsernamePasswordCredential;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.scim.api.ScimProvisioner;
import org.apereo.cas.web.support.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link PrincipalScimProvisionerAction}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class PrincipalScimProvisionerAction extends AbstractAction {
    private static final Logger LOGGER = LoggerFactory.getLogger(PrincipalScimProvisionerAction.class);

    private final ScimProvisioner scimProvisioner;

    public PrincipalScimProvisionerAction(final ScimProvisioner scimProvisioner) {
        this.scimProvisioner = scimProvisioner;
    }

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        final UsernamePasswordCredential c = (UsernamePasswordCredential) WebUtils.getCredential(requestContext);
        if (c == null) {
            LOGGER.debug("No credential found in the request context to provision");
            return success();
        }
        final Authentication authentication = WebUtils.getAuthentication(requestContext);
        if (authentication == null) {
            LOGGER.debug("No authentication found in the request context to provision");
            return success();
        }
        final Principal p = authentication.getPrincipal();
        LOGGER.debug("Starting to provision principal [{}]", p);
        final boolean res = this.scimProvisioner.create(p, c);
        if (res) {
            LOGGER.debug("Provisioning of principal [{}] executed successfully", p);
        } else {
            LOGGER.warn("Provisioning of principal [{}] has failed", p);
        }
        return success();
    }
}
