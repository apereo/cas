package org.apereo.cas.pm.web.flow.actions;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.pm.BasePasswordManagementService;
import org.apereo.cas.pm.PasswordManagementService;
import org.apereo.cas.web.support.WebUtils;
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
@Slf4j
@RequiredArgsConstructor
public class VerifySecurityQuestionsAction extends AbstractAction {
    private final PasswordManagementService passwordManagementService;

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        final var request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
        final var username = requestContext.getFlowScope().getString("username");

        final var questions = passwordManagementService.getSecurityQuestions(username);
        final var canonicalQuestions = BasePasswordManagementService.canonicalizeSecurityQuestions(questions);
        final var i = new AtomicInteger(0);
        final var c = canonicalQuestions
            .stream()
            .filter(q -> {
                final var answer = request.getParameter("q" + i.getAndIncrement());
                return passwordManagementService.isValidSecurityQuestionAnswer(username, q, questions.get(q), answer);
            })
            .count();
        if (c == questions.size()) {
            return success();
        }
        return error();
    }
}
