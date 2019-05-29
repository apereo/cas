package org.apereo.cas.support.saml.web.idp.profile.builders.response;

import org.apereo.cas.support.saml.SamlIdPUtils;
import org.apereo.cas.support.saml.SamlUtils;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;
import org.apereo.cas.support.saml.web.idp.profile.builders.enc.encoder.sso.SamlResponsePostEncoder;
import org.apereo.cas.support.saml.web.idp.profile.builders.slo.SamlLogoutResponseBuilderConfigurationContext;
import org.apereo.cas.util.RandomUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.saml.saml2.core.LogoutRequest;
import org.opensaml.saml.saml2.core.LogoutResponse;
import org.opensaml.saml.saml2.core.StatusCode;
import org.opensaml.saml.saml2.core.impl.LogoutRequestImpl;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The {@link SamlLogoutResponseBuilder}.
 *
 * @author Krzysztof Zych
 * @since 6.1.0
 */
@Slf4j
public class SamlLogoutResponseBuilder extends BaseSamlLogoutResponseBuilder<LogoutResponse> {
    public SamlLogoutResponseBuilder(final SamlLogoutResponseBuilderConfigurationContext samlLogoutResponseBuilderConfigurationContext) {
        super(samlLogoutResponseBuilderConfigurationContext);
    }

    @Override
    protected LogoutResponse buildResponse(final HttpServletRequest request,
                                           final HttpServletResponse response,
                                           final SamlRegisteredService samlService,
                                           final SamlRegisteredServiceServiceProviderMetadataFacade adaptor,
                                           final String binding,
                                           final MessageContext messageContext) {
        val logoutRequest = (LogoutRequestImpl) messageContext.getMessage();
        val id = '_' + String.valueOf(RandomUtils.nextLong());
        val samlIdPProperties = getSamlLogoutResponseBuilderConfigurationContext().getCasProperties().getAuthn().getSamlIdp();

        val skewAllowance = samlService.getSkewAllowance() > 0
                ? samlService.getSkewAllowance()
                : samlIdPProperties.getResponse().getSkewAllowance();

        val issueInstant = DateTime.now(DateTimeZone.UTC).plusSeconds(skewAllowance);
        val destination = SamlIdPUtils.determineEndpointForRequest(logoutRequest, adaptor, binding).getLocation();
        val status = newStatus(StatusCode.SUCCESS, null);
        val samlResponse = newLogoutResponse(id, issueInstant, destination, logoutRequest.getID(), status);

        if (StringUtils.isBlank(samlService.getIssuerEntityId())) {
            samlResponse.setIssuer(buildSamlResponseIssuer(getSamlLogoutResponseBuilderConfigurationContext().getCasProperties()
                    .getAuthn().getSamlIdp().getEntityId()));
        } else {
            samlResponse.setIssuer(buildSamlResponseIssuer(samlService.getIssuerEntityId()));
        }

        if (samlService.isSignResponses()) {
            LOGGER.debug("SAML entity id [{}] indicates that SAML responses should be signed", adaptor.getEntityId());
            val samlResponseSigned = getSamlLogoutResponseBuilderConfigurationContext().getSamlObjectSigner()
                    .encode(samlResponse, samlService, adaptor, response, request, binding, logoutRequest);
            SamlUtils.logSamlObject(openSamlConfigBean, samlResponseSigned);
            return samlResponseSigned;
        }

        return samlResponse;
    }

    @Override
    protected LogoutResponse encode(final LogoutResponse samlResponse,
                                    final HttpServletResponse httpResponse,
                                    final HttpServletRequest httpRequest,
                                    final SamlRegisteredServiceServiceProviderMetadataFacade adaptor,
                                    final String relayState,
                                    final String binding,
                                    final LogoutRequest logoutRequest) {
        LOGGER.trace("Constructing encoder based on binding [{}] for [{}]", binding, adaptor.getEntityId());
        val encoder = new SamlResponsePostEncoder<LogoutResponse>(getSamlLogoutResponseBuilderConfigurationContext().getVelocityEngineFactory(), adaptor, httpResponse, httpRequest);
        return encoder.encode(logoutRequest, samlResponse, relayState);

    }
}
