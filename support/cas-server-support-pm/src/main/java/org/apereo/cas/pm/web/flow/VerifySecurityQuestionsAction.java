package org.apereo.cas.pm.web.flow;

import org.apereo.cas.pm.PasswordManagementService;
import org.apereo.cas.web.support.WebUtils;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This is {@link VerifySecurityQuestionsAction}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class VerifySecurityQuestionsAction extends AbstractAction {
    private final PasswordManagementService passwordManagementService;

    public VerifySecurityQuestionsAction(final PasswordManagementService passwordManagementService) {
        this.passwordManagementService = passwordManagementService;
    }

    @Override
    protected Event doExecute(final RequestContext requestContext) throws Exception {
        final HttpServletRequest request = WebUtils.getHttpServletRequest(requestContext);
        final String username = requestContext.getFlowScope().getString("username");

        final Map<String, String> questions = passwordManagementService.getSecurityQuestions(username);

        final AtomicInteger i = new AtomicInteger(0);
        final long c = questions.values().stream().filter(v -> {
            final String answer = request.getParameter("q" + i.getAndIncrement());
            return answer.equals(v);
        }).count();
        if (c == questions.size()) {
            return success();
        }
        return error();
    }
}
