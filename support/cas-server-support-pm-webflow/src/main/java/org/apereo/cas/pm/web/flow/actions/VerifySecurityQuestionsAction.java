package org.apereo.cas.pm.web.flow.actions;

import org.apereo.cas.pm.BasePasswordManagementService;
import org.apereo.cas.pm.PasswordManagementService;
import org.apereo.cas.pm.web.flow.PasswordManagementWebflowUtils;
import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class VerifySecurityQuestionsAction extends AbstractAction {
    private final PasswordManagementService passwordManagementService;

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
        val username = PasswordManagementWebflowUtils.getPasswordResetUsername(requestContext);

        val questions = passwordManagementService.getSecurityQuestions(username);
        val canonicalQuestions = BasePasswordManagementService.canonicalizeSecurityQuestions(questions);
        LOGGER.debug("Canonical security questions are [{}]", canonicalQuestions);

        val index = new AtomicInteger(0);
        val count = canonicalQuestions
            .stream()
            .filter(question -> {
                val answer = request.getParameter("q" + index.getAndIncrement());
                val answerOnRecord = questions.get(question);
                LOGGER.trace("Validating security question [{}] with answer [{}] against provided answer [{}] by username [{}]",
                    question, answerOnRecord, answer, username);
                return passwordManagementService.isValidSecurityQuestionAnswer(username, question, answerOnRecord, answer);
            })
            .count();
        if (count == questions.size()) {
            return success();
        }
        LOGGER.error("Unable to validate answers to all security questions; only validated [{}] question(s) successfully", count);
        return error();
    }
}
