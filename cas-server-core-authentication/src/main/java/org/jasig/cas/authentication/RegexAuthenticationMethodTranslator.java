package org.jasig.cas.authentication;

import org.jasig.cas.authentication.principal.WebApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * A translator that will check a list of regex patterns and return an authentication method name.
 *
 * @author John Gasper
 * @since 4.3
 */
@Component("regexAuthenticationMethodTranslator")
public class RegexAuthenticationMethodTranslator implements AuthenticationMethodTranslator {
    @Autowired(required=false)
    @Qualifier("regexAuthenticationMethodsTranslationMap")
    private Map<Pattern, String> translationMap;

    @Autowired
    @Value("${cas.mfa.default.authn.method:}")
    private String defaultMfaMethod;

    private RegexAuthenticationMethodTranslator() {}

    /**
     * Instantiates a new Regex authentication method translator.
     *
     * @param translationMap the regex/mfa method translation map (maybe an ordered map)
     */
    public RegexAuthenticationMethodTranslator(final Map<String, String> translationMap) {
        this(translationMap, null);
    }

    /**
     * Instantiates a new Regex authentication method translator.
     *
     * @param translationMap the regex/mfa method translation map (maybe an ordered map)
     * @param defaultMfaMethod the default MFA merhod to use if no match is found.
     */
    public RegexAuthenticationMethodTranslator(final Map<String, String> translationMap, final String defaultMfaMethod) {
        this.defaultMfaMethod = defaultMfaMethod;

        final Map<Pattern, String> optimizedMap = new LinkedHashMap<>();

        for (final Map.Entry<String, String> stringStringEntry : translationMap.entrySet()) {
            optimizedMap.put(Pattern.compile(stringStringEntry.getKey()), stringStringEntry.getValue());
        }

        this.translationMap = optimizedMap;
    }

    @Override
    public String translate(final WebApplicationService targetService, final String triggerValue) {
        for (final Map.Entry<Pattern, String> patternStringEntry : translationMap.entrySet()) {
            if (patternStringEntry.getKey().matcher(triggerValue).matches()) {
                return this.translationMap.get(patternStringEntry.getKey());
            }
        }

        if (this.defaultMfaMethod != null) {
            return defaultMfaMethod;
        }

        throw new UnrecognizedAuthenticationMethodException(triggerValue);
    }
}
