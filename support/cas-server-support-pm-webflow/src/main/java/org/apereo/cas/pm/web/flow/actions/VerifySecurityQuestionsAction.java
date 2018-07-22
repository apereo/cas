package org.apereo.cas.pm.web.flow.actions;

import org.apereo.cas.pm.BasePasswordManagementService;
import org.apereo.cas.pm.PasswordManagementService;
import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * This is {@link VerifySecurityQuestionsAction}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiredArgsConstructor
public class VerifySecurityQuestionsAction extends AbstractAction {
    private final PasswordManagementService passwordManagementService;

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
        val username = requestContext.getFlowScope().getString("username");

        val questions = passwordManagementService.getSecurityQuestions(username);
        val canonicalQuestions = BasePasswordManagementService.canonicalizeSecurityQuestions(questions);
        val i = new AtomicInteger(0);
        val c = canonicalQuestions
            .stream()
            .filter(q -> {
                val answer = request.getParameter("q" + i.getAndIncrement());
                return passwordManagementService.isValidSecurityQuestionAnswer(username, q, questions.get(q), answer);
            })
            .count();
        if (c == questions.size()) {
            return success();
        }
        return error();
    }
}
