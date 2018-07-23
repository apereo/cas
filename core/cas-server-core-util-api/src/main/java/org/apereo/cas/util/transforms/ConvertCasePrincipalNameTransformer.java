package org.apereo.cas.util.transforms;

import org.apereo.cas.authentication.handler.PrincipalNameTransformer;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.val;

/**
 * A transformer that converts the form uid to either lowercase or
 * uppercase. The result is also trimmed. The transformer is also able
 * to accept and work on the result of a previous transformer that might
 * have modified the uid, such that the two can be chained.
 *
 * @author Misagh Moayyed
 * @since 4.1.0
 */
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ConvertCasePrincipalNameTransformer implements PrincipalNameTransformer {

    private boolean toUpperCase;

    @Override
    public String transform(final String formUserId) {
        val result = formUserId.trim();
        return this.toUpperCase ? result.toUpperCase() : result.toLowerCase();
    }
}
