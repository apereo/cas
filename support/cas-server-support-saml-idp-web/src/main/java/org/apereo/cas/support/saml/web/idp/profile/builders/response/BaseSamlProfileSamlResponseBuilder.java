package org.apereo.cas.support.saml.web.idp.profile.builders.response;

import org.apereo.cas.audit.AuditActionResolvers;
import org.apereo.cas.audit.AuditResourceResolvers;
import org.apereo.cas.audit.AuditableActions;
import org.apereo.cas.support.saml.SamlProtocolConstants;
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
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.saml.saml2.core.NameIDType;

import java.io.Serial;
import java.util.Map;
import java.util.Objects;

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
    @Serial
    private static final long serialVersionUID = -1891703354216174875L;

    private final transient SamlProfileSamlResponseBuilderConfigurationContext configurationContext;

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
        val assertion = buildSamlAssertion(context);
        val finalResponse = buildResponse(assertion, context);
        return encodeFinalResponse(context, finalResponse);
    }

    /**
     * Encode final response.
     *
     * @param context       the context
     * @param finalResponse the final response
     * @return the response
     * @throws Exception the exception
     */
    protected T encodeFinalResponse(final SamlProfileBuilderContext context,
                                    final T finalResponse) throws Exception {
        val scratch = context.getMessageContext().getSubcontext(ScratchContext.class, true);
        val map = (Map) Objects.requireNonNull(scratch).getMap();
        val encodeResponse = (Boolean) map.getOrDefault(SamlProtocolConstants.PARAMETER_ENCODE_RESPONSE, Boolean.TRUE);

        if (encodeResponse) {
            val relayState = SAMLBindingSupport.getRelayState(context.getMessageContext());
            LOGGER.trace("Relay state is [{}]", relayState);
            return encode(context, finalResponse, relayState);
        }
        return finalResponse;
    }

    /**
     * Build saml assertion assertion.
     *
     * @param context the context
     * @return the assertion
     * @throws Exception the exception
     */
    protected Assertion buildSamlAssertion(final SamlProfileBuilderContext context) throws Exception {
        return configurationContext.getSamlProfileSamlAssertionBuilder().build(context);
    }

    /**
     * Build response.
     *
     * @param assertion the assertion
     * @param context   the context
     * @return the response
     * @throws Exception the exception
     */
    protected abstract T buildResponse(Assertion assertion, SamlProfileBuilderContext context) throws Exception;

    /**
     * Build entity issuer issuer.
     *
     * @param entityId the entity id
     * @return the issuer
     */
    protected Issuer buildSamlResponseIssuer(final String entityId) {
        val issuer = newIssuer(entityId);
        issuer.setFormat(NameIDType.ENTITY);
        return issuer;
    }

    /**
     * Encode the final result into the http response.
     *
     * @param context      the context
     * @param samlResponse the saml response
     * @param relayState   the relay state
     * @return the t
     * @throws Exception the exception
     */
    protected abstract T encode(SamlProfileBuilderContext context,
                                T samlResponse,
                                String relayState) throws Exception;

    /**
     * Encrypt assertion.
     *
     * @param assertion the assertion
     * @param context   the context
     * @return the saml object
     */
    protected SAMLObject encryptAssertion(final Assertion assertion, final SamlProfileBuilderContext context) {
        if (context.getRegisteredService().isEncryptAssertions()) {
            LOGGER.debug("SAML service [{}] requires assertions to be encrypted", context.getAdaptor().getEntityId());
            val encrypted = configurationContext.getSamlObjectEncrypter().encode(assertion,
                context.getRegisteredService(), context.getAdaptor());
            if (encrypted == null) {
                LOGGER.debug("SAML registered service [{}] is unable to encrypt assertions",
                    context.getAdaptor().getEntityId());
                return assertion;
            }
            return encrypted;
        }
        LOGGER.debug("SAML registered service [{}] does not require assertions to be encrypted",
            context.getAdaptor().getEntityId());
        return assertion;

    }
}
