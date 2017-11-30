package org.apereo.cas.util.transforms;

import org.apereo.cas.authentication.handler.PrincipalNameTransformer;
import org.apereo.cas.util.RegexUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * A transformer that extracts the principal by a provided regex pattern.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class RegexPrincipalNameTransformer implements PrincipalNameTransformer {
    private static final long serialVersionUID = 1067914936775326709L;

    private Pattern pattern;

    public RegexPrincipalNameTransformer(final String pattern) {
        setPattern(pattern);
    }

    public void setPattern(final String pattern) {
        this.pattern = RegexUtils.createPattern(pattern);
    }

    @Override
    public String transform(final String username) {
        final Matcher matcher = this.pattern.matcher(username);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return username.trim();
    }
}
