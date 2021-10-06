package org.apereo.cas.support.saml.web.idp.profile;

import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.support.saml.SamlIdPConstants;
import org.apereo.cas.support.saml.SamlProtocolConstants;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.client.utils.URIBuilder;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.messaging.decoder.MessageDecodingException;
import org.opensaml.saml.common.SAMLObjectBuilder;
import org.opensaml.saml.common.binding.SAMLBindingSupport;
import org.opensaml.saml.common.messaging.context.SAMLBindingContext;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.saml.saml2.core.NameIDPolicy;
import org.opensaml.saml.saml2.core.RequestAbstractType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Objects;

/**
 * This is {@link SamlIdPInitiatedProfileHandlerController}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
public class SamlIdPInitiatedProfileHandlerController extends AbstractSamlIdPProfileHandlerController {

    public SamlIdPInitiatedProfileHandlerController(final SamlProfileHandlerConfigurationContext ctx) {
        super(ctx);
    }

    /**
     * Handle idp initiated sso requests.
     * The URL of the response location at the SP (called the "Assertion Consumer Service")
     * but can be omitted in favor of the IdP picking the default endpoint location from metadata.
     *
     * @param response the response
     * @param request  the request
     * @return the model and view
     * @throws Exception the exception
     */
    @GetMapping(path = SamlIdPConstants.ENDPOINT_SAML2_IDP_INIT_PROFILE_SSO)
    protected ModelAndView handleIdPInitiatedSsoRequest(final HttpServletResponse response,
                                                        final HttpServletRequest request) throws Exception {

        val providerId = request.getParameter(SamlIdPConstants.PROVIDER_ID);
        if (StringUtils.isBlank(providerId)) {
            LOGGER.warn("No providerId parameter given in unsolicited SSO authentication request.");
            throw new MessageDecodingException("Missing providerId");
        }

        val registeredService = verifySamlRegisteredService(providerId);
        val adaptor = getSamlMetadataFacadeFor(registeredService, providerId);
        if (adaptor.isEmpty()) {
            throw new UnauthorizedServiceException(UnauthorizedServiceException.CODE_UNAUTHZ_SERVICE, "Cannot find metadata linked to " + providerId);
        }

        var shire = request.getParameter(SamlIdPConstants.SHIRE);
        val facade = adaptor.get();
        if (StringUtils.isBlank(shire)) {
            LOGGER.info("Resolving service provider assertion consumer service URL for [{}] and binding [{}]",
                providerId, SAMLConstants.SAML2_POST_BINDING_URI);
            val acs = facade.getAssertionConsumerService(SAMLConstants.SAML2_POST_BINDING_URI);
            shire = acs != null
                ? StringUtils.isBlank(acs.getResponseLocation()) ? acs.getLocation() : acs.getResponseLocation()
                : null;
        }
        if (StringUtils.isBlank(shire)) {
            LOGGER.warn("Unable to resolve service provider assertion consumer service URL for AuthnRequest construction for entityID: [{}]", providerId);
            throw new MessageDecodingException("Unable to resolve SP ACS URL for AuthnRequest construction");
        }

        val target = request.getParameter(SamlIdPConstants.TARGET);
        val time = request.getParameter(SamlIdPConstants.TIME);

        val builder = (SAMLObjectBuilder) getConfigurationContext()
            .getOpenSamlConfigBean().getBuilderFactory().getBuilder(AuthnRequest.DEFAULT_ELEMENT_NAME);
        val authnRequest = (AuthnRequest) builder.buildObject();
        authnRequest.setAssertionConsumerServiceURL(shire);

        val isBuilder = (SAMLObjectBuilder) getConfigurationContext()
            .getOpenSamlConfigBean().getBuilderFactory().getBuilder(Issuer.DEFAULT_ELEMENT_NAME);
        val issuer = (Issuer) isBuilder.buildObject();
        issuer.setValue(providerId);
        authnRequest.setIssuer(issuer);

        authnRequest.setProtocolBinding(SAMLConstants.SAML2_POST_BINDING_URI);
        val pBuilder = (SAMLObjectBuilder) getConfigurationContext()
            .getOpenSamlConfigBean().getBuilderFactory().getBuilder(NameIDPolicy.DEFAULT_ELEMENT_NAME);
        val nameIDPolicy = (NameIDPolicy) pBuilder.buildObject();
        nameIDPolicy.setAllowCreate(Boolean.TRUE);
        authnRequest.setNameIDPolicy(nameIDPolicy);

        if (NumberUtils.isCreatable(time)) {
            authnRequest.setIssueInstant(Instant.ofEpochMilli(Long.parseLong(time)));
        } else {
            authnRequest.setIssueInstant(ZonedDateTime.now(ZoneOffset.UTC).toInstant());
        }
        authnRequest.setForceAuthn(Boolean.FALSE);
        if (StringUtils.isNotBlank(target)) {
            request.setAttribute(SamlProtocolConstants.PARAMETER_SAML_RELAY_STATE, target);
        }

        val ctx = new MessageContext();
        if (facade.isAuthnRequestsSigned() || registeredService.isSignUnsolicitedAuthnRequest()) {
            getConfigurationContext().getSamlObjectSigner().encode(authnRequest, registeredService,
                facade, response, request, SAMLConstants.SAML2_POST_BINDING_URI, authnRequest, ctx);
        }
        ctx.setMessage(authnRequest);
        val bindingContext = ctx.getSubcontext(SAMLBindingContext.class, true);
        Objects.requireNonNull(bindingContext).setHasBindingSignature(false);
        SAMLBindingSupport.setRelayState(ctx, target);

        val pair = Pair.<RequestAbstractType, MessageContext>of(authnRequest, ctx);
        val modelAndView = initiateAuthenticationRequest(pair, response, request);
        if (modelAndView != null) {
            val view = (RedirectView) modelAndView.getView();
            val urlBuilder = new URIBuilder(Objects.requireNonNull(view).getUrl());
            val paramNames = request.getParameterNames();
            while (paramNames.hasMoreElements()) {
                val parameterName = paramNames.nextElement();
                if (!parameterName.equalsIgnoreCase(SamlIdPConstants.TARGET)
                    && !parameterName.equalsIgnoreCase(SamlIdPConstants.TIME)
                    && !parameterName.equalsIgnoreCase(SamlIdPConstants.SHIRE)
                    && !parameterName.equalsIgnoreCase(SamlIdPConstants.PROVIDER_ID)) {
                    urlBuilder.addParameter(parameterName, request.getParameter(parameterName));
                }
            }
            view.setUrl(urlBuilder.build().toString());
        }
        return modelAndView;
    }
}
