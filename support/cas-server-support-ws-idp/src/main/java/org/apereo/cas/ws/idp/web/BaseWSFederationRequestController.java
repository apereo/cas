package org.apereo.cas.ws.idp.web;

import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredServiceAccessStrategyUtils;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.ticket.SecurityTokenTicket;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.web.AbstractController;
import org.apereo.cas.web.support.WebUtils;
import org.apereo.cas.ws.idp.WSFederationConstants;
import org.apereo.cas.ws.idp.services.WSFederationRegisteredService;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.cxf.ws.security.tokenstore.SecurityToken;
import org.apache.hc.core5.net.URIBuilder;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * This is {@link BaseWSFederationRequestController}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
@Getter
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class BaseWSFederationRequestController extends AbstractController {
    private final WSFederationRequestConfigurationContext configContext;

    protected String constructServiceUrl(final HttpServletRequest request,
                                         final HttpServletResponse response,
                                         final WSFederationRequest wsfedRequest) throws Exception {
        val builder = new URIBuilder(configContext.getCallbackService().getId());

        builder.addParameter(WSFederationConstants.WA, wsfedRequest.wa());
        builder.addParameter(WSFederationConstants.WREPLY, wsfedRequest.wreply());
        builder.addParameter(WSFederationConstants.WTREALM, wsfedRequest.wtrealm());

        if (StringUtils.isNotBlank(wsfedRequest.wctx())) {
            builder.addParameter(WSFederationConstants.WCTX, wsfedRequest.wctx());
        }
        if (StringUtils.isNotBlank(wsfedRequest.wfresh())) {
            builder.addParameter(WSFederationConstants.WREFRESH, wsfedRequest.wfresh());
        }
        if (StringUtils.isNotBlank(wsfedRequest.whr())) {
            builder.addParameter(WSFederationConstants.WHR, wsfedRequest.whr());
        }
        if (StringUtils.isNotBlank(wsfedRequest.wreq())) {
            builder.addParameter(WSFederationConstants.WREQ, wsfedRequest.wreq());
        }
        val url = builder.build().toASCIIString();
        LOGGER.trace("Built service callback url [{}]", url);
        return url;
    }


    /**
     * Gets security token from request.
     *
     * @param request the request
     * @return the security token from request
     */
    protected SecurityToken getSecurityTokenFromRequest(final HttpServletRequest request) {
        val cookieValue = configContext.getTicketGrantingTicketCookieGenerator().retrieveCookieValue(request);
        if (StringUtils.isNotBlank(cookieValue)) {
            val tgt = configContext.getTicketRegistry().getTicket(cookieValue, TicketGrantingTicket.class);
            if (tgt != null) {
                val sts = tgt.getDescendantTickets().stream()
                    .filter(t -> t.startsWith(SecurityTokenTicket.PREFIX))
                    .findFirst()
                    .orElse(null);
                if (StringUtils.isNotBlank(sts)) {
                    val stt = configContext.getTicketRegistry().getTicket(sts, SecurityTokenTicket.class);
                    if (stt == null || stt.isExpired()) {
                        LOGGER.warn("Security token ticket [{}] is not found or has expired", sts);
                        return null;
                    }
                    if (stt.getSecurityToken() == null || stt.getSecurityToken().isExpired()) {
                        LOGGER.warn("Security token linked to ticket [{}] has expired", sts);
                        return null;
                    }
                    return stt.getSecurityToken();
                }
            }
        }
        return null;
    }


    protected boolean shouldRenewAuthentication(final WSFederationRequest fedRequest,
                                                final HttpServletRequest request) {
        if (StringUtils.isBlank(fedRequest.wfresh()) || !NumberUtils.isCreatable(fedRequest.wfresh())) {
            return false;
        }
        val ttl = Long.parseLong(fedRequest.wfresh().trim());
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
                val expiryDate = new Date(createdDate.toEpochMilli() + ttlMs);
                return expiryDate.before(new Date());
            }
        }
        return false;
    }

    protected WSFederationRegisteredService findAndValidateFederationRequestForRegisteredService(final Service targetService,
                                                                                                 final WSFederationRequest fedRequest) {
        val svc = getWsFederationRegisteredService(targetService);
        if (StringUtils.isBlank(fedRequest.wtrealm()) || !Strings.CI.equals(fedRequest.wtrealm(), svc.getRealm())) {
            LOGGER.warn("Realm [{}] is not authorized for matching service [{}]", fedRequest.wtrealm(), svc);
            throw UnauthorizedServiceException.denied("Rejected: %s".formatted(svc.getRealm()));
        }
        val idp = configContext.getCasProperties().getAuthn().getWsfedIdp().getIdp();
        if (!Strings.CI.equals(idp.getRealm(), svc.getRealm())) {
            LOGGER.warn("Realm [{}] is not authorized for the identity provider realm [{}]", fedRequest.wtrealm(), idp.getRealm());
            throw UnauthorizedServiceException.denied("Rejected: %s".formatted(svc.getRealm()));
        }

        return svc;
    }

    protected WSFederationRegisteredService getWsFederationRegisteredService(final Service targetService) {
        val svc = configContext.getServicesManager().findServiceBy(targetService, WSFederationRegisteredService.class);
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
        return WebUtils.produceUnauthorizedErrorView(ex);
    }
}
