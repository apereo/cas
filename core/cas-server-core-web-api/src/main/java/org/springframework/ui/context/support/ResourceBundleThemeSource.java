package org.springframework.ui.context.support;

import org.springframework.ui.context.HierarchicalThemeSource;
import org.springframework.ui.context.Theme;
import org.springframework.ui.context.ThemeSource;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.context.HierarchicalMessageSource;
import org.springframework.context.MessageSource;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.lang.Nullable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * {@link ThemeSource} implementation that looks up an individual
 * {@link java.util.ResourceBundle} per theme. The theme name gets
 * interpreted as ResourceBundle basename, supporting a common
 * basename prefix for all themes.
 *
 * @author Jean-Pierre Pawlak
 * @author Juergen Hoeller
 * @since 7.3.0
 */
@Slf4j
public class ResourceBundleThemeSource implements HierarchicalThemeSource, BeanClassLoaderAware {

    @Nullable
    @Getter
    private ThemeSource parentThemeSource;

    private String basenamePrefix = StringUtils.EMPTY;

    @Nullable
    @Setter
    private String defaultEncoding;

    @Nullable
    @Setter
    private Boolean fallbackToSystemLocale;

    @Nullable
    @Setter
    private ClassLoader beanClassLoader;

    /** Map from theme name to Theme instance. */
    private final Map<String, Theme> themeCache = new ConcurrentHashMap<>();


    @Override
    public void setParentThemeSource(@Nullable final ThemeSource parent) {
        this.parentThemeSource = parent;
        synchronized (this.themeCache) {
            for (val theme : this.themeCache.values()) {
                initParent(theme);
            }
        }
    }
    
    /**
     * Set the prefix that gets applied to the {@link java.util.ResourceBundle} basenames,
     * i.e. the theme names.
     * For example: basenamePrefix="test.", themeName="theme" &rarr; basename="test.theme".
     * <p>Note that ResourceBundle names are effectively classpath locations: As a
     * consequence, the JDK's standard ResourceBundle treats dots as package separators.
     * This means that "test.theme" is effectively equivalent to "test/theme",
     * just like it is for programmatic {@code java.util.ResourceBundle} usage.
     * @see java.util.ResourceBundle#getBundle(String)
     */
    public void setBasenamePrefix(@Nullable final String basenamePrefix) {
        this.basenamePrefix = basenamePrefix != null ? basenamePrefix : StringUtils.EMPTY;
    }
    


    /**
     * This implementation returns a SimpleTheme instance, holding a
     * ResourceBundle-based MessageSource whose basename corresponds to
     * the given theme name (prefixed by the configured "basenamePrefix").
     * <p>SimpleTheme instances are cached per theme name. Use a reloadable
     * MessageSource if themes should reflect changes to the underlying files.
     * @see #setBasenamePrefix
     * @see #createMessageSource
     */
    @Override
    @Nullable
    public Theme getTheme(final String themeName) {
        var theme = this.themeCache.get(themeName);
        if (theme == null) {
            synchronized (this.themeCache) {
                theme = this.themeCache.get(themeName);
                if (theme == null) {
                    val basename = this.basenamePrefix + themeName;
                    val messageSource = createMessageSource(basename);
                    theme = new SimpleTheme(themeName, messageSource);
                    initParent(theme);
                    this.themeCache.put(themeName, theme);
                    LOGGER.debug("Theme created: name [{}], basename [{}]", themeName, basename);
                }
            }
        }
        return theme;
    }

    /**
     * Create a MessageSource for the given basename,
     * to be used as MessageSource for the corresponding theme.
     * <p>Default implementation creates a ResourceBundleMessageSource.
     * for the given basename. A subclass could create a specifically
     * configured ReloadableResourceBundleMessageSource, for example.
     * @param basename the basename to create a MessageSource for
     * @return the MessageSource
     * @see org.springframework.context.support.ResourceBundleMessageSource
     * @see org.springframework.context.support.ReloadableResourceBundleMessageSource
     */
    protected MessageSource createMessageSource(final String basename) {
        val messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename(basename);
        if (this.defaultEncoding != null) {
            messageSource.setDefaultEncoding(this.defaultEncoding);
        }
        if (this.fallbackToSystemLocale != null) {
            messageSource.setFallbackToSystemLocale(this.fallbackToSystemLocale);
        }
        if (this.beanClassLoader != null) {
            messageSource.setBeanClassLoader(this.beanClassLoader);
        }
        return messageSource;
    }

    /**
     * Initialize the {@link MessageSource} of the given theme with the
     * one from the corresponding parent of this ThemeSource.
     * @param theme the Theme to (re-)initialize
     */
    protected void initParent(final Theme theme) {
        if (theme.getMessageSource() instanceof final HierarchicalMessageSource messageSource) {
            if (getParentThemeSource() != null && messageSource.getParentMessageSource() == null) {
                val parentTheme = getParentThemeSource().getTheme(theme.getName());
                if (parentTheme != null) {
                    messageSource.setParentMessageSource(parentTheme.getMessageSource());
                }
            }
        }
    }

}
