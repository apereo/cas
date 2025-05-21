package org.apereo.cas.util.transforms;

import org.apereo.cas.authentication.PreventedException;
import org.apereo.cas.authentication.handler.PrincipalNameTransformer;
import org.apereo.cas.util.RegexUtils;

import lombok.Setter;
import lombok.val;

import java.util.regex.Pattern;

/**
 * A transformer that matches the username against a given regex pattern
 * and throws back an error if a match is found.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Setter
public class BlockingPrincipalNameTransformer implements PrincipalNameTransformer {

    private Pattern pattern;

    public BlockingPrincipalNameTransformer(final String pattern) {
        setPattern(RegexUtils.createPattern(pattern));
    }

    @Override
    public String transform(final String username) {
        val matcher = this.pattern.matcher(username);
        if (matcher.find()) {
            throw new PreventedException("Unable to accept username " + username);
        }
        return username.trim();
    }
}
