package org.apereo.cas.services.web;

import module java.base;
import org.apereo.cas.configuration.model.support.themes.ThemeProperties;
import org.apereo.cas.util.http.HttpRequestUtils;
import org.apereo.cas.web.flow.CasWebflowExecutionPlan;
import org.apereo.cas.web.support.WebUtils;
import org.apereo.cas.web.theme.ThemeResolver;
import org.apereo.cas.web.theme.ThemeSource;
import com.google.common.primitives.Ints;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.MessageSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.webflow.execution.RequestContextHolder;
import org.thymeleaf.context.WebEngineContext;
import org.thymeleaf.util.EvaluationUtils;
import jakarta.servlet.http.HttpServletRequest;

/**
 * This is {@link CasThymeleafTemplatesDirector}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@RequiredArgsConstructor
@Slf4j
public class CasThymeleafTemplatesDirector {
    /**
     * Theme property that turns the theme builder on or off.
     */
    public static final String THEME_PROPERTY_THEME_BUILDER_ENABLED = "cas.theme-builder.enabled";

    private static final Pattern THEME_NAME_PATTERN = Pattern.compile("[\\w.\\-]+");

    private static final Pattern THEME_PROPERTY_PATTERN = Pattern.compile("^(#\\s*)?(cas\\.[\\w.\\-]+)\\s*=(.*)$");

    private final ObjectProvider<CasWebflowExecutionPlan> webflowExecutionPlan;
    private final ObjectProvider<ThemeResolver> themeResolver;
    private final ObjectProvider<ThemeSource> themeSource;

    /**
     * Gets the URL external form supplemented by a question mark or an ampersand.
     *
     * @param url the url
     * @return the url external form
     */
    public String getUrlExternalForm(final URL url) {
        val externalForm = url.toExternalForm();
        if (externalForm.contains("?")) {
            return externalForm + '&';
        }
        return externalForm + '?';
    }

    /**
     * Gets exception class simple name.
     *
     * @param ex the ex
     * @return the exception class simple name
     */
    public String getExceptionClassSimpleName(final Throwable ex) {
        return ex.getClass().getSimpleName();
    }

    /**
     * Is login form viewable?.
     *
     * @param vars the vars
     * @return true/false
     */
    public boolean isLoginFormViewable(final WebEngineContext vars) {
        val requestContext = RequestContextHolder.getRequestContext();
        val providers = webflowExecutionPlan.getObject().getWebflowLoginContextProviders();
        return requestContext != null
            && (WebUtils.isCasLoginFormViewable(requestContext) || providers.isEmpty()
            || providers.stream().noneMatch(provider -> provider.isLoginFormViewable(requestContext)));
    }

    /**
     * Is login form username editable?.
     *
     * @param vars the vars
     * @return true/false
     */
    public boolean isLoginFormUsernameInputVisible(final WebEngineContext vars) {
        val requestContext = RequestContextHolder.getRequestContext();
        val providers = webflowExecutionPlan.getObject().getWebflowLoginContextProviders();
        return requestContext != null
            && (WebUtils.isCasLoginFormViewable(requestContext) || providers.isEmpty()
            || providers.stream().anyMatch(provider -> provider.isLoginFormUsernameInputVisible(requestContext)));
    }

    /**
     * Is login form username input disabled.
     *
     * @param vars the vars
     * @return true/false
     */
    public boolean isLoginFormUsernameInputDisabled(final WebEngineContext vars) {
        val requestContext = RequestContextHolder.getRequestContext();
        return requestContext == null || !WebUtils.isCasLoginFormViewable(requestContext)
            || WebUtils.isGraphicalUserAuthenticationEnabled(requestContext)
            || webflowExecutionPlan.getObject().getWebflowLoginContextProviders()
            .stream()
            .anyMatch(provider -> provider.isLoginFormUsernameInputDisabled(requestContext));
    }

    /**
     * Gets login form username.
     *
     * @param vars the vars
     * @return the login form username
     */
    public String getLoginFormUsername(final WebEngineContext vars) {
        val context = RequestContextHolder.getRequestContext();
        if (context != null && WebUtils.isCasLoginFormViewable(context)) {
            return webflowExecutionPlan.getObject().getWebflowLoginContextProviders()
                .stream()
                .map(provider -> provider.getCandidateUsername(context))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst()
                .orElse(StringUtils.EMPTY);
        }
        return StringUtils.EMPTY;
    }

    /**
     * Format.
     *
     * @param dateTime the date time
     * @param pattern  the pattern
     * @return the string
     */
    public String format(final LocalDateTime dateTime, final String pattern) {
        return dateTime.format(DateTimeFormatter.ofPattern(pattern, Locale.ENGLISH));
    }

    /**
     * Utility to consider blank string values as false.
     *
     * @param target the target
     * @return true/false
     */
    public Boolean isTrue(@Nullable final Object target) {
        return (target instanceof final String value && StringUtils.isBlank(value.trim()))
            ? Boolean.FALSE
            : EvaluationUtils.evaluateAsBoolean(target);
    }

    /**
     * Grab message from theme source using the given code.
     *
     * @param code the code
     * @return the value attributed to the code
     */
    public @Nullable String theme(final String code) {
        val request = HttpRequestUtils.getHttpServletRequestFromRequestAttributes();
        Objects.requireNonNull(request, "Http request cannot be null or undefined");
        return themeMessageSource(request).getMessage(code, null, StringUtils.EMPTY, request.getLocale());
    }

    /**
     * Read a theme property as a whole number.
     *
     * @param code         the code
     * @param defaultValue the value used when the property is blank or not a whole number
     * @return the number
     */
    public int themeInteger(final String code, final int defaultValue) {
        val value = Ints.tryParse(StringUtils.trimToEmpty(theme(code)));
        return value != null ? value : defaultValue;
    }

    /**
     * Whether the theme builder is turned on for the current theme.
     *
     * @return true/false
     */
    public boolean isThemeBuilderEnabled() {
        return isTrue(theme(THEME_PROPERTY_THEME_BUILDER_ENABLED));
    }

    /**
     * Describe the current theme for the theme builder. Properties are those declared, set or commented out,
     * in the default theme file; each one carries its default value, an example value taken from a commented
     * declaration, its description and the value the current theme resolves.
     *
     * @return the theme name and its properties
     */
    public Map<String, Object> getThemeBuilder() {
        val request = HttpRequestUtils.getHttpServletRequestFromRequestAttributes();
        Objects.requireNonNull(request, "Http request cannot be null or undefined");
        val messageSource = themeMessageSource(request);
        val properties = ThemeBuilderCatalog.PROPERTIES
            .stream()
            .map(entry -> entry.toMap(messageSource.getMessage(entry.name(), null, StringUtils.EMPTY, request.getLocale())))
            .toList();
        val themeName = StringUtils.defaultString(themeResolver.getObject().resolveThemeName(request));
        return Map.of(
            "theme", THEME_NAME_PATTERN.matcher(themeName).matches() ? themeName : ThemeProperties.DEFAULT_THEME_NAME,
            "properties", properties);
    }

    private MessageSource themeMessageSource(final HttpServletRequest request) {
        val themeName = themeResolver.getObject().resolveThemeName(request);
        val theme = themeSource.getObject().getTheme(Objects.requireNonNull(themeName));
        Objects.requireNonNull(theme, "Theme cannot be null or undefined");
        return theme.getMessageSource();
    }

    private static List<ThemeBuilderProperty> loadThemeBuilderCatalog() {
        val resource = new ClassPathResource(ThemeProperties.DEFAULT_THEME_NAME + ".properties");
        val catalog = new ArrayList<ThemeBuilderProperty>();
        try (val reader = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            val description = new StringBuilder();
            reader.lines().map(String::strip).forEach(line -> {
                val matcher = THEME_PROPERTY_PATTERN.matcher(line);
                if (matcher.matches()) {
                    val commented = matcher.group(1) != null;
                    val value = StringUtils.defaultString(matcher.group(3)).strip();
                    catalog.add(new ThemeBuilderProperty(StringUtils.defaultString(matcher.group(2)),
                        commented ? StringUtils.EMPTY : value, commented ? value : StringUtils.EMPTY,
                        description.toString().strip()));
                    description.setLength(0);
                } else if (!line.isEmpty() && line.charAt(0) == '#') {
                    description.append(' ').append(Strings.CS.removeStart(line, "#").strip());
                }
            });
        } catch (final IOException e) {
            LOGGER.warn("Unable to read theme properties from [{}]: [{}]", resource.getPath(), e.getMessage());
        }
        return List.copyOf(catalog);
    }

    /**
     * Sort object [].
     *
     * @param array the array
     * @return the object []
     */
    public Object[] sort(final Object[] array) {
        return Arrays.stream(array).sorted().toArray();
    }

    private static final class ThemeBuilderCatalog {
        private static final List<ThemeBuilderProperty> PROPERTIES = loadThemeBuilderCatalog();
    }

    private record ThemeBuilderProperty(String name, String defaultValue, String example, String description) {
        Map<String, String> toMap(final @Nullable String value) {
            return Map.of("name", name, "defaultValue", defaultValue, "example", example,
                "description", description, "value", StringUtils.defaultString(value));
        }
    }
}
