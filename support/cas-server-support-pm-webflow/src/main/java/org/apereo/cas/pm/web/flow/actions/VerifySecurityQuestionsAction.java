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
        LOGGER.debug("Canonical security questions are [{}]", canonicalQuestions);
        
        final AtomicInteger i = new AtomicInteger(0);
        final long c = canonicalQuestions
            .stream()
            .filter(q -> {
                final String answer = request.getParameter("q" + i.getAndIncrement());
                final String answerOnRecord = questions.get(q);
                final boolean result = passwordManagementService.isValidSecurityQuestionAnswer(username, q, answerOnRecord, answer);
                LOGGER.trace("Validating security question [{}] with answer [{}] against provided answer [{}] by username [{}]: [{}]",
                    q, answerOnRecord, answer, username, result);
                return result;
            })
            .count();
        
        if (c == questions.size()) {
            return success();
        }
        LOGGER.error("Unable to validate answers to all security questions; only validated [{}] question(s) successfully", c);
        return error();
    }
}
