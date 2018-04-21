package org.apereo.cas.pm.web.flow.actions;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.pm.BasePasswordManagementService;
import org.apereo.cas.pm.PasswordManagementService;
import org.apereo.cas.web.support.WebUtils;
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
@Slf4j
@RequiredArgsConstructor
public class VerifySecurityQuestionsAction extends AbstractAction {
    private final PasswordManagementService passwordManagementService;

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        final HttpServletRequest request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
        final String username = requestContext.getFlowScope().getString("username");

        final Map<String, String> questions = passwordManagementService.getSecurityQuestions(username);
        final List<String> canonicalQuestions = BasePasswordManagementService.canonicalizeSecurityQuestions(questions);
        final AtomicInteger i = new AtomicInteger(0);
        final long c = canonicalQuestions
            .stream()
            .filter(q -> {
                final String answer = request.getParameter("q" + i.getAndIncrement());
                return passwordManagementService.isValidSecurityQuestionAnswer(username, q, questions.get(q), answer);
            })
            .count();
        if (c == questions.size()) {
            return success();
        }
        return error();
    }
}
