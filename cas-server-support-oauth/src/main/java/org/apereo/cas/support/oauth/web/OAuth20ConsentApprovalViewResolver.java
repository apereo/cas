package org.apereo.cas.support.oauth.web;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.support.oauth.OAuthConstants;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.pac4j.core.context.J2EContext;
import org.pac4j.core.util.CommonHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.Map;

/**
 * This is {@link OAuth20ConsentApprovalViewResolver}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class OAuth20ConsentApprovalViewResolver implements ConsentApprovalViewResolver {
    private static final Logger LOGGER = LoggerFactory.getLogger(OAuth20ConsentApprovalViewResolver.class);

    @Override
    public ModelAndView resolve(final J2EContext context, final OAuthRegisteredService service) {

        final String bypassApprovalParameter = context.getRequestParameter(OAuthConstants.BYPASS_APPROVAL_PROMPT);
        LOGGER.debug("bypassApprovalParameter: {}", bypassApprovalParameter);

        /**
         * Inbound request; approval handled already.
         */
        if (StringUtils.isNotBlank(bypassApprovalParameter) || isConsentApprovalBypassed(context, service)) {
            return new ModelAndView();
        }
        return redirectToApproveView(context, service);
    }

    /**
     * Is consent approval bypassed?
     *
     * @param context the context
     * @param service the service
     * @return true/false
     */
    protected boolean isConsentApprovalBypassed(final J2EContext context, final OAuthRegisteredService service) {
        return service.isBypassApprovalPrompt();
    }
    
    /**
     * Redirect to approve view model and view.
     *
     * @param ctx the ctx
     * @param svc the svc
     * @return the model and view
     */
    protected ModelAndView redirectToApproveView(final J2EContext ctx, final OAuthRegisteredService svc) {
        String callbackUrl = ctx.getFullRequestURL();
        callbackUrl = CommonHelper.addParameter(callbackUrl, OAuthConstants.BYPASS_APPROVAL_PROMPT, "true");
        final Map<String, Object> model = new HashMap<>();
        model.put("callbackUrl", callbackUrl);
        model.put("serviceName", svc.getName());
        LOGGER.debug("callbackUrl: {}", callbackUrl);
        return new ModelAndView(OAuthConstants.CONFIRM_VIEW, model);
    }
}
