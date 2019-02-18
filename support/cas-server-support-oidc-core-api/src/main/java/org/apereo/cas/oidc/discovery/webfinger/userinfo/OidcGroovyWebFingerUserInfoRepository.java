package org.apereo.cas.oidc.discovery.webfinger.userinfo;

import org.apereo.cas.oidc.discovery.webfinger.OidcWebFingerUserInfoRepository;
import org.apereo.cas.util.scripting.WatchableGroovyScriptResource;

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
    private final transient WatchableGroovyScriptResource watchableScript;

    public OidcGroovyWebFingerUserInfoRepository(final Resource resource) {
        this.watchableScript = new WatchableGroovyScriptResource(resource);
    }

    @Override
    public Map<String, Object> findByEmailAddress(final String email) {
        val args = new Object[]{email, LOGGER};
        return watchableScript.execute("findByEmailAddress", Map.class, args);
    }

    @Override
    public Map<String, Object> findByUsername(final String username) {
        val args = new Object[]{username, LOGGER};
        return watchableScript.execute("findByUsername", Map.class, args);
    }

    @Override
    public void destroy() {
        this.watchableScript.close();
    }
}
