package org.apereo.cas.trusted.web.flow;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.trusted.MultifactorAuthenticationTrustUtils;
import org.apereo.cas.trusted.authentication.MultifactorAuthenticationTrustRecord;
import org.apereo.cas.trusted.authentication.MultifactorAuthenticationTrustStorage;
import org.apereo.cas.web.support.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link MultifactorAuthenticationSetTrustAction}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class MultifactorAuthenticationSetTrustAction extends AbstractAction {
    private static final Logger LOGGER = LoggerFactory.getLogger(MultifactorAuthenticationSetTrustAction.class);

    private MultifactorAuthenticationTrustStorage storage;

    @Override
    protected Event doExecute(final RequestContext requestContext) throws Exception {
        final Authentication c = WebUtils.getAuthentication(requestContext);
        if (c == null) {
            LOGGER.error("Could not determine authentication from the request context");
            return error();
        }
        final String principal = c.getPrincipal().getId();
        LOGGER.debug("Attempt to store trusted authentication record for {}", principal);
        final MultifactorAuthenticationTrustRecord record = MultifactorAuthenticationTrustRecord.newInstance(principal,
                MultifactorAuthenticationTrustUtils.generateGeography());
        storage.set(record);
        LOGGER.debug("Saved trusted authentication record for {}", principal);
        return success();
    }

    public void setStorage(final MultifactorAuthenticationTrustStorage storage) {
        this.storage = storage;
    }
}
