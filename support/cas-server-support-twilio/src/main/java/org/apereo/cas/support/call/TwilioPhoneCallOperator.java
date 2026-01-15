package org.apereo.cas.support.call;

import module java.base;
import org.apereo.cas.configuration.model.support.sms.TwilioProperties;
import org.apereo.cas.notifications.call.PhoneCallOperator;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Call;
import com.twilio.twiml.VoiceResponse;
import com.twilio.twiml.voice.Say;
import com.twilio.type.PhoneNumber;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

/**
 * This is {@link TwilioPhoneCallOperator}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Slf4j
public class TwilioPhoneCallOperator implements PhoneCallOperator {
    public TwilioPhoneCallOperator(final TwilioProperties properties) {
        val resolver = SpringExpressionLanguageValueResolver.getInstance();
        Twilio.init(resolver.resolve(properties.getAccountId()), resolver.resolve(properties.getToken()));
    }

    @Override
    public boolean call(final String from, final String to, final String message) {
        try {
            val say = new Say.Builder(message).build();
            val response = new VoiceResponse.Builder().say(say).build();
            val twiml = response.toXml();
            LOGGER.debug("Calling [{}] to say [{}]", to, twiml);
            val call = Call.creator(new PhoneNumber(to), new PhoneNumber(from), twiml).create();
            LOGGER.trace("Phone call result from Twilio: [{}]", call.toString());
            return StringUtils.isNotBlank(call.getSid());
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
        return false;
    }
}
