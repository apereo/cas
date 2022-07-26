package org.apereo.cas.support.saml.web.idp.profile.builders.response;

import org.apereo.cas.support.saml.SamlIdPUtils;
import org.apereo.cas.support.saml.idp.metadata.locator.SamlIdPSamlRegisteredServiceCriterion;
import org.apereo.cas.support.saml.web.idp.profile.builders.SamlProfileBuilderContext;
import org.apereo.cas.support.saml.web.idp.profile.builders.enc.encoder.sso.SamlResponseArtifactEncoder;
import org.apereo.cas.support.saml.web.idp.profile.builders.enc.encoder.sso.SamlResponsePostEncoder;
import org.apereo.cas.support.saml.web.idp.profile.builders.enc.encoder.sso.SamlResponsePostSimpleSignEncoder;
import org.apereo.cas.ticket.query.SamlAttributeQueryTicket;
import org.apereo.cas.ticket.query.SamlAttributeQueryTicketFactory;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.web.support.CookieUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jooq.lambda.Unchecked;
import org.opensaml.saml.common.SAMLVersion;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.metadata.criteria.entity.impl.EvaluableEntityRoleEntityDescriptorCriterion;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.AttributeQuery;
import org.opensaml.saml.saml2.core.EncryptedAssertion;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.core.StatusCode;
import org.opensaml.saml.saml2.metadata.IDPSSODescriptor;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Objects;

