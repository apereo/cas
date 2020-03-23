package org.apereo.cas.web.support.filters;

import org.apereo.cas.util.RegexUtils;

import com.google.common.base.Splitter;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * This is a Java Servlet Filter that examines specified Request Parameters as to whether they contain specified
 * characters and as to whether they are multivalued throws an Exception if they do not meet configured rules.
 * <p>
 * Configuration:
 * <p>
 * The filter defaults to checking all request parameters for the hash, percent, question mark,
 * and ampersand characters, and enforcing no-multi-valued-ness.
 * <p>
 * You can turn off multi-value checking by setting the init-param "allowMultiValuedParameters" to "true".  Setting it
 * to "false" is a no-op retaining the default configuration.  Setting this parameter to any other value may fail filter
 * initialization.
 * <p>
 * You can change the set of request parameters being examined by setting the init-param "parametersToCheck" to a
 * whitespace delimited list of parameters to check.  Setting it to the special value "*" retains the default
 * behavior of checking all.  Setting it to a blank value may fail filter initialization.  Setting it to a String
 * containing the asterisk token and any additional token may fail filter initialization.
 * <p>
 * You can change the set of characters looked for by setting the init-param "charactersToForbid" to a whitespace
 * delimited list of characters to forbid.  Setting it to the special value "none" disables the illicit character
 * blocking feature of this Filter (for the case where you only want to use the mutli-valued-ness blocking).
 * Setting it to a blank value may fail filter initialization.
 * Setting it to a value that fails to parse perfectly
 * (e.g., a value with multi-character Strings between the whitespace delimiters)
 * may fail filter initialization.  The default set of characters disallowed is percent, hash, question mark,
 * and ampersand.
 * <p>
 * You can limit a set of request parameters to only be allowed on requests of type POST by
 * setting the init-param "onlyPostParameters" to a whitespace-delimited list of parameters.
 * Unlike "parametersToCheck", this does not support the special value "*".
 * Setting  "onlyPostParameters" to a blank value fails filter initialization.
 * By default (when "onlyPostParameters" is not set), the filter does not limit request parameters
 * to only POST requests.
 * <p>
 * Setting any other init parameter other than these recognized by this Filter will fail Filter initialization.  This
 * is to protect the adopter from typos or misunderstandings in web.xml configuration such that an intended
 * configuration might not have taken effect, since that might have security implications.
 * <p>
 * Setting the Filter to both allow multi-valued parameters and to disallow no characters would make the Filter a
 * no-op, and so may fail filter initialization since you probably meant the Filter to be doing something.
 * <p>
 * The intent of this filter is rough, brute force blocking of unexpected characters in specific CAS protocol related
 * request parameters.  This is one option as a workaround for patching in place certain Java CAS Client versions that
 * may be vulnerable to certain attacks involving crafted request parameter values that may be mishandled.  This is
 * also suitable for patching certain CAS Server versions to make more of an effort to detect and block out-of-spec
 * CAS protocol requests.  Aside from the intent to be useful for those cases, there is nothing CAS-specific about
 * this Filter itself.  This is a generic Filter for doing some pretty basic generic sanity checking on request
 * parameters.  It might come in handy the next time this kind of issue arises.
 * <p>
 * This Filter is written to have no external .jar dependencies aside from the Servlet API necessary to be a Filter.
 *
 * @author Andrew Petro
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Setter
@Getter
public class RequestParameterPolicyEnforcementFilter extends AbstractSecurityFilter implements Filter {

    /**
     * The set of Characters blocked by default on checked parameters.
     * Expressed as a whitespace delimited set of characters.
     */
    public static final String DEFAULT_CHARACTERS_BLOCKED = "? & # %";

    /**
     * The name of the optional Filter init-param specifying what request parameters ought to be checked.
     * The value is a whitespace delimited set of parameters.
     * The exact value '*' has the special meaning of matching all parameters, and is the default behavior.
     */
    public static final String PARAMETERS_TO_CHECK = "parametersToCheck";

    /**
     * The name of the optional Filter init-param specifying what characters are forbidden in the checked request
     * parameters.  The value is a whitespace delimited set of such characters.
     */
    public static final String CHARACTERS_TO_FORBID = "charactersToForbid";

