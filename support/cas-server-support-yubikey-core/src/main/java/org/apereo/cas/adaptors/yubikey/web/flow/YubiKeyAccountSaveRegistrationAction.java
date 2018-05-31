package org.apereo.cas.adaptors.yubikey.web.flow;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.adaptors.yubikey.YubiKeyAccountRegistry;
import org.apereo.cas.web.support.WebUtils;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link YubiKeyAccountSaveRegistrationAction}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
@RequiredArgsConstructor
public class YubiKeyAccountSaveRegistrationAction extends AbstractAction {
    private final YubiKeyAccountRegistry registry;

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        final var uid = WebUtils.getAuthentication(requestContext).getPrincipal().getId();
        final var request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
        final var token = request.getParameter("token");
        if (StringUtils.isNotBlank(token) && registry.registerAccountFor(uid, token)) {
            return success();
        }
        return error();
    }
}
