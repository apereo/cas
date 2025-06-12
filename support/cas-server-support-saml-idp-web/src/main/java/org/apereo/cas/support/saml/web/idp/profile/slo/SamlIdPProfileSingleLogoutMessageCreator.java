package org.apereo.cas.support.saml.web.idp.profile.slo;

import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.logout.slo.SingleLogoutMessage;
import org.apereo.cas.logout.slo.SingleLogoutMessageCreator;
import org.apereo.cas.logout.slo.SingleLogoutRequestContext;
import org.apereo.cas.services.RegisteredServiceUsernameProviderContext;
import org.apereo.cas.support.saml.SamlUtils;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceMetadataAdaptor;
import org.apereo.cas.support.saml.util.AbstractSaml20ObjectBuilder;
import org.apereo.cas.support.saml.web.idp.profile.SamlProfileHandlerConfigurationContext;
import org.apereo.cas.support.saml.web.idp.profile.builders.nameid.SamlAttributeBasedNameIdGenerator;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.http.HttpRequestUtils;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.profile.context.ProfileRequestContext;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.core.LogoutRequest;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.core.NameIDType;
import org.opensaml.soap.common.SOAPObjectBuilder;
import org.opensaml.soap.soap11.Body;
import org.opensaml.soap.soap11.Envelope;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Optional;