    /**
     * The name of the optional Filter init-param specifying whether the checked request parameters are allowed
     * to have multiple values.  Allowable values for this init parameter are `true` and `false`.
     */
    public static final String ALLOW_MULTI_VALUED_PARAMETERS = "allowMultiValuedParameters";

    /**
     * Name of the setting to specify a pattern that would be checked against the request
     * URL to block if a successful match is found.
     */
    public static final String PATTERN_TO_BLOCK = "patternToBlock";

    /**
     * The name of the optional Filter init-param specifying what request parameters ought to be send via POST requests only.
     */
    public static final String ONLY_POST_PARAMETERS = "onlyPostParameters";

    /**
     * Pattern to check against the full request URL
     * and block if successful matches are found.
     */
    private Pattern patternToBlock;

    /**
     * Set of parameter names to check.
     * Empty set represents special behavior of checking all parameters.
     */
    private Set<String> parametersToCheck;

    /**
     * Set of characters to forbid in the checked request parameters.
     * Empty set represents not forbidding any characters.
     */
    private Set<Character> charactersToForbid;

    /**
     * Should checked parameters be permitted to have multiple values.
     */
    private boolean allowMultiValueParameters;

    /**
     * Set of parameters which should be only received via POST requests.
     */
    private Set<String> onlyPostParameters;


    /**
     * Examines the Filter init parameter names and throws ServletException if they contain an unrecognized
     * init parameter name.
     * <p>
     * This is a stateless static method.
     * <p>
     * This method is an implementation detail and is not exposed API.
     * This method is only non-private to allow JUnit testing.
     *
     * @param initParamNames init param names, in practice as read from the FilterConfig.
     */
    public static void throwIfUnrecognizedParamName(final Enumeration initParamNames) {
        val recognizedParameterNames = new HashSet<String>();
        recognizedParameterNames.add(ALLOW_MULTI_VALUED_PARAMETERS);
        recognizedParameterNames.add(PARAMETERS_TO_CHECK);
        recognizedParameterNames.add(ONLY_POST_PARAMETERS);
        recognizedParameterNames.add(CHARACTERS_TO_FORBID);
        recognizedParameterNames.add(THROW_ON_ERROR);
        recognizedParameterNames.add(PATTERN_TO_BLOCK);

        while (initParamNames.hasMoreElements()) {
            val initParamName = (String) initParamNames.nextElement();
            if (!recognizedParameterNames.contains(initParamName)) {
                logException(new ServletException("Unrecognized init parameter [" + initParamName + "]."));
            }
        }
    }

    /**
     * Parse the whitespace delimited String of parameters to check.
     * <p>
     * If the String is null, return the empty set.
     * If the whitespace delimited String contains no tokens, throw IllegalArgumentException.
     * If the sole token is an asterisk, return the empty set.
     * If the asterisk token is encountered among other tokens, throw IllegalArgumentException.
     * <p>
     * This method returning an empty Set has the special meaning of "check all parameters".
     * <p>
     * This is a stateless static method.
     * <p>
     * This method is an implementation detail and is not exposed API.
     * This method is only non-private to allow JUnit testing.
     *
     * @param initParamValue null, or a non-blank whitespace delimited list of parameters to check
     * @param allowWildcard  whether a wildcard is allowed instead of the parameters list
     * @return a Set of String names of parameters to check, or an empty set representing check-them-all.
     * @throws IllegalArgumentException when the init param value is out of spec
     */
    public static Set<String> parseParametersList(final String initParamValue, final boolean allowWildcard) {
        if (null == initParamValue) {
            return new HashSet<>(0);
        }
        if (initParamValue.trim().isEmpty()) {
            logException(new IllegalArgumentException('[' + initParamValue + "] had no tokens but should have had at least one token."));
        }
        val tokens = Splitter.onPattern("\\s+").splitToList(initParamValue.trim());
        if (tokens.isEmpty()) {
            logException(new IllegalArgumentException('[' + initParamValue + "] had no tokens but should have had at least one token."));
        }

        if (allowWildcard && 1 == tokens.size() && "*".equals(tokens.get(0))) {
            return new HashSet<>(0);
        }
        val parameterNames = new HashSet<String>();
        for (val parameterName : tokens) {
            if ("*".equals(parameterName)) {
                logException(new IllegalArgumentException("Star token encountered among other tokens in parsing [" + initParamValue + ']'));
            }
            parameterNames.add(parameterName);
        }
        return parameterNames;
    }

