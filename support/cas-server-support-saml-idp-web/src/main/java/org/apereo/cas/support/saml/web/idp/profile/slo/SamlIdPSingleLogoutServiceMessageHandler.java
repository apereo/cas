package org.apereo.cas.support.saml.web.idp.profile.slo;

import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.logout.LogoutHttpMessage;
import org.apereo.cas.logout.slo.BaseSingleLogoutServiceMessageHandler;
import org.apereo.cas.logout.slo.SingleLogoutMessage;
import org.apereo.cas.logout.slo.SingleLogoutMessageCreator;
import org.apereo.cas.logout.slo.SingleLogoutRequest;
import org.apereo.cas.logout.slo.SingleLogoutServiceLogoutUrlBuilder;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.cache.SamlRegisteredServiceCachingMetadataResolver;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.HttpUtils;
import org.apereo.cas.util.http.HttpClient;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.shibboleth.utilities.java.support.xml.SerializeSupport;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.velocity.app.VelocityEngine;
import org.opensaml.core.xml.util.XMLObjectSupport;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.core.LogoutRequest;
import org.springframework.http.HttpStatus;

import java.nio.charset.StandardCharsets;

/**
 * This is {@link SamlIdPSingleLogoutServiceMessageHandler}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Slf4j
@Getter
public class SamlIdPSingleLogoutServiceMessageHandler extends BaseSingleLogoutServiceMessageHandler {
    /**
     * The Saml registered service caching metadata resolver.
     */
    protected final SamlRegisteredServiceCachingMetadataResolver samlRegisteredServiceCachingMetadataResolver;

    /**
     * The velocity engine used to render logout messages.
     */
    protected final VelocityEngine velocityEngineFactory;

    public SamlIdPSingleLogoutServiceMessageHandler(final HttpClient httpClient,
                                                    final SingleLogoutMessageCreator logoutMessageBuilder,
                                                    final ServicesManager servicesManager,
                                                    final SingleLogoutServiceLogoutUrlBuilder singleLogoutServiceLogoutUrlBuilder,
                                                    final boolean asynchronous,
                                                    final AuthenticationServiceSelectionPlan authenticationRequestServiceSelectionStrategies,
                                                    final SamlRegisteredServiceCachingMetadataResolver samlRegisteredServiceCachingMetadataResolver,
                                                    final VelocityEngine velocityEngineFactory) {
        super(httpClient, logoutMessageBuilder, servicesManager,
            singleLogoutServiceLogoutUrlBuilder, asynchronous,
            authenticationRequestServiceSelectionStrategies);
        this.samlRegisteredServiceCachingMetadataResolver = samlRegisteredServiceCachingMetadataResolver;
        this.velocityEngineFactory = velocityEngineFactory;
    }

    @Override
    protected boolean supportsInternal(final WebApplicationService singleLogoutService, final RegisteredService registeredService) {
        return registeredService instanceof SamlRegisteredService;
    }

    @Override
    public int getOrder() {
        return 0;
    }

    @Override
    protected boolean sendMessageToEndpoint(final LogoutHttpMessage msg, final SingleLogoutRequest request, final SingleLogoutMessage logoutMessage) {
        val binding = request.getProperties().get(SamlIdPSingleLogoutServiceLogoutUrlBuilder.PROPERTY_NAME_SINGLE_LOGOUT_BINDING);

        if (SAMLConstants.SAML2_SOAP11_BINDING_URI.equalsIgnoreCase(binding)) {
            return super.sendMessageToEndpoint(msg, request, logoutMessage);
        }

        HttpResponse response = null;
        try {
            val logoutRequest = (LogoutRequest) logoutMessage.getMessage();
            LOGGER.trace("Sending logout response for binding [{}]", binding);
            if (SAMLConstants.SAML2_REDIRECT_BINDING_URI.equalsIgnoreCase(binding)) {
                val encoder = new SamlIdPHttpRedirectDeflateEncoder(msg.getUrl().toExternalForm(), logoutRequest);
                encoder.doEncode();
                val redirectUrl = encoder.getRedirectUrl();
                LOGGER.trace("Final logout redirect URL is [{}]", redirectUrl);
                response = HttpUtils.executeGet(redirectUrl);
            } else {
                val payload = SerializeSupport.nodeToString(XMLObjectSupport.marshall(logoutRequest));
                LOGGER.trace("Logout request payload is [{}]", payload);

                val message = EncodingUtils.encodeBase64(payload.getBytes(StandardCharsets.UTF_8), false);
                LOGGER.trace("Logout message encoded in base64 is [{}]", message);

                response = HttpUtils.executePost(msg.getUrl().toExternalForm(), CollectionUtils.wrap("SAMLRequest", message),
                    CollectionUtils.wrap("Content-Type", msg.getContentType()));
            }
            if (response != null && response.getStatusLine().getStatusCode() == HttpStatus.OK.value()) {
                val result = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
                LOGGER.trace("Received logout response as [{}]", result);
                return true;
            }
        } catch (final Exception e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.error(e.getMessage(), e);
            } else {
                LOGGER.error(e.getMessage());
            }
        } finally {
            HttpUtils.close(response);
        }
        LOGGER.warn("No (successful) logout response received from the url [{}]", msg.getUrl().toExternalForm());
        return false;
    }
}
