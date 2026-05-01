package org.apereo.cas.support.oauth.web.views;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.web.OAuth20RequestParameterResolver;
import org.apereo.cas.util.DigestUtils;
import org.apereo.cas.util.function.FunctionUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.hc.core5.net.URIBuilder;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.context.session.SessionStore;
import org.springframework.web.servlet.ModelAndView;

/**
 * This is {@link OAuth20ConsentApprovalViewResolver}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class OAuth20ConsentApprovalViewResolver implements ConsentApprovalViewResolver {
    private static final String APPROVAL_KEY_SALT = UUID.randomUUID().toString();

    protected final CasConfigurationProperties casProperties;

    protected final SessionStore sessionStore;

    protected final OAuth20RequestParameterResolver oauthRequestParameterResolver;
    
    @Override
    public ModelAndView resolve(final WebContext context, final OAuthRegisteredService service) throws Exception {
        return isConsentApprovalBypassed(context, service)
            ? new ModelAndView()
            : redirectToApproveView(context, service);
    }

    protected boolean isConsentApprovalBypassed(final WebContext context, final OAuthRegisteredService service) {
        var bypass = service.isBypassApprovalPrompt() || casProperties.getAuthn().getOauth().getCore().isBypassApprovalPrompt();
        if (!bypass) {
            val approvalSubmitted = context.getRequestParameter(OAuth20Constants.BYPASS_APPROVAL_PROMPT)
                .map(BooleanUtils::toBoolean).orElse(Boolean.FALSE);
            if (approvalSubmitted) {
                val approvalKey = context.getRequestParameter(OAuth20Constants.SCOPES_APPROVAL_KEY)
                    .orElseThrow(() -> new IllegalArgumentException("Unknown approval key"));
                val supportedScopes = resolveSupportedScopes(context, service);
                val approvalEntry = buildApprovalEntry(context, service, supportedScopes);
                bypass = Strings.CS.equals(approvalEntry.approvalKey(), approvalKey);
            }
        }
        return bypass;
    }

    protected ModelAndView redirectToApproveView(final WebContext context,
                                                 final OAuthRegisteredService service) throws Exception {
        val callbackUrl = context.getFullRequestURL();
        LOGGER.trace("Requesting URL to call back: [{}]", callbackUrl);

        val url = new URIBuilder(callbackUrl);
        url.addParameter(OAuth20Constants.BYPASS_APPROVAL_PROMPT, Boolean.TRUE.toString());

        val model = new HashMap<String, Object>();
        model.put("service", service);
        FunctionUtils.doIfNotNull(service.getAccessStrategy().getUnauthorizedRedirectUrl(),
            u -> model.put("deniedApprovalUrl", u));

        val supportedScopes = resolveSupportedScopes(context, service);
        val approvalEntry = buildApprovalEntry(context, service, supportedScopes);

        model.put("scopes", supportedScopes);
        model.put("approvalKey", approvalEntry.approvalKey());
        model.put("recordKey", approvalEntry.recordKey());

        url.addParameter(OAuth20Constants.SCOPES_APPROVAL_KEY, approvalEntry.approvalKey());
        model.put("callbackUrl", url.toString());

        prepareApprovalViewModel(model, context, service);
        return getApprovalModelAndView(model);
    }

    protected Set<String> resolveSupportedScopes(final WebContext context, final OAuthRegisteredService service) {
        val supportedScopes = new TreeSet<>(service.getScopes());
        val requestedScopes = oauthRequestParameterResolver.resolveRequestedScopes(context);
        supportedScopes.retainAll(requestedScopes);
        return supportedScopes;
    }

    private ApprovalEntry buildApprovalEntry(final WebContext context,
                                             final OAuthRegisteredService service,
                                             final Set<String> supportedScopes) {
        val profile = OAuth20Utils.getAuthenticatedUserProfile(context, sessionStore);
        val crypto = casProperties.getTgc().getCrypto();
        val secretKey = StringUtils.defaultIfBlank(crypto.getSigning().getKey(), APPROVAL_KEY_SALT)
            + StringUtils.defaultIfBlank(crypto.getEncryption().getKey(), APPROVAL_KEY_SALT);

        val approvalKey = String.format("%s/%s/%s",
            service.getClientId(),
            profile.getId(),
            String.join(",", supportedScopes));
        val encodedApprovalKey = DigestUtils.sha512Hmac(secretKey, approvalKey);
        val recordKey = String.format("%s/%s", service.getClientId(), profile.getId());
        return new ApprovalEntry(DigestUtils.sha512(recordKey), encodedApprovalKey);
    }

    protected ModelAndView getApprovalModelAndView(final Map<String, Object> model) {
        return new ModelAndView(getApprovalViewName(), model);
    }

    protected String getApprovalViewName() {
        return OAuth20Constants.CONFIRM_VIEW;
    }

    protected void prepareApprovalViewModel(final Map<String, Object> model,
                                            final WebContext context,
                                            final OAuthRegisteredService service) throws Exception {
    }

    private record ApprovalEntry(String recordKey, String approvalKey) {
    }
}