    /**
     * Parse a whitespace delimited set of Characters from a String.
     * <p>
     * If the String is "none" parse to empty set meaning block no characters.
     * If the String is empty throw, to avoid configurer accidentally configuring not to block any characters.
     *
     * @param value value of the init param to parse
     * @return non-null Set of zero or more Characters to block
     */
    public static Set<Character> parseCharactersToForbid(final String value) {

        val charactersToForbid = new HashSet<Character>();

        var paramValue = value;
        if (paramValue == null) {
            paramValue = DEFAULT_CHARACTERS_BLOCKED;
        } else if (paramValue.trim().isEmpty()) {
            logException(new IllegalArgumentException("Expected tokens when parsing [" + paramValue + "] but found no tokens."));
        }

        if ("none".equals(paramValue)) {
            return charactersToForbid;
        }

        var tokens = Splitter.onPattern("\\s+").splitToList(paramValue);

        if (tokens.isEmpty()) {
            logException(new IllegalArgumentException("Expected tokens when parsing [" + paramValue + "] but found no tokens."
                + " If you really want to configure no characters, use the magic value 'none'."));
        }

        for (val token : tokens) {
            if (token.length() > 1) {
                logException(new IllegalArgumentException("Expected tokens of length 1 but found [" + token + "] when parsing [" + paramValue + ']'));
            }
            val character = token.charAt(0);
            charactersToForbid.add(character);
        }
        return charactersToForbid;
    }

    /**
     * For each parameter to check, verify that it has zero or one value.
     * <p>
     * The Set of parameters to check MAY be empty.
     * The parameter map MAY NOT contain any given parameter to check.
     * <p>
     * This method is an implementation detail and is not exposed API.
     * This method is only non-private to allow JUnit testing.
     * <p>
     *
     * @param parametersToCheck non-null potentially empty Set of String names of parameters
     * @param parameterMap      non-null Map from String name of parameter to String[] values
     * @throws IllegalStateException if a parameterToCheck is present in the parameterMap with multiple values.
     */
    public static void requireNotMultiValued(final Set<String> parametersToCheck, final Map parameterMap) {

        for (val parameterName : parametersToCheck) {
            if (parameterMap.containsKey(parameterName)) {
                val values = (String[]) parameterMap.get(parameterName);
                if (values.length > 1) {
                    logException(new IllegalStateException("Parameter [" + parameterName + "] had multiple values ["
                        + Arrays.toString(values) + "] but at most one value is allowable."));
                }
            }
        }

    }

    /**
     * For each parameter to check, for each value of that parameter, check that the value does not contain
     * forbidden characters.
     * <p>
     * This method is an implementation detail and is not exposed API.
     * This method is only non-private to allow JUnit testing.
     *
     * @param parametersToCheck  Set of String request parameter names to look for
     * @param charactersToForbid Set of Character characters to forbid
     * @param parameterMap       String to String[] Map, in practice as read from ServletRequest
     */
    public static void enforceParameterContentCharacterRestrictions(
        final Set<String> parametersToCheck, final Set<Character> charactersToForbid, final Map parameterMap) {

        if (charactersToForbid.isEmpty()) {
            return;
        }

        for (val parameterToCheck : parametersToCheck) {
            val parameterValues = (String[]) parameterMap.get(parameterToCheck);
            if (null != parameterValues) {
                for (val parameterValue : parameterValues) {
                    for (val forbiddenCharacter : charactersToForbid) {
                        if (parameterValue.contains(forbiddenCharacter.toString())) {
                            logException(new IllegalArgumentException("Disallowed character [" + forbiddenCharacter
                                + "] found in value [" + parameterValue + "] of parameter named ["
                                + parameterToCheck + ']'));
                        }
                    }
                }
            }
        }
    }

