package org.apereo.cas.web.view;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.multitenancy.TenantExtractor;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.springframework.context.MessageSource;
import org.springframework.context.support.AbstractResourceBasedMessageSource;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import java.util.Locale;

/**
 * An extension of the {@link ReloadableResourceBundleMessageSource}.
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
@RequiredArgsConstructor
public class CasReloadableMessageBundle extends ReloadableResourceBundleMessageSource {
    private final TenantExtractor tenantExtractor;

    @Override
    protected String getMessageInternal(final String code, final Object[] args, final Locale locale) {
        val clientInfo = ClientInfoHolder.getClientInfo();
        if (clientInfo != null && StringUtils.isNotBlank(clientInfo.getTenant())) {
            val tenantDefinition = tenantExtractor.getTenantsManager().findTenant(clientInfo.getTenant()).orElseThrow();
            val boundProperties = tenantDefinition.bindProperties();
            if (boundProperties.isPresent()) {
                val properties = boundProperties.get();
                properties.getMessageBundle().getBaseNames().addAll(getBasenameSet());
                val bean = configure(new ReloadableResourceBundleMessageSource(), properties);
                return bean.getMessage(code, args, locale);
            }
        }
        return super.getMessageInternal(code, args, locale);
    }

    /**
     * Configure message source.
     *
     * @param bean          the bean
     * @param casProperties the cas properties
     * @return the message source
     */
    public static MessageSource configure(final AbstractResourceBasedMessageSource bean,
                                          final CasConfigurationProperties casProperties) {
        val mb = casProperties.getMessageBundle();
        bean.setDefaultEncoding(mb.getEncoding());
        bean.setCacheSeconds(Long.valueOf(Beans.newDuration(mb.getCacheSeconds()).toSeconds()).intValue());
        bean.setFallbackToSystemLocale(mb.isFallbackSystemLocale());
        bean.setUseCodeAsDefaultMessage(mb.isUseCodeMessage());
        bean.setBasenames(mb.getBaseNames().toArray(ArrayUtils.EMPTY_STRING_ARRAY));
        return bean;
    }
}
