package org.apereo.cas.pm.web.flow.actions;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.pm.PasswordManagementProperties;
import org.apereo.cas.pm.BasePasswordManagementService;
import org.apereo.cas.pm.PasswordManagementService;
import org.apereo.cas.web.support.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import javax.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This is {@link VerifySecurityQuestionsAction}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class VerifySecurityQuestionsAction extends AbstractAction {
    private static final Logger LOGGER = LoggerFactory.getLogger(VerifySecurityQuestionsAction.class);
    
    private final PasswordManagementService passwordManagementService;

    @Autowired
    private CasConfigurationProperties casProperties;
    
    public VerifySecurityQuestionsAction(final PasswordManagementService passwordManagementService) {
        this.passwordManagementService = passwordManagementService;
    }

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        final HttpServletRequest request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
        final String username = requestContext.getFlowScope().getString("username");
        final PasswordManagementProperties pm = casProperties.getAuthn().getPm();

        if (!pm.getReset().isSecurityQuestionsEnabled()) {
            LOGGER.debug("Security questions are not enabled");
            return success();
        }
        
        final Map<String, String> questions = passwordManagementService.getSecurityQuestions(username);
        final List<String> canonicalQuestions = BasePasswordManagementService.canonicalizeSecurityQuestions(questions);
        final AtomicInteger i = new AtomicInteger(0);
        final long c = canonicalQuestions.stream().filter(q -> {
            final String answer = request.getParameter("q" + i.getAndIncrement());
            return passwordManagementService.isValidSecurityQuestionAnswer(username, q, questions.get(q), answer);
        }).count();
        if (c == questions.size()) {
            return success();
        }
        return error();
    }
}
