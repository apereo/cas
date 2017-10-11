package org.apereo.cas.authentication.principal;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.handler.PrincipalNameTransformer;
import org.apereo.cas.configuration.model.core.authentication.PrincipalTransformationProperties;
import org.apereo.cas.util.transforms.ConvertCasePrincipalNameTransformer;
import org.apereo.cas.util.transforms.PrefixSuffixPrincipalNameTransformer;

/**
 * This is {@link PrincipalNameTransformerUtils}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public final class PrincipalNameTransformerUtils {

    private PrincipalNameTransformerUtils() {
    }

    /**
     * New principal name transformer.
     *
     * @param p the p
     * @return the principal name transformer
     */
    public static PrincipalNameTransformer newPrincipalNameTransformer(final PrincipalTransformationProperties p) {

        final PrincipalNameTransformer res;
        if (StringUtils.isNotBlank(p.getPrefix()) || StringUtils.isNotBlank(p.getSuffix())) {
            final PrefixSuffixPrincipalNameTransformer t = new PrefixSuffixPrincipalNameTransformer();
            t.setPrefix(p.getPrefix());
            t.setSuffix(p.getSuffix());
            res = t;
        } else {
            res = formUserId -> formUserId;
        }

        switch (p.getCaseConversion()) {
            case UPPERCASE:
                final ConvertCasePrincipalNameTransformer t = new ConvertCasePrincipalNameTransformer(res);
                t.setToUpperCase(true);
                return t;

            case LOWERCASE:
                final ConvertCasePrincipalNameTransformer t1 = new ConvertCasePrincipalNameTransformer(res);
                t1.setToUpperCase(false);
                return t1;
            default:
                //nothing
        }
        return res;
    }
}
