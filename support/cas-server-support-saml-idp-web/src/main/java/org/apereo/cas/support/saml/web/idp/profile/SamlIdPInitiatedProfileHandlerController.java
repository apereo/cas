package org.apereo.cas.support.saml.web.idp.profile;

import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.support.saml.SamlIdPConstants;
import org.apereo.cas.support.saml.SamlProtocolConstants;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.hc.core5.net.URIBuilder;
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

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

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

    @GetMapping(path = SamlIdPConstants.ENDPOINT_SAML2_IDP_INIT_PROFILE_SSO)
    protected ModelAndView handleIdPInitiatedSsoRequest(final HttpServletResponse response,
                                                        final HttpServletRequest request) throws Exception {
        val providerId = extractProviderId(request);
        val registeredService = verifySamlRegisteredService(providerId);
        val adaptor = getSamlMetadataFacadeFor(registeredService, providerId);
        if (adaptor.isEmpty()) {
            throw new UnauthorizedServiceException(UnauthorizedServiceException.CODE_UNAUTHZ_SERVICE, "Cannot find metadata linked to " + providerId);
        }

        val facade = adaptor.get();
        val shire = extractShire(request, providerId, facade);

        val target = request.getParameter(SamlIdPConstants.TARGET);
        val time = request.getParameter(SamlIdPConstants.TIME);

        val authnRequest = buildAuthnRequest(providerId, shire, time);
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

    protected AuthnRequest buildAuthnRequest(final String providerId, final String shire, final String time) {
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
        return authnRequest;
    }

    protected String extractShire(final HttpServletRequest request, final String providerId,
                                  final SamlRegisteredServiceServiceProviderMetadataFacade facade)
        throws MessageDecodingException {
        var shire = request.getParameter(SamlIdPConstants.SHIRE);
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
        return shire;
    }

    protected String extractProviderId(final HttpServletRequest request) throws MessageDecodingException {
        val providerId = request.getParameter(SamlIdPConstants.PROVIDER_ID);
        if (StringUtils.isBlank(providerId)) {
            LOGGER.warn("No providerId parameter given in unsolicited SSO authentication request.");
            throw new MessageDecodingException("Missing providerId");
        }
        return providerId;
    }
}
