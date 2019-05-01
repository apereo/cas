package org.apereo.cas.support.saml.web.idp.profile.slo;

import org.apereo.cas.configuration.model.support.saml.idp.SamlIdPProperties;
import org.apereo.cas.logout.slo.SingleLogoutMessage;
import org.apereo.cas.logout.slo.SingleLogoutMessageCreator;
import org.apereo.cas.logout.slo.SingleLogoutRequest;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.SamlUtils;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;
import org.apereo.cas.support.saml.services.idp.metadata.cache.SamlRegisteredServiceCachingMetadataResolver;
import org.apereo.cas.support.saml.util.AbstractSaml20ObjectBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.enc.SamlIdPObjectSigner;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.HttpRequestUtils;
import org.apereo.cas.util.RandomUtils;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.attribute.StringAttributeValue;
import net.shibboleth.idp.saml.attribute.encoding.impl.SAML2StringNameIDEncoder;
import org.apache.commons.lang3.StringUtils;
import org.jasig.cas.client.authentication.AttributePrincipal;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.core.LogoutRequest;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.soap.common.SOAPObjectBuilder;
import org.opensaml.soap.soap11.Body;
import org.opensaml.soap.soap11.Envelope;

/**
 * This is {@link SamlProfileSingleLogoutMessageCreator}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Slf4j
public class SamlProfileSingleLogoutMessageCreator extends AbstractSaml20ObjectBuilder implements SingleLogoutMessageCreator {

    private static final long serialVersionUID = -5895467960534493675L;

    /**
     * The Services manager.
     */
    protected final transient ServicesManager servicesManager;

    /**
     * The Saml registered service caching metadata resolver.
     */
    protected final transient SamlRegisteredServiceCachingMetadataResolver samlRegisteredServiceCachingMetadataResolver;

    /**
     * SAML idp settings.
     */
    protected final SamlIdPProperties samlIdPProperties;

    /**
     * Saml idp object signer.
     */
    protected final SamlIdPObjectSigner samlObjectSigner;

    private final transient SOAPObjectBuilder<Envelope> envelopeBuilder;
    private final transient SOAPObjectBuilder<Body> bodyBuilder;

    public SamlProfileSingleLogoutMessageCreator(final OpenSamlConfigBean configBean,
                                                 final ServicesManager servicesManager,
                                                 final SamlRegisteredServiceCachingMetadataResolver samlRegisteredServiceCachingMetadataResolver,
                                                 final SamlIdPProperties samlIdPProperties,
                                                 final SamlIdPObjectSigner samlObjectSigner) {
        super(configBean);
        this.servicesManager = servicesManager;
        this.samlRegisteredServiceCachingMetadataResolver = samlRegisteredServiceCachingMetadataResolver;
        this.samlIdPProperties = samlIdPProperties;
        this.samlObjectSigner = samlObjectSigner;
        val builderFactory = XMLObjectProviderRegistrySupport.getBuilderFactory();
        this.envelopeBuilder = (SOAPObjectBuilder<Envelope>) builderFactory.getBuilder(Envelope.DEFAULT_ELEMENT_NAME);
        this.bodyBuilder = (SOAPObjectBuilder<Body>) builderFactory.getBuilder(Body.DEFAULT_ELEMENT_NAME);
    }

    @Override
    @SneakyThrows
    public SingleLogoutMessage create(final SingleLogoutRequest request) {
        val id = '_' + String.valueOf(RandomUtils.nextLong());

        val samlService = (SamlRegisteredService) request.getRegisteredService();
        val skewAllowance = samlService.getSkewAllowance() > 0
            ? samlService.getSkewAllowance()
            : samlIdPProperties.getResponse().getSkewAllowance();

        val issueInstant = DateTime.now(DateTimeZone.UTC).plusSeconds(skewAllowance);

        val encoder = new SAML2StringNameIDEncoder();

        encoder.setNameFormat(StringUtils.defaultIfBlank(samlService.getRequiredNameIdFormat(), NameID.UNSPECIFIED));
        encoder.setNameQualifier(samlService.getNameIdQualifier());

        val attribute = new IdPAttribute(AttributePrincipal.class.getName());
        val principalName = request.getTicketGrantingTicket().getAuthentication().getPrincipal().getId();
        LOGGER.trace("Preparing NameID attribute for principal [{}]", principalName);
        attribute.setValues(CollectionUtils.wrap(new StringAttributeValue(principalName)));
        val nameId = encoder.encode(attribute);

        var samlLogoutRequest = newLogoutRequest(id, issueInstant,
            request.getLogoutUrl().toExternalForm(),
            newIssuer(samlIdPProperties.getEntityId()),
            request.getTicketId(),
            nameId);

        val binding = request.getProperties().get(SamlIdPSingleLogoutServiceLogoutUrlBuilder.PROPERTY_NAME_SINGLE_LOGOUT_BINDING);
        if (samlIdPProperties.getLogout().isForceSignedLogoutRequests()) {
            val serviceId = request.getService().getId();
            val adaptorRes = SamlRegisteredServiceServiceProviderMetadataFacade.get(samlRegisteredServiceCachingMetadataResolver, samlService, serviceId);
            val adaptor = adaptorRes.orElseThrow(() -> new IllegalArgumentException("Unable to find metadata for saml service " + serviceId));
            val httpRequest = HttpRequestUtils.getHttpServletRequestFromRequestAttributes();
            val httpResponse = HttpRequestUtils.getHttpServletResponseFromRequestAttributes();
            samlObjectSigner.encode(samlLogoutRequest, samlService, adaptor, httpResponse, httpRequest, binding, samlLogoutRequest);
        }

        if (SAMLConstants.SAML2_SOAP11_BINDING_URI.equalsIgnoreCase(binding)) {
            val envelope = envelopeBuilder.buildObject();
            val body = bodyBuilder.buildObject();
            envelope.setBody(body);
            body.getUnknownXMLObjects().add(samlLogoutRequest);
            return buildSingleLogoutMessage(samlLogoutRequest, envelope);
        }

        return buildSingleLogoutMessage(samlLogoutRequest, samlLogoutRequest);
    }

    private SingleLogoutMessage buildSingleLogoutMessage(final LogoutRequest logoutRequest, final XMLObject message) {
        val builder = SingleLogoutMessage.<LogoutRequest>builder();
        return builder
            .message(logoutRequest)
            .payload(SamlUtils.transformSamlObject(this.openSamlConfigBean, message).toString())
            .build();
    }
}
