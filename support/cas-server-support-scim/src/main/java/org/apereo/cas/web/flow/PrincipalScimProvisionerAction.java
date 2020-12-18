package org.apereo.cas.web.flow;

import org.apereo.cas.api.PrincipalProvisioner;
import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
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
        val credential = WebUtils.getCredential(requestContext);
        if (credential == null) {
            LOGGER.warn("No credential found in the request context to provision");
            return success();
        }
        val authentication = WebUtils.getAuthentication(requestContext);
        if (authentication == null) {
            LOGGER.warn("No authentication found in the request context to provision");
            return success();
        }
        val principal = authentication.getPrincipal();
        val registeredService = WebUtils.getRegisteredService(requestContext);
        LOGGER.debug("Starting to provision principal [{}] with registered service [{}]", principal, registeredService);
        val res = scimProvisioner.create(authentication, credential, registeredService);
        val msg = String.format("Provisioning of principal %s is%s done successfully", principal,
            BooleanUtils.toString(res, StringUtils.EMPTY, " not"));
        LOGGER.debug(msg);
        return success();
    }
}
