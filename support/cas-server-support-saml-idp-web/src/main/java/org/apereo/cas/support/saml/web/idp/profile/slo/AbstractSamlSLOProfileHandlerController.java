package org.apereo.cas.support.saml.web.idp.profile.slo;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.audit.AuditableContext;
import org.apereo.cas.logout.slo.SingleLogoutUrl;
import org.apereo.cas.support.saml.SamlIdPUtils;
import org.apereo.cas.support.saml.SamlProtocolConstants;
import org.apereo.cas.support.saml.SamlUtils;
import org.apereo.cas.support.saml.idp.slo.SamlIdPProfileSingleLogoutRequestProcessor;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceMetadataAdaptor;
import org.apereo.cas.support.saml.web.idp.profile.AbstractSamlIdPProfileHandlerController;
import org.apereo.cas.support.saml.web.idp.profile.SamlProfileHandlerConfigurationContext;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.web.support.WebUtils;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;
import org.jooq.lambda.Unchecked;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.messaging.decoder.servlet.BaseHttpServletRequestXMLMessageDecoder;
import org.opensaml.saml.common.SAMLException;
import org.opensaml.saml.common.SAMLObjectBuilder;
import org.opensaml.saml.common.SignableSAMLObject;
import org.opensaml.saml.common.binding.SAMLBindingSupport;
import org.opensaml.saml.ext.saml2mdreqinit.RequestInitiator;
import org.opensaml.saml.saml2.core.Extensions;
import org.opensaml.saml.saml2.core.LogoutRequest;
import org.opensaml.saml.saml2.core.LogoutResponse;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * This is {@link AbstractSamlSLOProfileHandlerController}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
public abstract class AbstractSamlSLOProfileHandlerController extends AbstractSamlIdPProfileHandlerController {

    protected AbstractSamlSLOProfileHandlerController(final SamlProfileHandlerConfigurationContext context) {
        super(context);
    }

    private void handleLogoutResponse(final Pair<? extends SignableSAMLObject, MessageContext> pair) {
        val logoutResponse = (LogoutResponse) pair.getKey();
        LOGGER.debug("Received logout response from [{}]", SamlIdPUtils.getIssuerFromSamlObject(logoutResponse.getIssuer()));
        getConfigurationContext().getOpenSamlConfigBean().logObject(logoutResponse);
    }

