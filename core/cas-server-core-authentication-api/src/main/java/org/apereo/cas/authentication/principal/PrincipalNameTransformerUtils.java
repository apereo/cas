package org.apereo.cas.authentication.principal;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.handler.PrincipalNameTransformer;
import org.apereo.cas.configuration.model.core.authentication.PrincipalTransformationProperties;
import org.apereo.cas.util.transforms.ChainingPrincipalNameTransformer;
import org.apereo.cas.util.transforms.ConvertCasePrincipalNameTransformer;
import org.apereo.cas.util.transforms.GroovyPrincipalNameTransformer;
import org.apereo.cas.util.transforms.NoOpPrincipalNameTransformer;
import org.apereo.cas.util.transforms.PrefixSuffixPrincipalNameTransformer;
import org.apereo.cas.util.transforms.RegexPrincipalNameTransformer;

/**
 * This is {@link PrincipalNameTransformerUtils}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
@UtilityClass
public class PrincipalNameTransformerUtils {

    /**
     * New principal name transformer.
     *
     * @param p the p
     * @return the principal name transformer
     */
    public static PrincipalNameTransformer newPrincipalNameTransformer(final PrincipalTransformationProperties p) {
        final var chain = new ChainingPrincipalNameTransformer();

        if (p.getGroovy().getLocation() != null) {
            final var t = new GroovyPrincipalNameTransformer(p.getGroovy().getLocation());
            chain.addTransformer(t);
        }
        
        if (StringUtils.isNotBlank(p.getPattern())) {
            final var t = new RegexPrincipalNameTransformer(p.getPattern());
            chain.addTransformer(t);
        }

        if (StringUtils.isNotBlank(p.getPrefix()) || StringUtils.isNotBlank(p.getSuffix())) {
            final var t = new PrefixSuffixPrincipalNameTransformer();
            t.setPrefix(p.getPrefix());
            t.setSuffix(p.getSuffix());
            chain.addTransformer(t);
        } else {
            chain.addTransformer(new NoOpPrincipalNameTransformer());
        }

        if (p.getCaseConversion() == PrincipalTransformationProperties.CaseConversion.UPPERCASE) {
            final var t = new ConvertCasePrincipalNameTransformer();
            t.setToUpperCase(true);
            chain.addTransformer(t);
        }

        if (p.getCaseConversion() == PrincipalTransformationProperties.CaseConversion.LOWERCASE) {
            final var t = new ConvertCasePrincipalNameTransformer();
            t.setToUpperCase(false);
            chain.addTransformer(t);
        }

        return chain;
    }
}
