package org.apereo.cas.support.saml.services;

import org.apereo.cas.configuration.support.ExpressionLanguageCapable;
import org.apereo.cas.services.RegisteredServiceAttributeReleasePolicyContext;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceMetadataAdaptor;
import org.apereo.cas.support.saml.services.idp.metadata.cache.SamlRegisteredServiceCachingMetadataResolver;
import org.apereo.cas.util.spring.ApplicationContextProvider;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jooq.lambda.Unchecked;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;

import java.io.Serial;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * This is {@link GroovySamlRegisteredServiceAttributeReleasePolicy}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GroovySamlRegisteredServiceAttributeReleasePolicy extends BaseSamlRegisteredServiceAttributeReleasePolicy {

    @Serial
    private static final long serialVersionUID = 3020434998499030162L;

    @ExpressionLanguageCapable
    private String groovyScript;

    @Override
    protected Map<String, List<Object>> getAttributesForSamlRegisteredService(
        final Map<String, List<Object>> attributes,
        final SamlRegisteredServiceCachingMetadataResolver resolver,
        final SamlRegisteredServiceMetadataAdaptor facade,
        final EntityDescriptor entityDescriptor,
        final RegisteredServiceAttributeReleasePolicyContext context) {

        return ApplicationContextProvider.getScriptResourceCacheManager()
            .map(cacheMgr -> {
                val groovyResource = SpringExpressionLanguageValueResolver.getInstance().resolve(this.groovyScript);
                val script = cacheMgr.resolveScriptableResource(groovyResource, groovyResource);
                return Optional.ofNullable(script)
                    .map(Unchecked.function(sc -> {
                        val args = new Object[]{attributes, context.getRegisteredService(), resolver,
                            facade, entityDescriptor, context.getApplicationContext(), LOGGER};
                        return (Map<String, List<Object>>) script.execute(args, Map.class, true);
                    }))
                    .orElseGet(() -> {
                        LOGGER.warn("Groovy script [{}] does not exist or cannot be loaded", groovyScript);
                        return new HashMap<>();
                    });
            })
            .orElseThrow(() -> new RuntimeException("No groovy script cache manager is available to execute attribute mappings"));
    }
}
