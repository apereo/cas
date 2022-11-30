package org.springframework.context.support;

import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Slf4j
public class StaticMessageSource extends AbstractMessageSource {

    private final Map<String, Map<Locale, MessageHolder>> messageMap = new HashMap<>();

    @Override
    protected String getMessageInternal(final String code, final Object[] args, final Locale locale) {
        LOGGER.info("Getting message for code {} with args {} and locale {}", code, args, locale);
        LOGGER.info("Using message format {}", isAlwaysUseMessageFormat());
        return super.getMessageInternal(code, args, locale);
    }

    @Override
    @Nullable
    protected String resolveCodeWithoutArguments(String code, Locale locale) {
        LOGGER.info("Checking for code {}", code);
        Map<Locale, MessageHolder> localeMap = this.messageMap.get(code);

        LOGGER.info("Found locale map {}", localeMap);
        if (localeMap == null) {
            return null;
        }

        LOGGER.info("Getting locale map for {}", locale);
        MessageHolder holder = localeMap.get(locale);
        LOGGER.info("Found locale holder for {}", holder);

        if (holder == null) {
            return null;
        }
        return holder.getMessage();
    }

    @Override
    @Nullable
    protected MessageFormat resolveCode(String code, Locale locale) {
        Map<Locale, MessageHolder> localeMap = this.messageMap.get(code);
        if (localeMap == null) {
            return null;
        }
        MessageHolder holder = localeMap.get(locale);
        if (holder == null) {
            return null;
        }
        return holder.getMessageFormat();
    }

    /**
     * Associate the given message with the given code.
     * @param code the lookup code
     * @param locale the locale that the message should be found within
     * @param msg the message associated with this lookup code
     */
    public void addMessage(String code, Locale locale, String msg) {
        Assert.notNull(code, "Code must not be null");
        Assert.notNull(locale, "Locale must not be null");
        Assert.notNull(msg, "Message must not be null");
        this.messageMap.computeIfAbsent(code, key -> new HashMap<>(4)).put(locale, new MessageHolder(msg, locale));
        logger.debug("Added message [" + msg + "] for code [" + code + "] and Locale [" + locale + ']');
    }

    /**
     * Associate the given message values with the given keys as codes.
     * @param messages the messages to register, with messages codes
     * as keys and message texts as values
     * @param locale the locale that the messages should be found within
     */
    public void addMessages(Map<String, String> messages, Locale locale) {
        Assert.notNull(messages, "Messages Map must not be null");
        messages.forEach((code, msg) -> addMessage(code, locale, msg));
    }


    @Override
    public String toString() {
        return getClass().getName() + ": " + this.messageMap;
    }


    private class MessageHolder {

        private final String message;

        private final Locale locale;

        @Nullable
        private volatile MessageFormat cachedFormat;

        public MessageHolder(String message, Locale locale) {
            this.message = message;
            this.locale = locale;
        }

        public String getMessage() {
            return this.message;
        }

        public MessageFormat getMessageFormat() {
            MessageFormat messageFormat = this.cachedFormat;
            if (messageFormat == null) {
                messageFormat = createMessageFormat(this.message, this.locale);
                this.cachedFormat = messageFormat;
            }
            return messageFormat;
        }

        @Override
        public String toString() {
            return this.message;
        }
    }

}