/**
 * This is {@link SamlProfileSaml2ResponseBuilder}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
public class SamlProfileSaml2ResponseBuilder extends BaseSamlProfileSamlResponseBuilder<Response> {
    private static final long serialVersionUID = 1488837627964481272L;

    public SamlProfileSaml2ResponseBuilder(final SamlProfileSamlResponseBuilderConfigurationContext configurationContext) {
        super(configurationContext);
    }

    @Override
    public Response buildResponse(final Assertion assertion,
                                  final SamlProfileBuilderContext context) throws Exception {
        val id = '_' + String.valueOf(RandomUtils.nextLong());
        val samlResponse = newResponse(id, ZonedDateTime.now(ZoneOffset.UTC), context.getSamlRequest().getID(), null);
        samlResponse.setVersion(SAMLVersion.VERSION_20);

        val issuerId = FunctionUtils.doIf(StringUtils.isNotBlank(context.getRegisteredService().getIssuerEntityId()),
                context.getRegisteredService()::getIssuerEntityId,
                Unchecked.supplier(() -> {
                    val criteriaSet = new CriteriaSet(
                        new EvaluableEntityRoleEntityDescriptorCriterion(IDPSSODescriptor.DEFAULT_ELEMENT_NAME),
                        new SamlIdPSamlRegisteredServiceCriterion(context.getRegisteredService()));
                    LOGGER.trace("Resolving entity id from SAML2 IdP metadata to determine issuer for [{}]", context.getRegisteredService().getName());
                    val entityDescriptor = Objects.requireNonNull(getConfigurationContext().getSamlIdPMetadataResolver().resolveSingle(criteriaSet));
                    return entityDescriptor.getEntityID();
                }))
            .get();

        samlResponse.setIssuer(buildSamlResponseIssuer(issuerId));
        val acs = SamlIdPUtils.determineEndpointForRequest(Pair.of(context.getSamlRequest(), context.getMessageContext()),
            context.getAdaptor(), context.getBinding());
        val location = StringUtils.isBlank(acs.getResponseLocation()) ? acs.getLocation() : acs.getResponseLocation();
        samlResponse.setDestination(location);

        if (getConfigurationContext().getCasProperties()
            .getAuthn().getSamlIdp().getCore().isAttributeQueryProfileEnabled()) {
            storeAttributeQueryTicketInRegistry(assertion, context);
        }

        val finalAssertion = encryptAssertion(assertion, context);

        if (finalAssertion instanceof EncryptedAssertion) {
            LOGGER.trace("Built assertion is encrypted, so the response will add it to the encrypted assertions collection");
            samlResponse.getEncryptedAssertions().add(EncryptedAssertion.class.cast(finalAssertion));
        } else {
            LOGGER.trace("Built assertion is not encrypted, so the response will add it to the assertions collection");
            samlResponse.getAssertions().add(Assertion.class.cast(finalAssertion));
        }

        val status = newStatus(StatusCode.SUCCESS, null);
        samlResponse.setStatus(status);

        openSamlConfigBean.logObject(samlResponse);

        if (context.getRegisteredService().getSignResponses().isTrue()) {
            LOGGER.debug("SAML entity id [{}] indicates that SAML responses should be signed", context.getAdaptor().getEntityId());
            val samlResponseSigned = getConfigurationContext().getSamlObjectSigner().encode(samlResponse,
                context.getRegisteredService(), context.getAdaptor(), context.getHttpResponse(), context.getHttpRequest(),
                context.getBinding(), context.getSamlRequest(), context.getMessageContext());
            openSamlConfigBean.logObject(samlResponseSigned);
            return samlResponseSigned;
        }

        return samlResponse;
    }

    @Override
    protected Response encode(final SamlProfileBuilderContext context,
                              final Response samlResponse,
                              final String relayState) throws Exception {
        LOGGER.trace("Constructing encoder based on binding [{}] for [{}]", context.getBinding(), context.getAdaptor().getEntityId());
        if (context.getBinding().equalsIgnoreCase(SAMLConstants.SAML2_ARTIFACT_BINDING_URI)) {
            val encoder = new SamlResponseArtifactEncoder(
                getConfigurationContext().getVelocityEngineFactory(),
                context.getAdaptor(), context.getHttpRequest(), context.getHttpResponse(),
                getConfigurationContext().getSamlArtifactMap());
            return encoder.encode(context.getSamlRequest(), samlResponse, relayState, context.getMessageContext());
        }

        if (context.getBinding().equalsIgnoreCase(SAMLConstants.SAML2_POST_SIMPLE_SIGN_BINDING_URI)) {
            val encoder = new SamlResponsePostSimpleSignEncoder(getConfigurationContext().getVelocityEngineFactory(),
                context.getAdaptor(), context.getHttpResponse(), context.getHttpRequest());
            return encoder.encode(context.getSamlRequest(), samlResponse, relayState, context.getMessageContext());
        }

        val encoder = new SamlResponsePostEncoder(getConfigurationContext().getVelocityEngineFactory(), context.getAdaptor(), context.getHttpResponse(), context.getHttpRequest());
        return encoder.encode(context.getSamlRequest(), samlResponse, relayState, context.getMessageContext());
    }

    private void storeAttributeQueryTicketInRegistry(final Assertion assertion, final SamlProfileBuilderContext context)
        throws Exception {
        val existingQuery = context.getHttpRequest().getAttribute(AttributeQuery.class.getSimpleName());
        if (existingQuery == null) {
            val nameId = (String) context.getHttpRequest().getAttribute(NameID.class.getName());
            val ticketGrantingTicket = CookieUtils.getTicketGrantingTicketFromRequest(
                getConfigurationContext().getTicketGrantingTicketCookieGenerator(),
                getConfigurationContext().getTicketRegistry(), context.getHttpRequest());

            val samlAttributeQueryTicketFactory = (SamlAttributeQueryTicketFactory) getConfigurationContext().getTicketFactory().get(SamlAttributeQueryTicket.class);
            val ticket = samlAttributeQueryTicketFactory.create(nameId, assertion, context.getAdaptor().getEntityId(), ticketGrantingTicket);
            getConfigurationContext().getTicketRegistry().addTicket(ticket);
            context.getHttpRequest().setAttribute(SamlAttributeQueryTicket.class.getName(), ticket);
        }
    }
}