    private void handleLogoutRequest(final HttpServletResponse response, final HttpServletRequest request,
                                     final Pair<? extends SignableSAMLObject, MessageContext> pair,
                                     final String logoutRequestBinding) throws Throwable {
        val logoutRequest = (LogoutRequest) pair.getKey();
        val messageContext = pair.getValue();

        val entityId = SamlIdPUtils.getIssuerFromSamlObject(logoutRequest);
        LOGGER.trace("SAML logout request from entity id [{}] is signed", entityId);

        val service = configurationContext.getWebApplicationServiceFactory().createService(entityId);
        service.getAttributes().put(SamlProtocolConstants.PARAMETER_ENTITY_ID, CollectionUtils.wrapList(entityId));
        val registeredService = configurationContext.getServicesManager().findServiceBy(service, SamlRegisteredService.class);
        val audit = AuditableContext.builder()
            .service(service)
            .registeredService(registeredService)
            .httpRequest(request)
            .httpResponse(response)
            .build();
        val accessResult = configurationContext.getRegisteredServiceAccessStrategyEnforcer().execute(audit);
        accessResult.throwExceptionIfNeeded();
        LOGGER.trace("SAML registered service tied to [{}] is [{}]", entityId, registeredService);

        ensureLogoutRequestIsSignedIfNecessary(registeredService, messageContext);

        val facade = SamlRegisteredServiceMetadataAdaptor.get(
            configurationContext.getSamlRegisteredServiceCachingMetadataResolver(), registeredService, entityId).orElseThrow();
        if (SAMLBindingSupport.isMessageSigned(messageContext)) {
            LOGGER.trace("Verifying signature on the SAML logout request for [{}]", entityId);
            configurationContext.getSamlObjectSignatureValidator()
                .verifySamlProfileRequest(logoutRequest, facade, request, messageContext);
        }
        configurationContext.getOpenSamlConfigBean().logObject(logoutRequest);

        val logoutUrls = SingleLogoutUrl.from(registeredService);
        if (!logoutUrls.isEmpty()) {
            val destination = logoutUrls.getFirst().getUrl();
            WebUtils.putLogoutRedirectUrl(request, destination);
        }

        WebUtils.putRegisteredService(request, registeredService);

        val extensions = buildSamlObject(Extensions.DEFAULT_ELEMENT_NAME, Extensions.class);
        val bindingAttribute = buildSamlObject(RequestInitiator.DEFAULT_ELEMENT_NAME, RequestInitiator.class);
        bindingAttribute.setBinding(logoutRequestBinding);
        extensions.getUnknownXMLObjects().add(bindingAttribute);
        logoutRequest.setExtensions(extensions);

        try (val writer = SamlUtils.transformSamlObject(configurationContext.getOpenSamlConfigBean(), logoutRequest)) {
            val encodedRequest = EncodingUtils.encodeBase64(writer.toString().getBytes(StandardCharsets.UTF_8));
            WebUtils.putSingleLogoutRequest(request, encodedRequest);
        }

        configurationContext.getApplicationContext()
            .getBeansOfType(SamlIdPProfileSingleLogoutRequestProcessor.class)
            .values()
            .stream()
            .filter(BeanSupplier::isNotProxy)
            .sorted(AnnotationAwareOrderComparator.INSTANCE)
            .filter(processor -> processor.supports(request, response, logoutRequest, messageContext))
            .forEach(Unchecked.consumer(processor -> processor.receive(request, response, logoutRequest, messageContext)));

        val requestDispatcher = request.getServletContext().getRequestDispatcher(CasProtocolConstants.ENDPOINT_LOGOUT);
        requestDispatcher.forward(request, response);
    }

    protected void ensureLogoutRequestIsSignedIfNecessary(final SamlRegisteredService registeredService,
                                                          final MessageContext messageContext) throws SAMLException {
        var ensureSignature = false;
        if (registeredService.getSignLogoutRequest().isUndefined()) {
            val logout = configurationContext.getCasProperties().getAuthn().getSamlIdp().getLogout();
            ensureSignature = logout.isForceSignedLogoutRequests();
        } else {
            ensureSignature = registeredService.getSignLogoutRequest().isTrue();
        }

        if (ensureSignature && !SAMLBindingSupport.isMessageSigned(messageContext)) {
            throw new SAMLException("Logout request is not signed but should be for service %s"
                .formatted(registeredService.getServiceId()));
        }
    }

    protected <T> T buildSamlObject(final QName qname, final Class<T> clazz) {
        val builderFactory = getConfigurationContext().getOpenSamlConfigBean().getBuilderFactory();
        val builder = (SAMLObjectBuilder) builderFactory.getBuilder(qname);
        return clazz.cast(Objects.requireNonNull(builder).buildObject());
    }

    protected void handleSloProfileRequest(final HttpServletResponse response,
                                           final HttpServletRequest request,
                                           final BaseHttpServletRequestXMLMessageDecoder decoder,
                                           final String logoutRequestBinding) throws Throwable {
        val logout = getConfigurationContext().getCasProperties().getAuthn().getSamlIdp().getLogout();
        if (logout.isSingleLogoutCallbacksDisabled()) {
            LOGGER.info("Processing SAML2 IdP SLO requests is disabled");
            return;
        }

        val extractor = getConfigurationContext().getSamlHttpRequestExtractor();
        val result = extractor.extract(request, decoder, SignableSAMLObject.class);
        if (result.isPresent()) {
            val pair = result.get();
            if (pair.getKey() instanceof LogoutResponse) {
                handleLogoutResponse(pair);
            } else if (pair.getKey() instanceof LogoutRequest) {
                handleLogoutRequest(response, request, pair, logoutRequestBinding);
            }
        } else {
            LOGGER.trace("Unable to process logout request/response");
        }

    }
}
