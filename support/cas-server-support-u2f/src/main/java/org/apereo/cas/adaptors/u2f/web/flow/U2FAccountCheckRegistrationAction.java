package org.apereo.cas.adaptors.u2f.web.flow;

import org.apereo.cas.adaptors.u2f.storage.U2FDeviceRegistrationRepository;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.web.support.WebUtils;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link U2FAccountCheckRegistrationAction}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class U2FAccountCheckRegistrationAction extends AbstractAction {
    private final U2FDeviceRegistrationRepository u2FDeviceRegistrationRepository;

    public U2FAccountCheckRegistrationAction(final U2FDeviceRegistrationRepository u2FDeviceRegistrationRepository) {
        this.u2FDeviceRegistrationRepository = u2FDeviceRegistrationRepository;
    }

    @Override
    protected Event doExecute(final RequestContext requestContext) throws Exception {
        final Principal p = WebUtils.getAuthentication(requestContext).getPrincipal();
        if (u2FDeviceRegistrationRepository.isRegistered(p.getId())) {
            return success();
        }
        return new EventFactorySupport().event(this, "register");
    }
}
