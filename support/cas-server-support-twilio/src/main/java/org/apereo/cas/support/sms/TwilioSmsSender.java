package org.apereo.cas.support.sms;

import module java.base;
import org.apereo.cas.configuration.model.support.sms.TwilioProperties;
import org.apereo.cas.notifications.sms.SmsSender;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

/**
 * This is {@link TwilioSmsSender}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
public class TwilioSmsSender implements SmsSender {
    public TwilioSmsSender(final TwilioProperties properties) {
        val resolver = SpringExpressionLanguageValueResolver.getInstance();
        Twilio.init(resolver.resolve(properties.getAccountId()), resolver.resolve(properties.getToken()));
    }

    @Override
    public boolean send(final String from, final String to, final String message) {
        try {
            val msg = Message.creator(new PhoneNumber(to), new PhoneNumber(from), message).create();
            LOGGER.trace("SMS result from Twilio: [{}]", msg.toString());
            return StringUtils.isNotBlank(msg.getSid());
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
        return false;
    }
}


