package org.apereo.cas.adaptors.u2f.storage;

import org.apereo.cas.util.scripting.WatchableGroovyScriptResource;

import com.github.benmanes.caffeine.cache.LoadingCache;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.core.io.Resource;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * This is {@link U2FGroovyResourceDeviceRepository}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
public class U2FGroovyResourceDeviceRepository extends BaseResourceU2FDeviceRepository implements DisposableBean {
    private final transient WatchableGroovyScriptResource watchableScript;

    public U2FGroovyResourceDeviceRepository(final LoadingCache<String, String> requestStorage,
                                             final Resource groovyScript, final long expirationTime,
                                             final TimeUnit expirationTimeUnit) {
        super(requestStorage, expirationTime, expirationTimeUnit);
        this.watchableScript = new WatchableGroovyScriptResource(groovyScript);
    }

    @Override
    public Map<String, List<U2FDeviceRegistration>> readDevicesFromResource() {
        val args = new Object[]{LOGGER};
        return this.watchableScript.execute("read", Map.class, args);
    }

    @Override
    public void writeDevicesBackToResource(final List<U2FDeviceRegistration> list) {
        val args = new Object[]{list, LOGGER};
        this.watchableScript.execute("write", Boolean.class, args);
        LOGGER.debug("Saved [{}] device(s) into repository [{}]", list.size(), watchableScript.getResource());
    }

    @Override
    public void removeAll() {
        val args = new Object[]{LOGGER};
        this.watchableScript.execute("removeAll", Void.class, args);
        LOGGER.debug("Removed all device records from repository");
    }

    @Override
    public void destroy() {
        this.watchableScript.close();
    }
}
