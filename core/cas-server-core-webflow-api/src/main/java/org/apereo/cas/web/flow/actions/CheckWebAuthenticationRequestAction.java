package org.apereo.cas.web.flow.actions;

import org.apereo.cas.util.RegexUtils;
import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link CheckWebAuthenticationRequestAction}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class CheckWebAuthenticationRequestAction extends BaseCasWebflowAction {
    private final String contentType;

    @Override
    protected Event doExecuteInternal(final RequestContext context) {
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(context);
        LOGGER.trace("Checking request content type [{}] against [{}]", request.getContentType(), this.contentType);
        val requestContentType = request.getContentType();
        if (StringUtils.isNotBlank(requestContentType) && RegexUtils.find(this.contentType, requestContentType)) {
            LOGGER.debug("Authentication request via type [{}] is not web-based", this.contentType);
            return eventFactory.no(this);
        }
        LOGGER.debug("Authenticated request is identified as web-based via type [{}]", requestContentType);
        return eventFactory.yes(this);
    }
}
