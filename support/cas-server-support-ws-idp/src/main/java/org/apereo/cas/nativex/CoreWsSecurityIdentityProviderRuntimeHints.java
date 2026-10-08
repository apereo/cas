package org.apereo.cas.nativex;

import module java.base;
import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import org.apereo.cas.ws.idp.services.WSFederationRegisteredService;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jooq.lambda.Unchecked;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

/**
 * This is {@link CoreWsSecurityIdentityProviderRuntimeHints}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public class CoreWsSecurityIdentityProviderRuntimeHints implements CasRuntimeHintsRegistrar {
    @Override
    public void registerHints(final @NonNull RuntimeHints hints, final @Nullable ClassLoader classLoader) {
        hints.resources().registerResourceBundle("messages.wss4j_errors");
        hints.resources().registerResourceBundle("org.apache.xml.security.resource.xmlsecurity");
        val resolver = new PathMatchingResourcePatternResolver(classLoader);
        val bundles = Unchecked.supplier(() -> resolver.getResources("classpath*:org/apache/cxf/**/Messages.properties")).get();
        for (val bundle : bundles) {
            val url = Unchecked.supplier(() -> bundle.getURL().toExternalForm()).get();
            val path = url.substring(url.indexOf("org/apache/cxf/"));
            hints.resources().registerResourceBundle(path.replace(".properties", StringUtils.EMPTY).replace('/', '.'));
        }
        registerSerializationHints(hints, WSFederationRegisteredService.class);
        registerReflectionHints(hints, List.of(
            WSFederationRegisteredService.class
        ));
    }
}
