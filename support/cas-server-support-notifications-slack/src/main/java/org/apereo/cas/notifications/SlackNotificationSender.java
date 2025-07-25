package org.apereo.cas.notifications;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.configuration.model.support.slack.SlackMessagingProperties;
import org.apereo.cas.notifications.push.NotificationSender;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;
import com.slack.api.Slack;
import com.slack.api.SlackConfig;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.request.chat.ChatPostMessageRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.Strings;
import org.jooq.lambda.Unchecked;
import java.util.List;
import java.util.Map;

/**
 * This is {@link SlackNotificationSender}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@RequiredArgsConstructor
@Slf4j
public class SlackNotificationSender implements NotificationSender {
    private final MethodsClient slackMethods;
    private final SlackMessagingProperties slackProperties;

    public SlackNotificationSender(final SlackMessagingProperties slackProperties) {
        this.slackProperties = slackProperties;
        val config = new SlackConfig();
        config.setPrettyResponseLoggingEnabled(true);
        val slack = Slack.getInstance(config);
        this.slackMethods = slack.methods(SpringExpressionLanguageValueResolver.getInstance().resolve(slackProperties.getApiToken()));
    }

    @Override
    public boolean notify(final Principal principal, final Map<String, String> messageData) {
        val body = buildNotificationMessageBody(messageData);
        val slackUsernames = FunctionUtils.doIfNotBlank(slackProperties.getUsernameAttribute(),
            () -> principal.getAttributes().get(slackProperties.getUsernameAttribute()),
            () -> List.of(principal.getId()));
        return slackUsernames
            .stream()
            .allMatch(Unchecked.predicate(slackUsername -> {
                val channel = Strings.CI.prependIfMissing(slackUsername.toString(), "@");
                val messageRequest = ChatPostMessageRequest.builder()
                    .channel(channel)
                    .text(body)
                    .linkNames(true)
                    .mrkdwn(true)
                    .build();
                val response = slackMethods.chatPostMessage(messageRequest);
                LOGGER.trace(response.toString());
                FunctionUtils.doIfNotBlank(response.getError(),
                    __ -> LoggingUtils.error(LOGGER, "Error: %s, Provided: %s, Needed: %s"
                        .formatted(response.getError(), response.getProvided(), response.getNeeded())));
                FunctionUtils.doIfNotBlank(response.getWarning(), LOGGER::warn);
                return response.isOk();
            }));
    }

    protected String buildNotificationMessageBody(final Map<String, String> messageData) {
        return "*%s*\n\n%s".strip().formatted(
            messageData.get(NotificationSender.ATTRIBUTE_NOTIFICATION_TITLE),
            messageData.get(NotificationSender.ATTRIBUTE_NOTIFICATION_MESSAGE));
    }
}
