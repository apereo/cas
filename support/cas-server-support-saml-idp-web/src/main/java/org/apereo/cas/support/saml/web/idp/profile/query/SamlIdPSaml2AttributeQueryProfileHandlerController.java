package org.apereo.cas.support.saml.web.idp.profile.query;

import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.support.saml.SamlIdPConstants;
import org.apereo.cas.support.saml.web.idp.profile.AbstractSamlIdPProfileHandlerController;
import org.apereo.cas.support.saml.web.idp.profile.SamlProfileHandlerConfigurationContext;
import org.apereo.cas.ticket.InvalidTicketException;
import org.apereo.cas.ticket.query.SamlAttributeQueryTicket;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.core.AttributeQuery;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.LinkedHashMap;

/**
 * This is {@link SamlIdPSaml2AttributeQueryProfileHandlerController}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
public class SamlIdPSaml2AttributeQueryProfileHandlerController extends AbstractSamlIdPProfileHandlerController {


    public SamlIdPSaml2AttributeQueryProfileHandlerController(final SamlProfileHandlerConfigurationContext samlProfileHandlerConfigurationContext) {
        super(samlProfileHandlerConfigurationContext);
    }

    /**
     * Handle post request.
     *
     * @param response the response
     * @param request  the request
     */
    @PostMapping(path = SamlIdPConstants.ENDPOINT_SAML2_SOAP_ATTRIBUTE_QUERY)
    protected void handlePostRequest(final HttpServletResponse response,
                                     final HttpServletRequest request) {
        val ctx = decodeSoapRequest(request);
        val query = (AttributeQuery) ctx.getMessage();
        val config = getSamlProfileHandlerConfigurationContext();
        try {
            val issuer = query.getIssuer().getValue();
            val service = verifySamlRegisteredService(issuer);
            val adaptor = getSamlMetadataFacadeFor(service, query);
            if (adaptor.isEmpty()) {
                throw new UnauthorizedServiceException(UnauthorizedServiceException.CODE_UNAUTHZ_SERVICE, "Cannot find metadata linked to " + issuer);
            }

            val facade = adaptor.get();
            verifyAuthenticationContextSignature(ctx, request, query, facade);

            val availableAttributes = new LinkedHashMap<String, Object>();
            val finalAttributes = new LinkedHashMap<String, Object>();
            if (!query.getAttributes().isEmpty()) {
                val id = config.getSamlAttributeQueryTicketFactory().createTicketIdFor(query.getSubject().getNameID().getValue());
                val ticket = config.getTicketRegistry().getTicket(id, SamlAttributeQueryTicket.class);
                if (ticket == null) {
                    throw new InvalidTicketException(id);
                }
                val authentication = ticket.getTicketGrantingTicket().getAuthentication();
                val principal = authentication.getPrincipal();
                availableAttributes.putAll(authentication.getAttributes());
                availableAttributes.putAll(principal.getAttributes());
            }
            query.getAttributes().forEach(a -> {
                if (availableAttributes.containsKey(a.getName())) {
                    finalAttributes.put(a.getName(), availableAttributes.get(a.getName()));
                }
            });
            LOGGER.trace("Final attributes for attribute query are [{}]", finalAttributes);
            val casAssertion = buildCasAssertion(issuer, service, finalAttributes);
            config.getResponseBuilder().build(query, request, response, casAssertion,
                service, facade, SAMLConstants.SAML2_SOAP11_BINDING_URI, ctx);
        } catch (final Exception e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.error(e.getMessage(), e);
            } else {
                LOGGER.error(e.getMessage());
            }
            request.setAttribute(SamlIdPConstants.REQUEST_ATTRIBUTE_ERROR,
                "Unable to build SOAP response: " + StringUtils.defaultString(e.getMessage()));
            config.getSamlFaultResponseBuilder().build(query, request, response,
                null, null, null, SAMLConstants.SAML2_SOAP11_BINDING_URI, ctx);
        }
    }
}
