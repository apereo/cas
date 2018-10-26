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
import org.apereo.cas.util.http.HttpClient;

import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.shibboleth.utilities.java.support.xml.SerializeSupport;
import org.apache.velocity.app.VelocityEngine;
import org.opensaml.core.xml.util.XMLObjectSupport;
import org.opensaml.saml.saml2.core.LogoutRequest;

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
    @SneakyThrows
    protected LogoutHttpMessage getLogoutHttpMessageToSend(final SingleLogoutRequest request, final SingleLogoutMessage logoutMessage) {
        val samlMessage = (LogoutRequest) logoutMessage.getMessage();
        val payload = SerializeSupport.nodeToString(XMLObjectSupport.marshall(samlMessage));
        return new SamlLogoutHttpMessage(request.getLogoutUrl(), payload, isAsynchronous());
    }
}
