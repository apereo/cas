package org.jasig.cas.support.oauth.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang.StringUtils;
import org.jasig.cas.services.ServicesManager;
import org.jasig.cas.ticket.registry.TicketRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

/**
 * This controller is the base controller for wrapping OAuth protocol in CAS.
 * 
 * @author Jerome Leleu
 */
public abstract class BaseOAuthWrapperController extends AbstractController implements InitializingBean {
    
    protected static final Logger logger = LoggerFactory.getLogger(BaseOAuthWrapperController.class);
    
    protected AbstractController authorizeController;
    
    protected AbstractController callbackAuthorizeController;
    
    protected AbstractController accessTokenController;
    
    protected AbstractController profileController;
    
    @NotNull
    protected String loginUrl;
    
    @NotNull
    protected ServicesManager servicesManager;
    
    @NotNull
    protected TicketRegistry ticketRegistry;
    
    @NotNull
    protected long timeout;
    
    public void afterPropertiesSet() throws Exception {
        initController();
    }
    
    protected abstract void initController();
    
    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response)
        throws Exception {
        
        String method = getMethod(request);
        logger.debug("method : {}", method);
        return internalHandleRequest(method, request, response);
    }
    
    protected abstract ModelAndView internalHandleRequest(String method, HttpServletRequest request,
                                                          HttpServletResponse response) throws Exception;
    
    private String getMethod(HttpServletRequest request) {
        String method = request.getRequestURI();
        if (method.indexOf("?") >= 0) {
            method = StringUtils.substringBefore(method, "?");
        }
        int pos = method.lastIndexOf("/");
        if (pos >= 0) {
            method = method.substring(pos + 1);
        }
        return method;
    }
    
    public void setServicesManager(ServicesManager servicesManager) {
        this.servicesManager = servicesManager;
    }
    
    public void setTicketRegistry(TicketRegistry ticketRegistry) {
        this.ticketRegistry = ticketRegistry;
    }
    
    public void setLoginUrl(String loginUrl) {
        this.loginUrl = loginUrl;
    }
    
    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }
}
