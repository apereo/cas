package org.apereo.cas.authentication.principal;

import org.apereo.cas.authentication.handler.PrincipalNameTransformer;
import org.apereo.cas.configuration.model.core.authentication.PrincipalTransformationProperties;
import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import org.apereo.cas.util.scripting.ExecutableCompiledScriptFactory;
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
     * @param properties the properties
     * @return the principal name transformer
     */
    public static PrincipalNameTransformer newPrincipalNameTransformer(final PrincipalTransformationProperties properties) {
        val chain = new ChainingPrincipalNameTransformer();

        if (properties.getGroovy().getLocation() != null
            && CasRuntimeHintsRegistrar.notInNativeImage()
            && ExecutableCompiledScriptFactory.findExecutableCompiledScriptFactory().isPresent()) {
            val transformer = new GroovyPrincipalNameTransformer(properties.getGroovy().getLocation());
            chain.addTransformer(transformer);
        }

        if (StringUtils.isNotBlank(properties.getPattern())) {
            val transformer = new RegexPrincipalNameTransformer(SpringExpressionLanguageValueResolver.getInstance().resolve(properties.getPattern()));
            chain.addTransformer(transformer);
        }

        if (StringUtils.isNotBlank(properties.getPrefix()) || StringUtils.isNotBlank(properties.getSuffix())) {
            val transformer = new PrefixSuffixPrincipalNameTransformer();
            transformer.setPrefix(SpringExpressionLanguageValueResolver.getInstance().resolve(properties.getPrefix()));
            transformer.setSuffix(SpringExpressionLanguageValueResolver.getInstance().resolve(properties.getSuffix()));
            chain.addTransformer(transformer);
        }

        if (properties.getCaseConversion() == PrincipalTransformationProperties.CaseConversion.UPPERCASE) {
            val transformer = new ConvertCasePrincipalNameTransformer();
            transformer.setToUpperCase(true);
            chain.addTransformer(transformer);
        }

        if (properties.getCaseConversion() == PrincipalTransformationProperties.CaseConversion.LOWERCASE) {
            val transformer = new ConvertCasePrincipalNameTransformer();
            transformer.setToUpperCase(false);
            chain.addTransformer(transformer);
        }

        if (StringUtils.isNotBlank(properties.getBlockingPattern())) {
            val transformer = new BlockingPrincipalNameTransformer(SpringExpressionLanguageValueResolver.getInstance().resolve(properties.getBlockingPattern()));
            chain.addTransformer(transformer);
        }

        val transformers = ServiceLoader.load(PrincipalNameTransformer.class)
            .stream()
            .map(ServiceLoader.Provider::get)
            .toList();
        chain.addTransformers(transformers);
        return chain;
    }
}
