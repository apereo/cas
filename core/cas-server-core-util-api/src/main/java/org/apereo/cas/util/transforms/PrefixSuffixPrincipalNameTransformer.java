package org.apereo.cas.util.transforms;

import org.apereo.cas.authentication.handler.PrincipalNameTransformer;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.val;

/**
 * Transform the user id by adding a prefix or suffix.
 *
 * @author Howard Gilbert
 * @author Scott Battaglia
 * @since 3.3.6
 */
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PrefixSuffixPrincipalNameTransformer implements PrincipalNameTransformer {

    private String prefix;
    private String suffix;

    @Override
    public String transform(final String formUserId) {
        val stringBuilder = new StringBuilder();
        if (this.prefix != null) {
            stringBuilder.append(this.prefix);
        }
        stringBuilder.append(formUserId);
        if (this.suffix != null) {
            stringBuilder.append(this.suffix);
        }
        return stringBuilder.toString();
    }
}
