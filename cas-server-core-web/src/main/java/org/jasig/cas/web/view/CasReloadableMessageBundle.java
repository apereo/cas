package org.jasig.cas.web.view;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.stereotype.Component;

import java.util.Locale;

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
@Component("messageSource")
public class CasReloadableMessageBundle extends ReloadableResourceBundleMessageSource {

    private String[] basenames;
    
    private final transient Logger logger = LoggerFactory.getLogger(this.getClass());
    
    @Override
    protected String getDefaultMessage(final String code) {
        final String messageToReturn = super.getDefaultMessage(code);
        if (!StringUtils.isBlank(messageToReturn) && messageToReturn.equals(code)) {
            logger.warn("The code [{}] cannot be found in the default language bundle and will "
                    + "be used as the message itself.", code);
        }
        return messageToReturn;
    }

    @Override
    protected String getMessageInternal(final String code, final Object[] args, final Locale locale) {
        boolean foundCode = false;
        
        if (!locale.equals(Locale.ENGLISH)) {
          for (int i = 0; !foundCode && i < this.basenames.length; i++) {
              final String filename = this.basenames[i] + '_' + locale;
              
              logger.debug("Examining language bundle [{}] for the code [{}]", filename, code);
              final PropertiesHolder holder = this.getProperties(filename);
              foundCode =  holder != null && holder.getProperties() != null
                                     && holder.getProperty(code) != null;  
          }       
          
          if (!foundCode) {
              logger.debug("The code [{}] cannot be found in the language bundle for the locale [{}]",
                      code, locale);
          }
        }
        return super.getMessageInternal(code, args, locale);
    }

    @Override
    @Autowired
    public void setBasenames(
        @Value("#{T(java.util.Arrays)"
            + ".asList('${message.bundle.basenames:classpath:custom_messages,classpath:messages}')}")
             final String... basenames) {
        this.basenames = basenames;
        super.setBasenames(basenames);
    }

    @Override
    @Autowired
    public void setDefaultEncoding(@Value("${message.bundle.encoding:UTF-8}")
                                   final String defaultEncoding) {
        super.setDefaultEncoding(defaultEncoding);
    }

    @Override
    @Autowired
    public void setCacheSeconds(@Value("${message.bundle.cacheseconds:180}")
                                final int cacheSeconds) {
        super.setCacheSeconds(cacheSeconds);
    }

    @Override
    @Autowired
    public void setFallbackToSystemLocale(@Value("${message.bundle.fallback.systemlocale:false}")
                                          final boolean fallbackToSystemLocale) {
        super.setFallbackToSystemLocale(fallbackToSystemLocale);
    }

    @Override
    @Autowired
    public void setUseCodeAsDefaultMessage(@Value("${message.bundle.usecode.message:true}")
                                           final boolean useCodeAsDefaultMessage) {
        super.setUseCodeAsDefaultMessage(useCodeAsDefaultMessage);
    }
}
