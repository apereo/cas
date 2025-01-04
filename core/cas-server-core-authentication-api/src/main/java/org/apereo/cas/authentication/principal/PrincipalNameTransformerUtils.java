package org.apereo.cas.authentication.principal;

import org.apereo.cas.authentication.handler.PrincipalNameTransformer;
import org.apereo.cas.configuration.model.core.authentication.PrincipalTransformationProperties;
import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;
import org.apereo.cas.util.transforms.BlockingPrincipalNameTransformer;
import org.apereo.cas.util.transforms.ChainingPrincipalNameTransformer;
import org.apereo.cas.util.transforms.ConvertCasePrincipalNameTransformer;
import org.apereo.cas.util.transforms.GroovyPrincipalNameTransformer;
import org.apereo.cas.util.transforms.PrefixSuffixPrincipalNameTransformer;
import org.apereo.cas.util.transforms.RegexPrincipalNameTransformer;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import java.util.ServiceLoader;

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

        if (p.getGroovy().getLocation() != null && CasRuntimeHintsRegistrar.notInNativeImage()) {
            val t = new GroovyPrincipalNameTransformer(p.getGroovy().getLocation());
            chain.addTransformer(t);
        }

        if (StringUtils.isNotBlank(p.getPattern())) {
            val t = new RegexPrincipalNameTransformer(SpringExpressionLanguageValueResolver.getInstance().resolve(p.getPattern()));
            chain.addTransformer(t);
        }

        if (StringUtils.isNotBlank(p.getPrefix()) || StringUtils.isNotBlank(p.getSuffix())) {
            val t = new PrefixSuffixPrincipalNameTransformer();
            t.setPrefix(SpringExpressionLanguageValueResolver.getInstance().resolve(p.getPrefix()));
            t.setSuffix(SpringExpressionLanguageValueResolver.getInstance().resolve(p.getSuffix()));
            chain.addTransformer(t);
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

        if (StringUtils.isNotBlank(p.getBlockingPattern())) {
            val t = new BlockingPrincipalNameTransformer(SpringExpressionLanguageValueResolver.getInstance().resolve(p.getBlockingPattern()));
            chain.addTransformer(t);
        }

        val transformers = ServiceLoader.load(PrincipalNameTransformer.class)
            .stream()
            .map(ServiceLoader.Provider::get)
            .toList();
        chain.addTransformers(transformers);
        return chain;
    }
}
