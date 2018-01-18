package org.apereo.cas.util.transforms;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.handler.PrincipalNameTransformer;
import org.apereo.cas.util.RegexUtils;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.Setter;

/**
 * A transformer that extracts the principal by a provided regex pattern.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
@Setter
public class RegexPrincipalNameTransformer implements PrincipalNameTransformer {

    private static final long serialVersionUID = 1067914936775326709L;

    private Pattern pattern;

    public RegexPrincipalNameTransformer(final String pattern) {
        setPattern(RegexUtils.createPattern(pattern));
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
