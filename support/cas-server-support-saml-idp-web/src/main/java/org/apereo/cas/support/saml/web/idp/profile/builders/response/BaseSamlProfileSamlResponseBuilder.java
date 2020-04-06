package org.apereo.cas.support.saml.web.idp.profile.builders.response;

import org.apereo.cas.support.saml.SamlException;
import org.apereo.cas.support.saml.SamlProtocolConstants;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;
import org.apereo.cas.support.saml.util.AbstractSaml20ObjectBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.SamlProfileObjectBuilder;

import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apereo.inspektr.audit.annotation.Audit;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.messaging.context.ScratchContext;
import org.opensaml.saml.common.SAMLObject;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.saml.saml2.core.RequestAbstractType;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.Map;

/**
 * The {@link BaseSamlProfileSamlResponseBuilder} is responsible for
 * building the final SAML assertion for the relying party.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
@Slf4j
@Getter
public abstract class BaseSamlProfileSamlResponseBuilder<T extends XMLObject> extends AbstractSaml20ObjectBuilder implements SamlProfileObjectBuilder {
    private static final long serialVersionUID = -1891703354216174875L;

    private final transient SamlProfileSamlResponseBuilderConfigurationContext samlResponseBuilderConfigurationContext;

    public BaseSamlProfileSamlResponseBuilder(final SamlProfileSamlResponseBuilderConfigurationContext samlResponseBuilderConfigurationContext) {
        super(samlResponseBuilderConfigurationContext.getOpenSamlConfigBean());
        this.samlResponseBuilderConfigurationContext = samlResponseBuilderConfigurationContext;
    }

    @Audit(
        action = "SAML2_RESPONSE",
        actionResolverName = "SAML2_RESPONSE_ACTION_RESOLVER",
        resourceResolverName = "SAML2_RESPONSE_RESOURCE_RESOLVER")
    @Override
    public T build(final RequestAbstractType authnRequest,
                   final HttpServletRequest request,
                   final HttpServletResponse response,
                   final Object casAssertion,
                   final SamlRegisteredService service,
                   final SamlRegisteredServiceServiceProviderMetadataFacade adaptor,
                   final String binding,
                   final MessageContext messageContext) throws SamlException {
        val assertion = buildSamlAssertion(authnRequest, request, response,
            casAssertion, service, adaptor, binding, messageContext);
        val finalResponse = buildResponse(assertion, casAssertion, authnRequest,
            service, adaptor, request, response, binding, messageContext);
        return encodeFinalResponse(request, response, service, adaptor, finalResponse,
            binding, authnRequest, casAssertion, messageContext);
    }

    /**
     * Encode final response.
     *
     * @param request        the request
     * @param response       the response
     * @param service        the service
     * @param adaptor        the adaptor
     * @param finalResponse  the final response
     * @param binding        the binding
     * @param authnRequest   the authn request
     * @param assertion      the assertion
     * @param messageContext the message context
     * @return the response
     */
    protected T encodeFinalResponse(final HttpServletRequest request,
                                    final HttpServletResponse response,
                                    final SamlRegisteredService service,
                                    final SamlRegisteredServiceServiceProviderMetadataFacade adaptor,
                                    final T finalResponse,
                                    final String binding,
                                    final RequestAbstractType authnRequest,
                                    final Object assertion,
                                    final MessageContext messageContext) {

        val scratch = messageContext.getSubcontext(ScratchContext.class, true);
        val map = (Map) scratch.getMap();
        val encodeResponse = (Boolean) map.getOrDefault(SamlProtocolConstants.PARAMETER_ENCODE_RESPONSE, Boolean.TRUE);

        if (encodeResponse) {
            val relayState = request != null ? request.getParameter(SamlProtocolConstants.PARAMETER_SAML_RELAY_STATE) : StringUtils.EMPTY;
            LOGGER.trace("RelayState is [{}]", relayState);
            return encode(service, finalResponse, response, request, adaptor, relayState, binding, authnRequest, assertion);
        }
        return finalResponse;
    }

    /**
     * Build saml assertion assertion.
     *
     * @param authnRequest   the authn request
     * @param request        the request
     * @param response       the response
     * @param casAssertion   the cas assertion
     * @param service        the service
     * @param adaptor        the adaptor
     * @param binding        the binding
     * @param messageContext the message context
     * @return the assertion
     */
    protected Assertion buildSamlAssertion(final RequestAbstractType authnRequest,
                                           final HttpServletRequest request,
                                           final HttpServletResponse response,
                                           final Object casAssertion,
                                           final SamlRegisteredService service,
                                           final SamlRegisteredServiceServiceProviderMetadataFacade adaptor,
                                           final String binding,
                                           final MessageContext messageContext) {
        return samlResponseBuilderConfigurationContext.getSamlProfileSamlAssertionBuilder()
            .build(authnRequest, request, response, casAssertion, service, adaptor, binding, messageContext);
    }

    /**
     * Build response response.
     *
     * @param assertion      the assertion
     * @param casAssertion   the cas assertion
     * @param authnRequest   the authn request
     * @param service        the service
     * @param adaptor        the adaptor
     * @param request        the request
     * @param response       the response
     * @param binding        the binding
     * @param messageContext the message context
     * @return the response
     * @throws SamlException the saml exception
     */
    protected abstract T buildResponse(Assertion assertion,
                                       Object casAssertion,
                                       RequestAbstractType authnRequest,
                                       SamlRegisteredService service,
                                       SamlRegisteredServiceServiceProviderMetadataFacade adaptor,
                                       HttpServletRequest request,
                                       HttpServletResponse response,
                                       String binding,
                                       MessageContext messageContext) throws SamlException;

    /**
     * Build entity issuer issuer.
     *
     * @param entityId the entity id
     * @return the issuer
     */
    protected Issuer buildSamlResponseIssuer(final String entityId) {
        val issuer = newIssuer(entityId);
        issuer.setFormat(Issuer.ENTITY);
        return issuer;
    }

    /**
     * Encode the final result into the http response.
     *
     * @param service      the service
     * @param samlResponse the saml response
     * @param httpResponse the http response; may be null to mute encoding.
     * @param httpRequest  the http request
     * @param adaptor      the adaptor
     * @param relayState   the relay state
     * @param binding      the binding
     * @param authnRequest the authn request
     * @param assertion    the assertion
     * @return the t
     * @throws SamlException the saml exception
     */
    protected abstract T encode(SamlRegisteredService service,
                                T samlResponse,
                                HttpServletResponse httpResponse,
                                HttpServletRequest httpRequest,
                                SamlRegisteredServiceServiceProviderMetadataFacade adaptor,
                                String relayState,
                                String binding,
                                RequestAbstractType authnRequest,
                                Object assertion) throws SamlException;

    /**
     * Encrypt assertion.
     *
     * @param assertion the assertion
     * @param request   the request
     * @param response  the response
     * @param service   the service
     * @param adaptor   the adaptor
     * @return the saml object
     * @throws SamlException the saml exception
     */
    @SneakyThrows
    protected SAMLObject encryptAssertion(final Assertion assertion,
                                          final HttpServletRequest request,
                                          final HttpServletResponse response,
                                          final SamlRegisteredService service,
                                          final SamlRegisteredServiceServiceProviderMetadataFacade adaptor) throws SamlException {

        if (service.isEncryptAssertions()) {
            LOGGER.debug("SAML service [{}] requires assertions to be encrypted", adaptor.getEntityId());
            val encrypted = samlResponseBuilderConfigurationContext.getSamlObjectEncrypter().encode(assertion, service, adaptor);
            if (encrypted == null) {
                LOGGER.debug("SAML registered service [{}] is unable to encrypt assertions", adaptor.getEntityId());
                return assertion;
            }
            return encrypted;
        }
        LOGGER.debug("SAML registered service [{}] does not require assertions to be encrypted", adaptor.getEntityId());
        return assertion;

    }
}
