package org.apereo.cas.sms;

import org.apereo.cas.configuration.model.support.email.MailjetProperties;
import org.apereo.cas.notifications.sms.SmsSender;
import org.apereo.cas.util.LoggingUtils;
import com.mailjet.client.MailjetClient;
import com.mailjet.client.MailjetRequest;
import com.mailjet.client.resource.sms.SmsSend;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.http.HttpStatus;

/**
 * This is {@link MailjetSmsSender}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@RequiredArgsConstructor
@Slf4j
public class MailjetSmsSender implements SmsSender {
    protected final MailjetClient mailjetClient;
    protected final MailjetProperties properties;

    @Override
    public boolean send(final String from, final String to, final String message) {
        try {
            val mailjetRequest = new MailjetRequest(SmsSend.resource)
                .property(SmsSend.FROM, from)
                .property(SmsSend.TO, to)
                .property(SmsSend.TEXT, message);
            val response = mailjetClient.post(mailjetRequest);
            LOGGER.debug("SMS Response: [{}]", response.getData().getJSONObject(0).toString());
            return response.getStatus() == HttpStatus.OK.value();
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
        return false;
    }
}
