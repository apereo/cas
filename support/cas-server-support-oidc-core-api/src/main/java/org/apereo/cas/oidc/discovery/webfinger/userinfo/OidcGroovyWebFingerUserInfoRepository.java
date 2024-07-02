package org.apereo.cas.oidc.discovery.webfinger.userinfo;

import org.apereo.cas.oidc.discovery.webfinger.OidcWebFingerUserInfoRepository;
import org.apereo.cas.util.scripting.ExecutableCompiledScript;
import org.apereo.cas.util.scripting.ExecutableCompiledScriptFactory;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.core.io.Resource;
import java.util.Map;

/**
 * This is {@link OidcGroovyWebFingerUserInfoRepository}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Slf4j
public class OidcGroovyWebFingerUserInfoRepository implements OidcWebFingerUserInfoRepository, DisposableBean {
    private final ExecutableCompiledScript watchableScript;

    public OidcGroovyWebFingerUserInfoRepository(final Resource resource) {
        val scriptFactory = ExecutableCompiledScriptFactory.getExecutableCompiledScriptFactory();
        this.watchableScript = scriptFactory.fromResource(resource);
    }

    @Override
    public Map<String, Object> findByEmailAddress(final String email) throws Throwable {
        val args = new Object[]{email, LOGGER};
        return watchableScript.execute("findByEmailAddress", Map.class, args);
    }

    @Override
    public Map<String, Object> findByUsername(final String username) throws Throwable {
        val args = new Object[]{username, LOGGER};
        return watchableScript.execute("findByUsername", Map.class, args);
    }

    @Override
    public void destroy() {
        this.watchableScript.close();
    }
}
