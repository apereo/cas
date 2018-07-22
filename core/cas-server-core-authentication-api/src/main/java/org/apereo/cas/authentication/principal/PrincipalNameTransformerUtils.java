package org.apereo.cas.authentication.principal;

import org.apereo.cas.authentication.handler.PrincipalNameTransformer;
import org.apereo.cas.configuration.model.core.authentication.PrincipalTransformationProperties;
import org.apereo.cas.util.transforms.ChainingPrincipalNameTransformer;
import org.apereo.cas.util.transforms.ConvertCasePrincipalNameTransformer;
import org.apereo.cas.util.transforms.GroovyPrincipalNameTransformer;
import org.apereo.cas.util.transforms.NoOpPrincipalNameTransformer;
import org.apereo.cas.util.transforms.PrefixSuffixPrincipalNameTransformer;
import org.apereo.cas.util.transforms.RegexPrincipalNameTransformer;

import lombok.experimental.UtilityClass;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

/**
 * This is {@link PrincipalNameTransformerUtils}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@UtilityClass
public class PrincipalNameTransformerUtils {

    /**
     * New principal name transformer.
     *
     * @param p the p
     * @return the principal name transformer
     */
    public static PrincipalNameTransformer newPrincipalNameTransformer(final PrincipalTransformationProperties p) {
        val chain = new ChainingPrincipalNameTransformer();

        if (p.getGroovy().getLocation() != null) {
            val t = new GroovyPrincipalNameTransformer(p.getGroovy().getLocation());
            chain.addTransformer(t);
        }

        if (StringUtils.isNotBlank(p.getPattern())) {
            val t = new RegexPrincipalNameTransformer(p.getPattern());
            chain.addTransformer(t);
        }

        if (StringUtils.isNotBlank(p.getPrefix()) || StringUtils.isNotBlank(p.getSuffix())) {
            val t = new PrefixSuffixPrincipalNameTransformer();
            t.setPrefix(p.getPrefix());
            t.setSuffix(p.getSuffix());
            chain.addTransformer(t);
        } else {
            chain.addTransformer(new NoOpPrincipalNameTransformer());
        }

        if (p.getCaseConversion() == PrincipalTransformationProperties.CaseConversion.UPPERCASE) {
            val t = new ConvertCasePrincipalNameTransformer();
            t.setToUpperCase(true);
            chain.addTransformer(t);
        }

        if (p.getCaseConversion() == PrincipalTransformationProperties.CaseConversion.LOWERCASE) {
            val t = new ConvertCasePrincipalNameTransformer();
            t.setToUpperCase(false);
            chain.addTransformer(t);
        }

        return chain;
    }
}
