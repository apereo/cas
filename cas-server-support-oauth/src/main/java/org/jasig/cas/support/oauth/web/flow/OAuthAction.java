package org.jasig.cas.support.oauth.web.flow;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang.StringUtils;
import org.jasig.cas.CentralAuthenticationService;
import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.support.oauth.authentication.principal.OAuthCredentials;
import org.jasig.cas.support.oauth.provider.OAuthProvider;
import org.jasig.cas.ticket.TicketException;
import org.jasig.cas.web.support.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This class represents an action in the webflow to retrieve OAuth information on the callback url which is the webflow url (/login). The
 * oauth_provider and the other parameters (code, oauth_token, oauth_verifier) are expected after OAuth authentication. Providers are
 * defined by configuration. The service is stored and retrieved from web session after OAuth authentication.
 * 
 * @author Jerome Leleu
 */
public class OAuthAction extends AbstractAction {
    
    private static final Logger logger = LoggerFactory.getLogger(OAuthAction.class);
    
    @NotNull
    private List<OAuthProvider> providers;
    
    @NotNull
    private CentralAuthenticationService centralAuthenticationService;
    
    @Override
    protected Event doExecute(RequestContext context) throws Exception {
        HttpServletRequest request = WebUtils.getHttpServletRequest(context);
        HttpSession session = request.getSession();
        
        // get provider name
        String providerName = request.getParameter("oauth_provider");
        logger.debug("providerName : {}", providerName);
        
        // it's an authentication
        if (StringUtils.isNotBlank(providerName)) {
            // get provider
            OAuthProvider provider = null;
            for (OAuthProvider aProvider : providers) {
                if (StringUtils.equals(providerName, aProvider.getName())) {
                    provider = aProvider;
                    break;
                }
            }
            
            // get token and verifier
            String token = provider.extractTokenFromRequest(request);
            String verifier = provider.extractVerifierFromRequest(request);
            logger.debug("token : {}", token);
            logger.debug("verifier : {}", verifier);
            
            // retrieve service from session and put it into webflow
            Service service = (Service) session.getAttribute("service");
            context.getFlowScope().put("service", service);
            
            // create credentials
            Credentials credentials = new OAuthCredentials(providerName, provider.getClass().getName(), token, verifier);
            
            try {
                WebUtils.putTicketGrantingTicketInRequestScope(context, this.centralAuthenticationService
                    .createTicketGrantingTicket(credentials));
                return success();
            } catch (final TicketException e) {
                return error();
            }
        } else {
            // no authentication : go to login page
            
            // put service in session from flow scope
            Service service = (Service) context.getFlowScope().get("service");
            session.setAttribute("service", service);
            
            // for all providers, generate authorization urls
            for (OAuthProvider provider : providers) {
                String key = provider.getName() + "_authorizationUrl";
                String authorizatonUrl = provider.getAuthorizationUrl(session);
                logger.debug("key : {} -> authorizationUrl : {}", key, authorizatonUrl);
                request.setAttribute(key, authorizatonUrl);
            }
        }
        
        return error();
    }
    
    public void setCentralAuthenticationService(CentralAuthenticationService centralAuthenticationService) {
        this.centralAuthenticationService = centralAuthenticationService;
    }
    
    public void setProviders(List<OAuthProvider> providers) {
        this.providers = providers;
    }
}