    /**
     * Check that some parameters should only be in POST requests (according to the configuration).
     *
     * @param method             the method of the request
     * @param parameterMap       all the request parameters
     * @param onlyPostParameters parameters that should only be in POST requests
     */
    public static void checkOnlyPostParameters(final String method, final Map parameterMap, final Set<String> onlyPostParameters) {
        if (!"POST".equals(method)) {
            val names = parameterMap.keySet();
            for (val onlyPostParameter : onlyPostParameters) {
                if (names.contains(onlyPostParameter)) {
                    logException(new IllegalArgumentException(onlyPostParameter + " parameter should only be used in POST requests"));
                }
            }
        }
    }

    @Override
    public void init(final FilterConfig filterConfig) {
        val failSafeParam = filterConfig.getInitParameter(THROW_ON_ERROR);

        if (null != failSafeParam) {
            setThrowOnErrors(Boolean.parseBoolean(failSafeParam));
        }

        val initParamNames = filterConfig.getInitParameterNames();
        throwIfUnrecognizedParamName(initParamNames);

        val initParamAllowMultiValuedParameters = filterConfig.getInitParameter(ALLOW_MULTI_VALUED_PARAMETERS);
        val initParamParametersToCheck = filterConfig.getInitParameter(PARAMETERS_TO_CHECK);
        val initParamOnlyPostParameters = filterConfig.getInitParameter(ONLY_POST_PARAMETERS);
        val initParamCharactersToForbid = filterConfig.getInitParameter(CHARACTERS_TO_FORBID);

        val initParamPatternToBlock = filterConfig.getInitParameter(PATTERN_TO_BLOCK);
        this.patternToBlock = StringUtils.isNotBlank(initParamPatternToBlock)
            ? RegexUtils.createPattern(initParamPatternToBlock)
            : null;

        this.allowMultiValueParameters = Boolean.parseBoolean(initParamAllowMultiValuedParameters);
        this.parametersToCheck = parseParametersList(initParamParametersToCheck, true);
        this.onlyPostParameters = parseParametersList(initParamOnlyPostParameters, false);
        this.charactersToForbid = parseCharactersToForbid(initParamCharactersToForbid);

        if (this.allowMultiValueParameters && this.charactersToForbid.isEmpty()) {
            logException(new ServletException("Configuration to allow multi-value parameters and forbid no characters makes "
                + getClass().getSimpleName() + " a no-op"));
        }
    }

    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain)
        throws IOException, ServletException {
        try {
            if (request instanceof HttpServletRequest) {
                val httpServletRequest = (HttpServletRequest) request;
                val parameterMap = httpServletRequest.getParameterMap();

                blockRequestIfNecessary(httpServletRequest);

                val parametersToCheckHere = new HashSet<String>();
                if (this.parametersToCheck.isEmpty()) {
                    parametersToCheckHere.addAll(parameterMap.keySet());
                } else {
                    parametersToCheckHere.addAll(this.parametersToCheck);
                }

                if (!this.allowMultiValueParameters) {
                    requireNotMultiValued(parametersToCheckHere, parameterMap);
                }

                enforceParameterContentCharacterRestrictions(parametersToCheckHere,
                    this.charactersToForbid, parameterMap);

                checkOnlyPostParameters(httpServletRequest.getMethod(), parameterMap, this.onlyPostParameters);
            }
        } catch (final Exception e) {
            logException(new ServletException(getClass().getSimpleName() + " is blocking this request.", e));
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }

    private void blockRequestIfNecessary(final HttpServletRequest httpServletRequest) {
        if (patternToBlock != null && StringUtils.isNotBlank(httpServletRequest.getRequestURI())) {
            val uri = UriComponentsBuilder.fromHttpRequest(new ServletServerHttpRequest(httpServletRequest))
                .build()
                .toUriString();
            if (!this.patternToBlock.equals(RegexUtils.MATCH_NOTHING_PATTERN)
                && this.patternToBlock.matcher(uri).find()) {
                logException(new ServletException("The request is blocked as it matches a prohibited pattern"));
            }
        }
    }
}
