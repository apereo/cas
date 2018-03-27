package org.apereo.cas.util.transforms;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.handler.PrincipalNameTransformer;
import lombok.Setter;
import lombok.NoArgsConstructor;

/**
 * A transformer that converts the form uid to either lowercase or
 * uppercase. The result is also trimmed. The transformer is also able
 * to accept and work on the result of a previous transformer that might
 * have modified the uid, such that the two can be chained.
 *
 * @author Misagh Moayyed
 * @since 4.1.0
 */
@Slf4j
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ConvertCasePrincipalNameTransformer implements PrincipalNameTransformer {

    private boolean toUpperCase;

    @Override
    public String transform(final String formUserId) {
        final String result = formUserId.trim();
        return this.toUpperCase ? result.toUpperCase() : result.toLowerCase();
    }
}
