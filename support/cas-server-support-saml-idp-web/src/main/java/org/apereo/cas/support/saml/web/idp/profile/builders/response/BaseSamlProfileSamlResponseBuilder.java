package org.apereo.cas.support.saml.web.idp.profile.builders.response;

import org.apereo.cas.audit.AuditActionResolvers;
import org.apereo.cas.audit.AuditResourceResolvers;
import org.apereo.cas.audit.AuditableActions;
import org.apereo.cas.support.saml.SamlIdPConstants;
import org.apereo.cas.support.saml.SamlIdPUtils;
import org.apereo.cas.support.saml.SamlProtocolConstants;
import org.apereo.cas.support.saml.services.idp.metadata.MetadataEntityAttributeQuery;
import org.apereo.cas.support.saml.util.AbstractSaml20ObjectBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.SamlProfileBuilderContext;
import org.apereo.cas.support.saml.web.idp.profile.builders.SamlProfileObjectBuilder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apereo.inspektr.audit.annotation.Audit;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.messaging.context.ScratchContext;
import org.opensaml.saml.common.SAMLObject;
import org.opensaml.saml.common.binding.SAMLBindingSupport;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.saml.saml2.core.NameIDType;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * The {@link BaseSamlProfileSamlResponseBuilder} is responsible for
 * building the final SAML assertion for the relying party.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
@Slf4j
@Getter
public abstract class BaseSamlProfileSamlResponseBuilder<T extends XMLObject> extends AbstractSaml20ObjectBuilder
    implements SamlProfileObjectBuilder<T> {

    protected final SamlProfileSamlResponseBuilderConfigurationContext configurationContext;

    protected BaseSamlProfileSamlResponseBuilder(final SamlProfileSamlResponseBuilderConfigurationContext ctx) {
        super(ctx.getOpenSamlConfigBean());
        this.configurationContext = ctx;
    }

    @Audit(
        action = AuditableActions.SAML2_RESPONSE,
        actionResolverName = AuditActionResolvers.SAML2_RESPONSE_ACTION_RESOLVER,
        resourceResolverName = AuditResourceResolvers.SAML2_RESPONSE_RESOURCE_RESOLVER)
    @Override
    public T build(final SamlProfileBuilderContext context) throws Exception {
        val assertion = context.getAuthenticatedAssertion().isPresent()
            ? Optional.of(buildSamlAssertion(context))
            : Optional.<Assertion>empty();
        val finalResponse = buildResponse(assertion, context);
        return encodeFinalResponse(context, finalResponse);
    }

    protected T encodeFinalResponse(final SamlProfileBuilderContext context,
                                    final T finalResponse) throws Exception {
        val scratch = context.getMessageContext().ensureSubcontext(ScratchContext.class);
        val map = (Map) Objects.requireNonNull(scratch).getMap();
        val encodeResponse = (Boolean) map.getOrDefault(SamlProtocolConstants.PARAMETER_ENCODE_RESPONSE, Boolean.TRUE);

        if (encodeResponse) {
            val relayState = SAMLBindingSupport.getRelayState(context.getMessageContext());
            LOGGER.trace("Relay state is [{}]", relayState);
            return encode(context, finalResponse, relayState);
        }
        return finalResponse;
    }

    protected Assertion buildSamlAssertion(final SamlProfileBuilderContext context) throws Exception {
        return configurationContext.getSamlProfileSamlAssertionBuilder().build(context);
    }

    protected abstract T buildResponse(Optional<Assertion> assertion, SamlProfileBuilderContext context) throws Exception;

    protected Issuer buildSamlResponseIssuer(final String entityId) {
        val issuer = newIssuer(entityId);
        issuer.setFormat(NameIDType.ENTITY);
        return issuer;
    }

    protected abstract T encode(SamlProfileBuilderContext context,
                                T samlResponse,
                                String relayState) throws Exception;

    protected Optional<SAMLObject> encryptAssertion(final Optional<Assertion> assertion, final SamlProfileBuilderContext context) {
        return assertion.map(result -> {
            if (encryptAssertionFor(context)) {
                LOGGER.debug("SAML service [{}] requires assertions to be encrypted", context.getAdaptor().getEntityId());
                val encrypted = configurationContext.getSamlObjectEncrypter().encode(assertion.get(),
                    context.getRegisteredService(), context.getAdaptor());
                if (encrypted == null) {
                    LOGGER.debug("SAML registered service [{}] is unable to encrypt assertions",
                        context.getAdaptor().getEntityId());
                    return assertion.get();
                }
                return encrypted;
            }
            LOGGER.debug("SAML registered service [{}] does not require assertions to be encrypted",
                context.getAdaptor().getEntityId());
            return assertion.get();
        });

    }

    protected boolean encryptAssertionFor(final SamlProfileBuilderContext context) {
        return context.getRegisteredService().isEncryptAssertions()
            || SamlIdPUtils.doesEntityDescriptorMatchEntityAttribute(context.getAdaptor().getEntityDescriptor(),
            List.of(MetadataEntityAttributeQuery.of(SamlIdPConstants.KnownEntityAttributes.SHIBBOLETH_ENCRYPT_ASSERTIONS.getName(),
                Attribute.URI_REFERENCE, List.of(Boolean.TRUE.toString()))));
    }
}
