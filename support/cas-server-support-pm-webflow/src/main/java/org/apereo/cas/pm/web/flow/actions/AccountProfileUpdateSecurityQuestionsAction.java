package org.apereo.cas.pm.web.flow.actions;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.pm.PasswordManagementQuery;
import org.apereo.cas.pm.PasswordManagementService;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;
import org.apereo.cas.web.support.WebUtils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.binding.message.MessageBuilder;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
    protected Event doExecute(final RequestContext requestContext) throws Exception {
        val messages = requestContext.getMessageContext();
        try {
            val requestParameters = requestContext.getRequestParameters();
            val questions = Arrays.stream(requestParameters.getRequiredArray("questions", String.class))
                .distinct()
                .collect(Collectors.toList());
            val answers = Arrays.stream(requestParameters.getRequiredArray("answers", String.class))
                .distinct()
                .collect(Collectors.toList());
            FunctionUtils.throwIf(questions.size() != answers.size(),
                () -> new IllegalArgumentException("Security questions do not match the given answers"));
            val securityQuestions = new LinkedMultiValueMap<String, String>();
            for (var i = 0; i < questions.size(); i++) {
                val question = questions.get(i).trim();
                val answer = answers.get(i).trim();
                if (StringUtils.isNotBlank(question) && StringUtils.isNotBlank(answer)
                    && !StringUtils.equalsIgnoreCase(question, answer)
                    && question.length() >= INPUT_LENGTH_MINIMUM
                    && answer.length() >= INPUT_LENGTH_MINIMUM) {
                    securityQuestions.put(question, List.of(answer));
                }
            }
            FunctionUtils.throwIf(securityQuestions.isEmpty(),
                () -> new IllegalArgumentException("Security questions cannot be empty or unspecified"));
            val tgt = WebUtils.getTicketGrantingTicket(requestContext);
            val principal = tgt.getAuthentication().getPrincipal();
            val query = PasswordManagementQuery.builder()
                .username(principal.getId())
                .securityQuestions(securityQuestions)
                .build();
            LOGGER.debug("Updating security questions for [{}]", query);
            passwordManagementService.updateSecurityQuestions(query);
            messages.addMessage(new MessageBuilder().info().code(CODE_SUCCESS).build());
            return success();
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
            messages.addMessage(new MessageBuilder().error().code(CODE_FAILURE).build());
            return error();
        }
    }
}
