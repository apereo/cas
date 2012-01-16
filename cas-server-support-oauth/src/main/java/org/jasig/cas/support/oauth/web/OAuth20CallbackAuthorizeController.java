package org.jasig.cas.support.oauth.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.jasig.cas.support.oauth.OAuthConstants;
import org.jasig.cas.support.oauth.OAuthUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

public class OAuth20CallbackAuthorizeController extends AbstractController {
    
    private static final Logger logger = LoggerFactory.getLogger(OAuth20CallbackAuthorizeController.class);
    
    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response)
        throws Exception {
        // get CAS ticket
        String ticket = request.getParameter("ticket");
        logger.debug("ticket : {}", ticket);
        
        // retrieve callback url from session
        HttpSession session = request.getSession();
        String callbackUrl = (String) session.getAttribute(OAuthConstants.OAUTH20_CALLBACKURL);
        logger.debug("callbackUrl : {}", callbackUrl);
        session.removeAttribute(OAuthConstants.OAUTH20_CALLBACKURL);
        
        // return to callback with code
        String callbackUrlWithCode = OAuthUtils.addParameter(callbackUrl, "code", ticket);
        logger.debug("callbackUrlWithCode : {}", callbackUrlWithCode);
        return OAuthUtils.redirectTo(callbackUrlWithCode);
    }
}
