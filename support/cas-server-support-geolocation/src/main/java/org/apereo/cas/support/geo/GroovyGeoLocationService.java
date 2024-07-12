package org.apereo.cas.support.geo;

import org.apereo.cas.authentication.adaptive.geo.GeoLocationResponse;
import org.apereo.cas.util.scripting.ExecutableCompiledScript;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.ApplicationContext;
import java.net.InetAddress;

/**
 * This is {@link GroovyGeoLocationService}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@RequiredArgsConstructor
@Slf4j
public class GroovyGeoLocationService extends AbstractGeoLocationService {
    private final ExecutableCompiledScript watchableScript;

    private final ApplicationContext applicationContext;

    @Override
    public GeoLocationResponse locate(final InetAddress address) throws Throwable {
        val args = new Object[]{address, applicationContext, LOGGER};
        return watchableScript.execute("locateByAddress", GeoLocationResponse.class, args);
    }

    @Override
    public GeoLocationResponse locate(final Double latitude, final Double longitude) throws Throwable {
        val args = new Object[]{latitude, longitude, applicationContext, LOGGER};
        return watchableScript.execute("locateByCoordinates", GeoLocationResponse.class, args);
    }
}
