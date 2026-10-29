package org.apereo.cas.support.oauth.web.views;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.web.OAuth20RequestParameterResolver;
import org.apereo.cas.util.DigestUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.http.HttpRequestUtils;
import org.apereo.cas.web.CasSessionStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.hc.core5.net.URIBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.web.servlet.ModelAndView;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

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

    protected final CasSessionStore sessionStore;

    protected final OAuth20RequestParameterResolver oauthRequestParameterResolver;
    
    @Override
    public ModelAndView resolve(final HttpServletRequest request, final HttpServletResponse response,
                                final OAuthRegisteredService service)
            throws Exception {
        return isConsentApprovalBypassed(request, response, service)
            ? new ModelAndView()
            : redirectToApproveView(request, response, service);
    }

    protected boolean isConsentApprovalBypassed(final HttpServletRequest request, final HttpServletResponse response,
                                                final OAuthRegisteredService service) {
        val bypass = service.isBypassApprovalPrompt() || casProperties.getAuthn().getOauth().getCore().isBypassApprovalPrompt();
        return bypass || isConsentApprovalSubmitted(request, response, service);
    }

    /**
     * Whether the request carries the approval the end-user just gave on the consent view,
     * that is the approval flag together with the approval key computed for this client, user and scopes.
     *
     * @param request  the request
     * @param response the response
     * @param service  the service
     * @return true if consent was submitted for this request
     */
    protected boolean isConsentApprovalSubmitted(final HttpServletRequest request, final HttpServletResponse response,
                                                 final OAuthRegisteredService service) {
        val approvalSubmitted = Optional.ofNullable(request.getParameter(OAuth20Constants.BYPASS_APPROVAL_PROMPT))
            .map(BooleanUtils::toBoolean).orElse(Boolean.FALSE);
        if (approvalSubmitted) {
            val approvalKey = Optional.ofNullable(request.getParameter(OAuth20Constants.SCOPES_APPROVAL_KEY))
                .orElseThrow(() -> new IllegalArgumentException("Unknown approval key"));
            val supportedScopes = resolveSupportedScopes(request, service);
            val approvalEntry = buildApprovalEntry(request, response, service, supportedScopes, resolveRequestedResources(request));
            return Strings.CS.equals(approvalEntry.approvalKey(), approvalKey);
        }
        return false;
    }

    /**
     * Show the consent view, whose approval returns to the authorization request with the approval flag and key. The request
     * is rebuilt with every parameter it carries: the query is read as a browser form-encodes it, with {@code +} as a space,
     * so scopes and state come back unchanged, and a {@code POST} keeps the parameters of its body. An approval flag and key
     * that the request already carries are replaced, so a stale key is never read back instead of the new one.
     *
     * @param request  the request
     * @param response the response
     * @param service  the service
     * @return the consent view
     * @throws Exception the exception
     */
    protected ModelAndView redirectToApproveView(final HttpServletRequest request,
                                                 final HttpServletResponse response,
                                                 final OAuthRegisteredService service)
            throws Exception {
        val url = buildApprovalCallbackUrl(request);
        LOGGER.trace("Requesting URL to call back: [{}]", url);
        url.setParameter(OAuth20Constants.BYPASS_APPROVAL_PROMPT, Boolean.TRUE.toString());

        val model = new HashMap<String, Object>();
        model.put("service", service);
        FunctionUtils.doIfNotNull(service.getAccessStrategy().getUnauthorizedRedirectUrl(),
            u -> model.put("deniedApprovalUrl", u));

        val supportedScopes = resolveSupportedScopes(request, service);
        val resources = resolveRequestedResources(request);
        val approvalEntry = buildApprovalEntry(request, response, service, supportedScopes, resources);

        model.put("scopes", supportedScopes);
        model.put("resources", resources);
        model.put("approvalKey", approvalEntry.approvalKey());
        model.put("recordKey", approvalEntry.recordKey());

        url.setParameter(OAuth20Constants.SCOPES_APPROVAL_KEY, approvalEntry.approvalKey());
        model.put("callbackUrl", url.toString());

        prepareApprovalViewModel(model, request, response, service);
        return getApprovalModelAndView(model);
    }

    private static URIBuilder buildApprovalCallbackUrl(final HttpServletRequest request) throws URISyntaxException {
        if (HttpMethod.POST.matches(request.getMethod())) {
            val url = new URIBuilder(request.getRequestURL().toString());
            request.getParameterMap().forEach((name, values) -> Arrays.stream(values).forEach(value -> url.addParameter(name, value)));
            return url;
        }
        return new URIBuilder(HttpRequestUtils.getFullRequestUrl(request)).setPlusAsBlank(true);
    }

    protected Set<String> resolveSupportedScopes(final HttpServletRequest request, final OAuthRegisteredService service) {
        val supportedScopes = new TreeSet<>(service.getScopes());
        val requestedScopes = oauthRequestParameterResolver.resolveRequestedScopes(request);
        supportedScopes.retainAll(requestedScopes);
        return supportedScopes;
    }

    /**
     * Resources requested with the authorization request (RFC 8707). They are shown on the consent view and are part of
     * the approval, so a decision remembered for some resources does not approve a request for others.
     *
     * @param request the request
     * @return the requested resources
     */
    protected Set<String> resolveRequestedResources(final HttpServletRequest request) {
        return new TreeSet<>(oauthRequestParameterResolver.resolveRequestedResources(request));
    }

    /**
     * The approval key covers the client, the user, the scopes and the requested resources; without resources, the key
     * is the one issued before resources were part of it, so decisions remembered earlier still apply.
     *
     * @param request         the request
     * @param response        the response
     * @param service         the service
     * @param supportedScopes the scopes
     * @param resources       the requested resources
     * @return the approval entry
     */
    private ApprovalEntry buildApprovalEntry(final HttpServletRequest request,
                                             final HttpServletResponse response,
                                             final OAuthRegisteredService service,
                                             final Set<String> supportedScopes,
                                             final Set<String> resources) {
        val profile = OAuth20Utils.getAuthenticatedUserProfile(request, response, sessionStore);
        val crypto = casProperties.getTgc().getCrypto();
        val secretKey = StringUtils.defaultIfBlank(crypto.getSigning().getKey(), APPROVAL_KEY_SALT)
            + StringUtils.defaultIfBlank(crypto.getEncryption().getKey(), APPROVAL_KEY_SALT);

        val scopesKey = String.format("%s/%s/%s",
            service.getClientId(),
            profile.getId(),
            String.join(",", supportedScopes));
        val approvalKey = resources.isEmpty() ? scopesKey : scopesKey + '/' + String.join(",", resources);
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
                                            final HttpServletRequest request,
                                            final HttpServletResponse response,
                                            final OAuthRegisteredService service)
            throws Exception {
    }

    private record ApprovalEntry(String recordKey, String approvalKey) {
    }
}
