package org.apereo.cas.adaptors.yubikey.web.flow;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.adaptors.yubikey.YubiKeyAccountRegistry;
import org.apereo.cas.web.support.WebUtils;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import javax.servlet.http.HttpServletRequest;

/**
 * This is {@link YubiKeyAccountSaveRegistrationAction}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class YubiKeyAccountSaveRegistrationAction extends AbstractAction {
    private final YubiKeyAccountRegistry registry;

    public YubiKeyAccountSaveRegistrationAction(final YubiKeyAccountRegistry registry) {
        this.registry = registry;
    }

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        final String uid = WebUtils.getAuthentication(requestContext).getPrincipal().getId();
        final HttpServletRequest request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
        final String token = request.getParameter("token");
        if (StringUtils.isNotBlank(token) && registry.registerAccountFor(uid, token)) {
            return success();
        }
        return error();
    }
}
