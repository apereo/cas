package org.apereo.cas.web.view;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

import java.util.Locale;
import java.util.stream.IntStream;

/**
 * An extension of the {@link ReloadableResourceBundleMessageSource} whose sole concern
 * is to print a WARN message in cases where a language key is not found in the active and
 * default bundles.
 * 
 * <p>Note: By default, if a key not found in a localized bundle, Spring will auto-fallback
 * to the default bundle that is {@code messages.properties}. However, if the key is also
 * not found in the default bundle, and {@link #setUseCodeAsDefaultMessage(boolean)}
 * is set to true, then only the requested code itself will be used as the message to display.
 * In this case, the class will issue a WARN message instructing the caller that the bundle
 * needs further attention. If {@link #setUseCodeAsDefaultMessage(boolean)} is set to false,
 * only then a {@code null} value will be returned, which subsequently causes an instance
 * of {@link org.springframework.context.NoSuchMessageException} to be thrown.
 * 
 * @author Misagh Moayyed
 * @since 4.0.0
 */
public class CasReloadableMessageBundle extends ReloadableResourceBundleMessageSource {

    private static final Logger LOGGER = LoggerFactory.getLogger(CasReloadableMessageBundle.class);

    private String[] basenames;

    @Override
    protected String getDefaultMessage(final String code) {
        final String messageToReturn = super.getDefaultMessage(code);
        if (!StringUtils.isBlank(messageToReturn) && messageToReturn.equals(code)) {
            LOGGER.warn("The code [{}] cannot be found in the default language bundle and will "
                    + "be used as the message itself.", code);
        }
        return messageToReturn;
    }

    @Override
    protected String getMessageInternal(final String code, final Object[] args, final Locale locale) {
        final boolean foundCode;
        
        if (!locale.equals(Locale.ENGLISH)) {
            foundCode = IntStream.range(0, this.basenames.length).filter(i -> {
                final String filename = this.basenames[i] + '_' + locale;

                LOGGER.trace("Examining language bundle [{}] for the code [{}]", filename, code);
                final PropertiesHolder holder = this.getProperties(filename);
                return holder != null && holder.getProperties() != null
                        && holder.getProperty(code) != null;
            }).findFirst().isPresent();

            if (!foundCode) {
                LOGGER.trace("The code [{}] cannot be found in the language bundle for the locale [{}]", code, locale);
            }
        }
        return super.getMessageInternal(code, args, locale);
    }

    @Override
    public void setBasenames(final String... basenames) {
        this.basenames = basenames;
        super.setBasenames(basenames);
    }

}
