package org.apereo.cas.services;

import org.apereo.cas.util.RegexUtils;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.net.URL;

/**
 * A proxy policy that only allows proxying to pgt urls
 * that match the specified regex pattern.
 *
 * @author Misagh Moayyed
 * @since 4.1.0
 */
@Slf4j
@ToString
@Getter
@NoArgsConstructor
@EqualsAndHashCode
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RegexMatchingRegisteredServiceProxyPolicy implements RegisteredServiceProxyPolicy {

    private static final long serialVersionUID = -211069319543047324L;

    private String pattern;

    /**
     * Init the policy with the pgt url regex pattern that
     * will determine the urls allowed to receive the pgt.
     * The matching by default is done in a case insensitive manner.
     *
     * @param pgtUrlPattern the pgt url pattern
     */
    @JsonCreator
    public RegexMatchingRegisteredServiceProxyPolicy(@JsonProperty("pattern") final String pgtUrlPattern) {
        if (RegexUtils.isValidRegex(pgtUrlPattern)) {
            this.pattern = pgtUrlPattern;
        } else {
            LOGGER.warn("Pattern specified [{}] is not a valid regular expression", pgtUrlPattern);
            this.pattern = RegexUtils.MATCH_NOTHING_PATTERN.pattern();
        }
    }

    @JsonIgnore
    @Override
    public boolean isAllowedToProxy() {
        return true;
    }

    @Override
    public boolean isAllowedProxyCallbackUrl(final URL pgtUrl) {
        return RegexUtils.find(this.pattern, pgtUrl.toExternalForm());
    }
}
