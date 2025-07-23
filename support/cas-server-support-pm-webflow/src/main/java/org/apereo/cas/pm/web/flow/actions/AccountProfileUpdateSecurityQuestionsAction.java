package org.apereo.cas.pm.web.flow.actions;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.pm.PasswordManagementQuery;
import org.apereo.cas.pm.PasswordManagementService;
import org.apereo.cas.ticket.AuthenticationAwareTicket;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;
import org.apereo.cas.web.support.WebUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import java.util.Arrays;
import java.util.List;

/**
 * This is {@link AccountProfileUpdateSecurityQuestionsAction}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@Slf4j
@RequiredArgsConstructor
@Getter
public class AccountProfileUpdateSecurityQuestionsAction extends BaseCasWebflowAction {
    private static final long INPUT_LENGTH_MINIMUM = 5;

    private static final String CODE_FAILURE = "screen.account.securityquestions.failure";

    private static final String CODE_SUCCESS = "screen.account.securityquestions.success";

    private final PasswordManagementService passwordManagementService;

    private final CasConfigurationProperties casProperties;

    @Override
    protected Event doExecuteInternal(final RequestContext requestContext) {
        try {
            val requestParameters = requestContext.getRequestParameters();
            val questions = Arrays.stream(requestParameters.getRequiredArray("questions", String.class))
                .distinct().toList();
            val answers = Arrays.stream(requestParameters.getRequiredArray("answers", String.class))
                .distinct().toList();
            FunctionUtils.throwIf(questions.size() != answers.size(),
                () -> new IllegalArgumentException("Security questions do not match the given answers"));
            val securityQuestions = new LinkedMultiValueMap<String, String>();
            for (var i = 0; i < questions.size(); i++) {
                val question = questions.get(i).trim();
                val answer = answers.get(i).trim();
                if (StringUtils.isNotBlank(question) && StringUtils.isNotBlank(answer)
                    && !Strings.CI.equals(question, answer)
                    && question.length() >= INPUT_LENGTH_MINIMUM
                    && answer.length() >= INPUT_LENGTH_MINIMUM) {
                    securityQuestions.put(question, List.of(answer));
                }
            }
            FunctionUtils.throwIf(securityQuestions.isEmpty(),
                () -> new IllegalArgumentException("Security questions cannot be empty or unspecified"));
            val tgt = WebUtils.getTicketGrantingTicket(requestContext);
            if (tgt instanceof final AuthenticationAwareTicket aat) {
                val principal = aat.getAuthentication().getPrincipal();
                val query = PasswordManagementQuery.builder()
                    .username(principal.getId())
                    .securityQuestions(securityQuestions)
                    .build();
                LOGGER.debug("Updating security questions for [{}]", query);
                passwordManagementService.updateSecurityQuestions(query);
                WebUtils.addInfoMessageToContext(requestContext, CODE_SUCCESS);
                return success();
            }
        } catch (final Throwable e) {
            LoggingUtils.error(LOGGER, e);
            WebUtils.addErrorMessageToContext(requestContext, CODE_FAILURE);
        }
        return error();
    }
}
