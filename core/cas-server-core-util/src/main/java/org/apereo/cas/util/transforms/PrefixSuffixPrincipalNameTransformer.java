package org.apereo.cas.util.transforms;

import org.apereo.cas.authentication.handler.PrincipalNameTransformer;

/**
 * Transform the user id by adding a prefix or suffix.
 *
 * @author Howard Gilbert
 * @author Scott Battaglia
 * @since 3.3.6
 */
public class PrefixSuffixPrincipalNameTransformer implements PrincipalNameTransformer {

    private String prefix;
    private String suffix;

    /**
     * Instantiates a new Prefix suffix principal name transformer.
     */
    public PrefixSuffixPrincipalNameTransformer() {
        this.prefix = null;
        this.suffix = null;
    }

    public PrefixSuffixPrincipalNameTransformer(final String prefix, final String suffix) {
        this.prefix = prefix;
        this.suffix = suffix;
    }

    @Override
    public String transform(final String formUserId) {
        final StringBuilder stringBuilder = new StringBuilder();

        if (this.prefix != null) {
            stringBuilder.append(this.prefix);
        }

        stringBuilder.append(formUserId);

        if (this.suffix != null) {
            stringBuilder.append(this.suffix);
        }

        return stringBuilder.toString();
    }

    public void setPrefix(final String prefix) {
        this.prefix = prefix;
    }

    public void setSuffix(final String suffix) {
        this.suffix = suffix;
    }
}
