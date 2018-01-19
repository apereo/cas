package org.apereo.cas.web.flow.actions;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.web.support.WebUtils;
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
@Slf4j
@AllArgsConstructor
public class CheckWebAuthenticationRequestAction extends AbstractAction {
    private final String contentType;

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
