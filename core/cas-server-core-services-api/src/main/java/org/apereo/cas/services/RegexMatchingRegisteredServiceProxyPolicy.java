package org.apereo.cas.services;

import module java.base;
import org.apereo.cas.util.RegexUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

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
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@EqualsAndHashCode
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class RegexMatchingRegisteredServiceProxyPolicy implements RegisteredServiceProxyPolicy {

    @Serial
    private static final long serialVersionUID = -211069319543047324L;

    private String pattern;

    private boolean useServiceId;

    private boolean exactMatch;

    @JsonIgnore
    @Override
    public boolean isAllowedToProxy() {
        return true;
    }

    @Override
    public boolean isAllowedProxyCallbackUrl(final RegisteredService registeredService, final URL pgtUrl) {
        var patternToUse = this.useServiceId ? registeredService.getServiceId() : this.pattern;
        if (!RegexUtils.isValidRegex(patternToUse)) {
            LOGGER.warn("Pattern specified [{}] is not a valid regular expression", patternToUse);
            return false;
        }
        if (exactMatch) {
            LOGGER.debug("Pattern [{}] is compared against URL [{}] for exact equality", patternToUse, pgtUrl.toExternalForm());
            return patternToUse.equals(pgtUrl.toExternalForm());
        }
        LOGGER.debug("Using pattern [{}] to authorize proxy policy for URL [{}]", patternToUse, pgtUrl.toExternalForm());
        return RegexUtils.find(patternToUse, pgtUrl.toExternalForm());
    }
}
