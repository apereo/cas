package org.apereo.cas.pm.web;

import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.pm.PasswordManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
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
        final Collection<String> questions = passwordManagementService.getSecurityQuestions();
        
        if (questions.isEmpty()) {
            throw new RuntimeException("No security questions could be found for " + username);
        }
        final Map<String, Object> model = Maps.newHashMap();
        model.put("questions", questions);
        model.put("username", username);
        return new ModelAndView("casResetPasswordVerifyQuestions", model);
    }
}
