package org.apereo.cas.adaptors.yubikey.web.flow;

import org.apereo.cas.adaptors.yubikey.YubiKeyAccountRegistry;
import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link YubiKeyAccountSaveRegistrationAction}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiredArgsConstructor
public class YubiKeyAccountSaveRegistrationAction extends AbstractAction {
    private final YubiKeyAccountRegistry registry;

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        val uid = WebUtils.getAuthentication(requestContext).getPrincipal().getId();
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
        val token = request.getParameter("token");
        if (StringUtils.isNotBlank(token) && registry.registerAccountFor(uid, token)) {
            return success();
        }
        return error();
    }
}
