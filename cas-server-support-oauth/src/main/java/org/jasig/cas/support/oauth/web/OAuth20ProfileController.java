package org.jasig.cas.support.oauth.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.jasig.cas.support.oauth.OAuthConstants;
import org.jasig.cas.support.oauth.OAuthUtils;
import org.jasig.cas.ticket.TicketGrantingTicketImpl;
import org.jasig.cas.ticket.registry.TicketRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

public class OAuth20ProfileController extends AbstractController {
    
    private static final Logger logger = LoggerFactory.getLogger(OAuth20ProfileController.class);
    
    private TicketRegistry ticketRegistry;
    
    public OAuth20ProfileController(TicketRegistry ticketRegistry) {
        this.ticketRegistry = ticketRegistry;
    }
    
    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response)
        throws Exception {
        String accessToken = request.getParameter(OAuthConstants.ACCESS_TOKEN);
        logger.debug("accessToken : {}", accessToken);
        
        // accessToken is required
        if (StringUtils.isBlank(accessToken)) {
            logger.error("missing accessToken");
            return OAuthUtils.writeXmlError(response, "missing_accessToken");
        }
        
        // get ticket granting ticket
        TicketGrantingTicketImpl ticketGrantingTicketImpl = (TicketGrantingTicketImpl) ticketRegistry
            .getTicket(accessToken);
        if (ticketGrantingTicketImpl == null || ticketGrantingTicketImpl.isExpired()) {
            logger.error("expired accessToken : {}", accessToken);
            return OAuthUtils.writeXmlError(response, "expired_accessToken");
        }
        
        String xml = "<profile><id>" + ticketGrantingTicketImpl.getAuthentication().getPrincipal().getId()
                     + "</id></profile>";
        logger.debug("xml : {}", xml);
        return OAuthUtils.writeXml(response, xml);
    }
}
