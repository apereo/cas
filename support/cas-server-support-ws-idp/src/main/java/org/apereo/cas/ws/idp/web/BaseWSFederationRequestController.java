package org.apereo.cas.ws.idp.web;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredServiceAccessStrategyUtils;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.support.saml.SamlException;
import org.apereo.cas.ticket.SecurityTokenTicket;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.web.support.WebUtils;
import org.apereo.cas.ws.idp.WSFederationConstants;
import org.apereo.cas.ws.idp.services.WSFederationRegisteredService;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.cxf.ws.security.tokenstore.SecurityToken;
import org.apache.http.client.utils.URIBuilder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * This is {@link BaseWSFederationRequestController}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Controller
@Slf4j
@Getter
@RequiredArgsConstructor
public abstract class BaseWSFederationRequestController {
    private final WSFederationRequestConfigurationContext wsFederationRequestConfigurationContext;

    /**
     * Construct service url string.
     *
     * @param request      the request
     * @param response     the response
     * @param wsfedRequest the ws federation request
     * @return the service url
     */
    protected String constructServiceUrl(final HttpServletRequest request, final HttpServletResponse response,
                                         final WSFederationRequest wsfedRequest) {
        try {
            val builder = new URIBuilder(wsFederationRequestConfigurationContext.getCallbackService().getId());

            builder.addParameter(WSFederationConstants.WA, wsfedRequest.getWa());
            builder.addParameter(WSFederationConstants.WREPLY, wsfedRequest.getWreply());
            builder.addParameter(WSFederationConstants.WTREALM, wsfedRequest.getWtrealm());

            if (StringUtils.isNotBlank(wsfedRequest.getWctx())) {
                builder.addParameter(WSFederationConstants.WCTX, wsfedRequest.getWctx());
            }
            if (StringUtils.isNotBlank(wsfedRequest.getWfresh())) {
                builder.addParameter(WSFederationConstants.WREFRESH, wsfedRequest.getWfresh());
            }
            if (StringUtils.isNotBlank(wsfedRequest.getWhr())) {
                builder.addParameter(WSFederationConstants.WHR, wsfedRequest.getWhr());
            }
            if (StringUtils.isNotBlank(wsfedRequest.getWreq())) {
                builder.addParameter(WSFederationConstants.WREQ, wsfedRequest.getWreq());
            }

            val url = builder.build();

            LOGGER.trace("Built service callback url [{}]", url);
            return org.jasig.cas.client.util.CommonUtils.constructServiceUrl(request, response,
                url.toString(), wsFederationRequestConfigurationContext.getCasProperties().getServer().getName(),
                CasProtocolConstants.PARAMETER_SERVICE,
                CasProtocolConstants.PARAMETER_TICKET, false);
        } catch (final Exception e) {
            throw new SamlException(e.getMessage(), e);
        }
    }

    /**
     * Gets security token from request.
     *
     * @param request the request
     * @return the security token from request
     */
    protected SecurityToken getSecurityTokenFromRequest(final HttpServletRequest request) {
        val cookieValue = wsFederationRequestConfigurationContext.getTicketGrantingTicketCookieGenerator().retrieveCookieValue(request);
        if (StringUtils.isNotBlank(cookieValue)) {
            val tgt = wsFederationRequestConfigurationContext.getTicketRegistry().getTicket(cookieValue, TicketGrantingTicket.class);
            if (tgt != null) {
                val sts = tgt.getDescendantTickets().stream()
                    .filter(t -> t.startsWith(SecurityTokenTicket.PREFIX))
                    .findFirst()
                    .orElse(null);
                if (StringUtils.isNotBlank(sts)) {
                    val stt = wsFederationRequestConfigurationContext.getTicketRegistry().getTicket(sts, SecurityTokenTicket.class);
                    if (stt == null || stt.isExpired()) {
                        LOGGER.warn("Security token ticket [{}] is not found or has expired", sts);
                        return null;
                    }
                    if (stt.getSecurityToken().isExpired()) {
                        LOGGER.warn("Security token linked to ticket [{}] has expired", sts);
                        return null;
                    }
                    return stt.getSecurityToken();
                }
            }
        }
        return null;
    }


    /**
     * Is authentication required?
     *
     * @param fedRequest the fed request
     * @param request    the request
     * @return true/false
     */
    protected boolean shouldRenewAuthentication(final WSFederationRequest fedRequest,
                                                final HttpServletRequest request) {
        if (StringUtils.isBlank(fedRequest.getWfresh()) || NumberUtils.isCreatable(fedRequest.getWfresh())) {
            return false;
        }
        val ttl = Long.parseLong(fedRequest.getWfresh().trim());
        if (ttl == 0) {
            return false;
        }

        val idpToken = getSecurityTokenFromRequest(request);
        if (idpToken == null) {
            return true;
        }

        val ttlMs = TimeUnit.MINUTES.toMillis(ttl);
        if (ttlMs > 0) {
            val createdDate = idpToken.getCreated();
            if (createdDate != null) {
                val expiryDate = new Date();
                expiryDate.setTime(createdDate.toEpochMilli() + ttlMs);
                return expiryDate.before(new Date());
            }
        }
        return false;
    }

    /**
     * Gets ws federation registered service.
     *
     * @param targetService the target service
     * @param fedRequest    the fed request
     * @return the ws federation registered service
     */
    protected WSFederationRegisteredService findAndValidateFederationRequestForRegisteredService(final Service targetService,
                                                                                                 final WSFederationRequest fedRequest) {
        val svc = getWsFederationRegisteredService(targetService);

        val idp = wsFederationRequestConfigurationContext.getCasProperties().getAuthn().getWsfedIdp().getIdp();
        if (StringUtils.isBlank(fedRequest.getWtrealm()) || !StringUtils.equals(fedRequest.getWtrealm(), svc.getRealm())) {
            LOGGER.warn("Realm [{}] is not authorized for matching service [{}]", fedRequest.getWtrealm(), svc);
            throw new UnauthorizedServiceException(UnauthorizedServiceException.CODE_UNAUTHZ_SERVICE, StringUtils.EMPTY);
        }
        if (!StringUtils.equals(idp.getRealm(), svc.getRealm())) {
            LOGGER.warn("Realm [{}] is not authorized for the identity provider realm [{}]", fedRequest.getWtrealm(), idp.getRealm());
            throw new UnauthorizedServiceException(UnauthorizedServiceException.CODE_UNAUTHZ_SERVICE, StringUtils.EMPTY);
        }

        return svc;
    }

    /**
     * Gets ws federation registered service.
     *
     * @param targetService the target service
     * @return the ws federation registered service
     */
    protected WSFederationRegisteredService getWsFederationRegisteredService(final Service targetService) {
        val svc = wsFederationRequestConfigurationContext.getServicesManager().findServiceBy(targetService, WSFederationRegisteredService.class);
        RegisteredServiceAccessStrategyUtils.ensureServiceAccessIsAllowed(targetService, svc);
        return svc;
    }

    /**
     * Handle unauthorized service exception.
     *
     * @param req the req
     * @param ex  the ex
     * @return the model and view
     */
    @ExceptionHandler(Exception.class)
    public ModelAndView handleUnauthorizedServiceException(final HttpServletRequest req, final Exception ex) {
        return WebUtils.produceUnauthorizedErrorView();
    }
}
