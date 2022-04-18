package org.apereo.cas.web.flow;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.pac4j.client.DelegatedClientAuthenticationFailureEvaluator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * This is {@link DefaultDelegatedClientAuthenticationFailureEvaluator}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@RequiredArgsConstructor
@Slf4j
public class DefaultDelegatedClientAuthenticationFailureEvaluator implements DelegatedClientAuthenticationFailureEvaluator {
    /**
     * Delegation configuration context.
     */
    protected final DelegatedClientAuthenticationConfigurationContext configContext;

    @Override
    public Optional<ModelAndView> evaluate(final HttpServletRequest request, final int status) {
        val params = request.getParameterMap();
        if (Stream.of("error", "error_code", "error_description", "error_message").anyMatch(params::containsKey)) {
            val model = new HashMap<String, Object>();
            if (params.containsKey("error_code")) {
                model.put("code", StringEscapeUtils.escapeHtml4(request.getParameter("error_code")));
            } else {
                model.put("code", status);
            }
            model.put("error", StringEscapeUtils.escapeHtml4(request.getParameter("error")));
            model.put("reason", StringEscapeUtils.escapeHtml4(request.getParameter("error_reason")));
            if (params.containsKey("error_description")) {
                model.put("description", StringEscapeUtils.escapeHtml4(request.getParameter("error_description")));
            } else if (params.containsKey("error_message")) {
                model.put("description", StringEscapeUtils.escapeHtml4(request.getParameter("error_message")));
            }
            model.put(CasProtocolConstants.PARAMETER_SERVICE, request.getAttribute(CasProtocolConstants.PARAMETER_SERVICE));

            configContext.getDelegatedClientNameExtractor().extract(request)
                .map(StringEscapeUtils::escapeHtml4)
                .ifPresent(name -> model.put("client", name));

            LOGGER.debug("Delegation request has failed. Details are [{}]", model);
            return Optional.of(new ModelAndView(CasWebflowConstants.VIEW_ID_PAC4J_STOP_WEBFLOW, model));
        }
        return Optional.empty();
    }
}
