package org.apereo.cas.support.saml.web.idp.profile.builders.response;

import org.apereo.cas.support.saml.SamlException;
import org.apereo.cas.support.saml.SamlProtocolConstants;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;
import org.apereo.cas.support.saml.util.AbstractSaml20ObjectBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.SamlLogoutResponseObjectBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.slo.SamlLogoutResponseBuilderConfigurationContext;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.messaging.context.ScratchContext;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.saml.saml2.core.LogoutRequest;
import org.opensaml.saml.saml2.core.RequestAbstractType;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The {@link BaseSamlLogoutResponseBuilder} is responsible for
 * building the SAML logout response for the relying party.
 *
 * @since 6.1.0
 * @author Krzysztof Zych
 * */
@Slf4j
@Getter
public abstract class BaseSamlLogoutResponseBuilder<T extends XMLObject> extends AbstractSaml20ObjectBuilder implements SamlLogoutResponseObjectBuilder {
    private static final long serialVersionUID = -4601190836461447128L;

    private final transient SamlLogoutResponseBuilderConfigurationContext samlLogoutResponseBuilderConfigurationContext;

    public BaseSamlLogoutResponseBuilder(final SamlLogoutResponseBuilderConfigurationContext samlLogoutResponseBuilderConfigurationContext) {
        super(samlLogoutResponseBuilderConfigurationContext.getOpenSamlConfigBean());
        this.samlLogoutResponseBuilderConfigurationContext = samlLogoutResponseBuilderConfigurationContext;
    }

    @Override
    public T build(final RequestAbstractType logoutRequest,
                   final HttpServletRequest request,
                   final HttpServletResponse response,
                   final SamlRegisteredService service,
                   final SamlRegisteredServiceServiceProviderMetadataFacade adaptor,
                   final String binding,
                   final MessageContext messageContext) throws SamlException {
        val finalResponse = buildResponse(request, response,
                service, adaptor, binding, messageContext);
        return encodeFinalResponse(finalResponse, request, response, service,
                adaptor, binding, messageContext);
    }

    /**
     * Encode final response.
     *
     * @param finalResponse     the final response
     * @param request           the request
     * @param response          the response
     * @param service           the service
     * @param adaptor           the adaptor
     * @param binding           the binding
     * @param messageContext    the message context
     * @return                  encoded final response
     * @throws SamlException    the saml exception
     */
    protected T encodeFinalResponse(final T finalResponse,
                                    final HttpServletRequest request,
                                    final HttpServletResponse response,
                                    final SamlRegisteredService service,
                                    final SamlRegisteredServiceServiceProviderMetadataFacade adaptor,
                                    final String binding,
                                    final MessageContext messageContext) throws SamlException {
        val scratch = messageContext.getSubcontext(ScratchContext.class, true);

        if (scratch == null) {
            return finalResponse;
        }

        val encodeResponse = (Boolean) scratch.getMap().getOrDefault(SamlProtocolConstants.PARAMETER_ENCODE_RESPONSE, Boolean.TRUE);

        if (encodeResponse) {
            val relayState = request != null ? request.getParameter(SamlProtocolConstants.PARAMETER_SAML_RELAY_STATE) : StringUtils.EMPTY;
            LOGGER.trace("RelayState is [{}]", relayState);
            return encode(finalResponse, response, request, adaptor, relayState, binding, (LogoutRequest) messageContext.getMessage());
        }

        return finalResponse;
    }

    /**
     * Encode the final result into http response.
     *
     * @param finalResponse     the saml response
     * @param response          the http response
     * @param request           the http request
     * @param adaptor           the adaptor
     * @param relayState        the relay state
     * @param binding           the binding
     * @param logoutRequest     the logout request
     * @return the t
     */
    protected abstract T encode(T finalResponse,
                                HttpServletResponse response,
                                HttpServletRequest request,
                                SamlRegisteredServiceServiceProviderMetadataFacade adaptor,
                                String relayState, String binding,
                                LogoutRequest logoutRequest);


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
     * Build final response.
     *
     * @param request           the request
     * @param response          the response
     * @param service           the service
     * @param adaptor           the adaptor
     * @param binding           the binding
     * @param messageContext    the message context
     * @return final response
     */
    protected abstract T buildResponse(HttpServletRequest request,
                                       HttpServletResponse response,
                                       SamlRegisteredService service,
                                       SamlRegisteredServiceServiceProviderMetadataFacade adaptor,
                                       String binding,
                                       MessageContext messageContext);
}