/**
 * This is {@link SamlIdPProfileSingleLogoutMessageCreator}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Slf4j
public class SamlIdPProfileSingleLogoutMessageCreator extends AbstractSaml20ObjectBuilder implements SingleLogoutMessageCreator {

    private final SamlProfileHandlerConfigurationContext samlProfileHandlerConfigurationContext;

    private final SOAPObjectBuilder<Envelope> envelopeBuilder;

    private final SOAPObjectBuilder<Body> bodyBuilder;

    public SamlIdPProfileSingleLogoutMessageCreator(final SamlProfileHandlerConfigurationContext samlProfileHandlerConfigurationContext) {
        super(samlProfileHandlerConfigurationContext.getOpenSamlConfigBean());
        this.samlProfileHandlerConfigurationContext = samlProfileHandlerConfigurationContext;
        val builderFactory = XMLObjectProviderRegistrySupport.getBuilderFactory();
        this.envelopeBuilder = (SOAPObjectBuilder<Envelope>) builderFactory.getBuilder(Envelope.DEFAULT_ELEMENT_NAME);
        this.bodyBuilder = (SOAPObjectBuilder<Body>) builderFactory.getBuilder(Body.DEFAULT_ELEMENT_NAME);
    }

    @Override
    public SingleLogoutMessage create(final SingleLogoutRequestContext request) throws Throwable {
        val samlIdPProperties = samlProfileHandlerConfigurationContext.getCasProperties().getAuthn().getSamlIdp();
        val id = '_' + String.valueOf(RandomUtils.nextLong());

        val samlService = (SamlRegisteredService) request.getRegisteredService();
        val skewAllowance = samlService.getSkewAllowance() != 0
            ? samlService.getSkewAllowance()
            : Beans.newDuration(samlIdPProperties.getResponse().getSkewAllowance()).toSeconds();

        val issueInstant = ZonedDateTime.now(ZoneOffset.UTC).plusSeconds(skewAllowance);

        val serviceId = request.getService().getId();
        val resolver = samlProfileHandlerConfigurationContext.getSamlRegisteredServiceCachingMetadataResolver();
        val adaptor = SamlRegisteredServiceMetadataAdaptor.get(resolver, samlService, serviceId)
            .orElseThrow(() -> new IllegalArgumentException("Unable to find metadata for saml service " + serviceId));

        val nameId = buildNameId(request, adaptor);

        val samlLogoutRequest = newLogoutRequest(id, issueInstant,
            request.getLogoutUrl().toExternalForm(),
            newIssuer(samlIdPProperties.getCore().getEntityId()),
            request.getTicketId(),
            nameId);
        val binding = request.getProperties().get(SamlIdPSingleLogoutServiceLogoutUrlBuilder.PROPERTY_NAME_SINGLE_LOGOUT_BINDING);
        if (shouldSignLogoutRequestFor(samlService)) {
            val httpRequest = HttpRequestUtils.getHttpServletRequestFromRequestAttributes();
            val httpResponse = HttpRequestUtils.getHttpServletResponseFromRequestAttributes();
            FunctionUtils.doUnchecked(__ -> samlProfileHandlerConfigurationContext.getSamlObjectSigner().encode(
                samlLogoutRequest, samlService, adaptor,
                httpResponse, httpRequest, binding, samlLogoutRequest, new MessageContext()));
        }
        if (SAMLConstants.SAML2_SOAP11_BINDING_URI.equalsIgnoreCase(binding)) {
            val envelope = envelopeBuilder.buildObject();
            val body = bodyBuilder.buildObject();
            envelope.setBody(body);
            body.getUnknownXMLObjects().add(samlLogoutRequest);
            SamlUtils.logSamlObject(openSamlConfigBean, envelope);
            return buildSingleLogoutMessage(samlLogoutRequest, envelope);
        }

        SamlUtils.logSamlObject(openSamlConfigBean, samlLogoutRequest);
        return buildSingleLogoutMessage(samlLogoutRequest, samlLogoutRequest);
    }

    private NameID buildNameId(final SingleLogoutRequestContext request, final SamlRegisteredServiceMetadataAdaptor adaptor) throws Throwable {
        val samlService = (SamlRegisteredService) request.getRegisteredService();
        val effectiveNameIdFormats = StringUtils.isNotBlank(samlService.getRequiredNameIdFormat())
            ? CollectionUtils.wrapList(samlService.getRequiredNameIdFormat())
            : adaptor.getSupportedNameIdFormats();
        if (effectiveNameIdFormats.isEmpty()) {
            effectiveNameIdFormats.add(NameIDType.UNSPECIFIED);
        }

        for (val nameFormat : effectiveNameIdFormats) {
            try {
                val nameIdValue = buildLogoutRequestNameId(request, nameFormat);
                val encoder = SamlAttributeBasedNameIdGenerator.get(Optional.empty(), nameFormat, samlService, nameIdValue);
                LOGGER.debug("Encoding NameID based on [{}]", nameFormat);
                val nameId = encoder.generate(new ProfileRequestContext(), nameFormat);
                if (nameId != null) {
                    LOGGER.debug("Generated NameID [{}] with format [{}]", nameId.getValue(), nameFormat);
                    return nameId;
                }
            } catch (final Exception e) {
                LoggingUtils.error(LOGGER, e);
            }
        }
        throw new IllegalArgumentException("Unable to find supported NameID format for service %s".formatted(samlService.getServiceId()));
    }

    protected boolean shouldSignLogoutRequestFor(final SamlRegisteredService registeredService) {
        val samlIdPProperties = samlProfileHandlerConfigurationContext.getCasProperties().getAuthn().getSamlIdp();
        return registeredService.getSignLogoutRequest().isUndefined()
            ? samlIdPProperties.getLogout().isForceSignedLogoutRequests()
            : registeredService.getSignLogoutRequest().isTrue();
    }

    protected String buildLogoutRequestNameId(final SingleLogoutRequestContext request, final String nameIdFormat) throws Throwable {
        val samlService = (SamlRegisteredService) request.getRegisteredService();
        LOGGER.debug("Preparing NameID attribute for SAML service [{}] with format [{}]", samlService.getName(), nameIdFormat);
        val principal = request.getExecutionRequest().getTicketGrantingTicket()
            .getAuthentication().getPrincipal();
        if (NameIDType.TRANSIENT.equalsIgnoreCase(StringUtils.trim(nameIdFormat))) {
            val serviceId = request.getService().getId();
            val resolver = samlProfileHandlerConfigurationContext.getSamlRegisteredServiceCachingMetadataResolver();
            val adaptorRes = SamlRegisteredServiceMetadataAdaptor.get(resolver, samlService, serviceId);
            val adaptor = adaptorRes.orElseThrow(() -> new IllegalArgumentException("Unable to find metadata for saml service " + serviceId));
            val entityId = adaptor.getEntityId();
            val principalName = principal.getId();
            LOGGER.debug("Generating transient NameID value for principal [{}] and entity id [{}]", principalName, entityId);
            return samlProfileHandlerConfigurationContext.getPersistentIdGenerator().generate(principalName, entityId);
        }

        val usernameContext = RegisteredServiceUsernameProviderContext.builder()
            .registeredService(samlService)
            .service(request.getService())
            .principal(principal)
            .applicationContext(samlProfileHandlerConfigurationContext.getApplicationContext())
            .build();
        val principalName = samlService.getUsernameAttributeProvider().resolveUsername(usernameContext);
        LOGGER.trace("Preparing NameID attribute for principal [{}]", principalName);
        return principalName;
    }

    private SingleLogoutMessage buildSingleLogoutMessage(final LogoutRequest logoutRequest, final XMLObject message) {
        val builder = SingleLogoutMessage.<LogoutRequest>builder();
        return builder
            .message(logoutRequest)
            .payload(SamlUtils.transformSamlObject(this.openSamlConfigBean, message).toString())
            .build();
    }
}
