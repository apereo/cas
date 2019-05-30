package org.apereo.cas.util.transforms;

import org.apereo.cas.authentication.handler.PrincipalNameTransformer;
import org.apereo.cas.util.RegexUtils;

import lombok.Setter;
import lombok.val;

import java.util.regex.Pattern;

/**
 * A transformer that extracts the principal by a provided regex pattern.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Setter
public class RegexPrincipalNameTransformer implements PrincipalNameTransformer {

    private Pattern pattern;

    public RegexPrincipalNameTransformer(final String pattern) {
        setPattern(RegexUtils.createPattern(pattern));
    }

    @Override
    public String transform(final String username) {
        val matcher = this.pattern.matcher(username);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return username.trim();
    }
}
