package org.apereo.cas.oidc.claims;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.scripting.ScriptResourceCacheManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jose4j.jwt.JwtClaims;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * This is {@link OidcGroovyIdTokenClaimCollector}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@RequiredArgsConstructor
@Slf4j
public class OidcGroovyIdTokenClaimCollector implements OidcIdTokenClaimCollector {
    protected final ScriptResourceCacheManager scriptResourceCacheManager;
    protected final ConfigurableApplicationContext applicationContext;
    protected final CasConfigurationProperties casProperties;

    @Override
    public void conclude(final RegisteredService registeredService, final JwtClaims claims) {
        FunctionUtils.doAndHandle(_ -> {
            val location = casProperties.getAuthn().getOidc().getIdToken().getCollectorScript().getLocation();
            val watchableScript = scriptResourceCacheManager.resolveScriptableResource(location.getURI().toString());
            Objects.requireNonNull(watchableScript, "Unable to locate Groovy script for ID token claim collection");
            watchableScript.execute("conclude", Void.class, claims, registeredService, applicationContext, LOGGER);
        });
    }

    @Override
    public void collect(final RegisteredService registeredService, final JwtClaims claims, final String name, final List<Object> values) {
        FunctionUtils.doAndHandle(_ -> {
            val location = casProperties.getAuthn().getOidc().getIdToken().getCollectorScript().getLocation();
            val watchableScript = scriptResourceCacheManager.resolveScriptableResource(location.getURI().toString());
            Objects.requireNonNull(watchableScript, "Unable to locate Groovy script for ID token claim collection");
            watchableScript.execute("collect", Void.class,
                claims, name, values, registeredService, applicationContext, LOGGER);
        });
    }
}
