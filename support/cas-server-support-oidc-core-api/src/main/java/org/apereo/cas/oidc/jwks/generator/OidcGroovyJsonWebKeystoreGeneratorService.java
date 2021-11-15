package org.apereo.cas.oidc.jwks.generator;

import org.apereo.cas.oidc.jwks.OidcJsonWebKeystoreGeneratorService;
import org.apereo.cas.util.scripting.WatchableGroovyScriptResource;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

import java.nio.charset.StandardCharsets;

/**
 * This is {@link OidcGroovyJsonWebKeystoreGeneratorService}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@Slf4j
public class OidcGroovyJsonWebKeystoreGeneratorService implements OidcJsonWebKeystoreGeneratorService {
    private final WatchableGroovyScriptResource watchableScript;

    public OidcGroovyJsonWebKeystoreGeneratorService(final Resource watchableScript) {
        this.watchableScript = new WatchableGroovyScriptResource(watchableScript);
    }

    @Override
    public Resource generate() {
        val args = new Object[]{LOGGER};
        val result = watchableScript.execute(args, String.class);
        LOGGER.debug("Received payload result from [{}] as [{}]", watchableScript, result);
        return new ByteArrayResource(result.getBytes(StandardCharsets.UTF_8), "OIDC JWKS");
    }
}
