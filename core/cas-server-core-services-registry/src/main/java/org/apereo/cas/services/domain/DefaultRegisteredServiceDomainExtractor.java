package org.apereo.cas.services.domain;

import org.apereo.cas.util.RegexUtils;

import lombok.val;
import org.apache.commons.lang3.StringUtils;

import java.util.regex.Pattern;

/**
 * This is {@link DefaultRegisteredServiceDomainExtractor}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
public class DefaultRegisteredServiceDomainExtractor implements RegisteredServiceDomainExtractor {
    /**
     * This regular expression is used to strip the domain form the serviceId that is set in
     * the Service and also passed as the service parameter to the login endpoint.
     */
    private final Pattern domainExtractor = RegexUtils.createPattern("^\\^?https?\\??://(.*?)(?:[(]?[:/]|$)");
    private final Pattern domainPattern = RegexUtils.createPattern("^[a-z0-9-.]*$");

    @Override
    public String extract(final String service) {
        val extractor = this.domainExtractor.matcher(service.toLowerCase());
        return extractor.lookingAt() ? validate(extractor.group(1)) : DOMAIN_DEFAULT;
    }

    private String validate(final String providedDomain) {
        val domain = StringUtils.remove(providedDomain, "\\");
        val match = domainPattern.matcher(StringUtils.remove(domain, "\\"));
        return match.matches() ? domain : DOMAIN_DEFAULT;
    }
}
