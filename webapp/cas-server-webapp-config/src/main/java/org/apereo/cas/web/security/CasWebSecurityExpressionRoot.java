package org.apereo.cas.web.security;

import org.apereo.cas.util.RegexUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.access.expression.WebSecurityExpressionRoot;

import java.util.regex.Pattern;

/**
 * This is {@link CasWebSecurityExpressionRoot}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Slf4j
public class CasWebSecurityExpressionRoot extends WebSecurityExpressionRoot {

    public CasWebSecurityExpressionRoot(final Authentication a, final FilterInvocation fi) {
        super(a, fi);
    }

    @Override
    public boolean hasIpAddress(final String ipAddress) {
        try {
            return super.hasIpAddress(ipAddress);
        } catch (final Exception ex) {
            LOGGER.trace(ex.getMessage(), ex);
            val remoteAddr = this.request.getRemoteAddr();
            LOGGER.trace("Attempting to match [{}] against [{}] as a regular expression", remoteAddr, ipAddress);
            val matcher = RegexUtils.createPattern(ipAddress, Pattern.CASE_INSENSITIVE).matcher(remoteAddr);
            val result = matcher.matches();
            if (!result) {
                LOGGER.warn("Provided regular expression pattern [{}] does not match [{}]", ipAddress, remoteAddr);
            }
            return result;
        }
    }
}
