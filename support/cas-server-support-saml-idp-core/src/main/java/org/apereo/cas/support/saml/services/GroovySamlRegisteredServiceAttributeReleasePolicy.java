package org.apereo.cas.support.saml.services;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;
import org.apereo.cas.support.saml.services.idp.metadata.cache.SamlRegisteredServiceCachingMetadataResolver;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.scripting.ScriptingUtils;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.springframework.context.ApplicationContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private static final long serialVersionUID = 3020434998499030162L;

    private String groovyScript;

    @Override
    protected Map<String, List<Object>> getAttributesForSamlRegisteredService(final Map<String, List<Object>> attributes,
                                                                              final SamlRegisteredService registeredService,
                                                                              final ApplicationContext applicationContext,
                                                                              final SamlRegisteredServiceCachingMetadataResolver resolver,
                                                                              final SamlRegisteredServiceServiceProviderMetadataFacade facade,
                                                                              final EntityDescriptor entityDescriptor,
                                                                              final Principal principal,
                                                                              final Service selectedService) {
        try {
            val args = new Object[] {attributes, registeredService, resolver, facade, entityDescriptor, applicationContext, LOGGER};
            val resource = ResourceUtils.getResourceFrom(SpringExpressionLanguageValueResolver.getInstance().resolve(this.groovyScript));
            return ScriptingUtils.executeGroovyScript(resource, args, Map.class, true);
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        LOGGER.warn("Groovy script [{}] does not exist or cannot be loaded", groovyScript);
        return new HashMap<>(0);
    }
}
