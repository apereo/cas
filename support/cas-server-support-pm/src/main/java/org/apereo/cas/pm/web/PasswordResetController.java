package org.apereo.cas.pm.web;

import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.pm.PasswordManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.Map;

/**
 * This is {@link PasswordResetController}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Controller("passwordResetController")
public class PasswordResetController {
    @Autowired
    private CasConfigurationProperties casProperties;

    private PasswordManagementService passwordManagementService;

    public PasswordResetController(final PasswordManagementService passwordManagementService) {
        this.passwordManagementService = passwordManagementService;
    }

    /**
     * Handle model and view.
     *
     * @param request  the request
     * @param response the response
     * @param token    the token
     * @return the model and view
     * @throws Exception the exception
     */
    @RequestMapping(method = RequestMethod.GET, value = "/passwordReset/{token:.+}")
    protected ModelAndView handle(final HttpServletRequest request, final HttpServletResponse response,
                                  @PathVariable("token") final String token) throws Exception {
        if (StringUtils.isBlank(token)) {
            throw new RuntimeException("Password reset token is missing");
        }
        final String username = passwordManagementService.parseToken(token);
        if (StringUtils.isBlank(username)) {
            throw new RuntimeException("Password reset token could not be verified");
        }
        final Collection<String> questions = passwordManagementService.getSecurityQuestions(username).keySet();
        
        if (questions.isEmpty()) {
            throw new RuntimeException("No security questions could be found for " + username);
        }
        final Map<String, Object> model = Maps.newHashMap();
        model.put("questions", questions);
        model.put("username", username);
        model.put("token", token);
        return new ModelAndView("casResetPasswordVerifyQuestions", model);
    }

    /**
     * Handle answers model and view.
     *
     * @param request    the request
     * @param response   the response
     * @param token      the token
     * @param uid        the uid
     * @param webRequest the web request
     * @return the model and view
     * @throws Exception the exception
     */
    @RequestMapping(method = RequestMethod.POST, value = "/passwordReset")
    protected ModelAndView handleAnswers(final HttpServletRequest request, 
                                         final HttpServletResponse response,
                                         @RequestParam("token") final String token,
                                         @RequestParam("username") final String uid,
                                         final WebRequest webRequest) throws Exception {
        if (StringUtils.isBlank(token)) {
            throw new RuntimeException("Password reset token is missing");
        }
        final String username = passwordManagementService.parseToken(token);
        if (StringUtils.isBlank(username) || !uid.equals(username)) {
            throw new RuntimeException("Password reset token could not be verified");
        }
        //webRequest.getParameterValues()
        return null;
    }
}
