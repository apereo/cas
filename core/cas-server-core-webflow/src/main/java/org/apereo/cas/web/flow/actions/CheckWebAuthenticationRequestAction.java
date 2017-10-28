package org.apereo.cas.web.flow.actions;

import org.apereo.cas.web.support.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import javax.servlet.http.HttpServletRequest;

/**
 * This is {@link CheckWebAuthenticationRequestAction}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class CheckWebAuthenticationRequestAction extends AbstractAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(CheckWebAuthenticationRequestAction.class);

    private final String contentType;

    public CheckWebAuthenticationRequestAction(final String contentType) {
        this.contentType = contentType;
    }

    @Override
    protected Event doExecute(final RequestContext context) {
        final HttpServletRequest request = WebUtils.getHttpServletRequestFromExternalWebflowContext(context);

        LOGGER.debug("Checking request content type [{}] against [{}]", request.getContentType(), this.contentType);
        if (this.contentType.equalsIgnoreCase(request.getContentType())) {
            LOGGER.debug("Authentication request via type [{}] is not web-based", this.contentType);
            return new EventFactorySupport().no(this);
        }

        LOGGER.debug("Authenticated request is identified as web-based via type [{}]", request.getContentType());
        return new EventFactorySupport().yes(this);
    }
}
