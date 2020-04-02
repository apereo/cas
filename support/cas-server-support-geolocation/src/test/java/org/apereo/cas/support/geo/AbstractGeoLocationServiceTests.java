package org.apereo.cas.support.geo;

import org.apereo.cas.authentication.adaptive.geo.GeoLocationRequest;
import org.apereo.cas.authentication.adaptive.geo.GeoLocationResponse;

import lombok.val;
import org.junit.jupiter.api.Test;

import java.net.InetAddress;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link AbstractGeoLocationServiceTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
public class AbstractGeoLocationServiceTests {
    @Test
    public void verifyLocate() {
        val svc = new DummyGeoLocationService();
        assertNotNull(svc.locate("1.2.3.4", new GeoLocationRequest(1, 1)));
        assertNotNull(svc.locate(new GeoLocationRequest(1, 1)));
    }

    @Test
    public void verifyLocateByIpStack() {
        val svc = new DummyGeoLocationService();
        svc.setIpStackAccessKey("abcdefghijklmnop");
        assertNotNull(svc.locate("0.0.0.0"));
    }

    private static class DummyGeoLocationService extends AbstractGeoLocationService {
        @Override
        public GeoLocationResponse locate(final InetAddress address) {
            return new GeoLocationResponse()
                .setLatitude(1)
                .setLongitude(1)
                .addAddress("1234 Main Street");
        }

        @Override
        public GeoLocationResponse locate(final Double latitude, final Double longitude) {
            return new GeoLocationResponse()
                .setLatitude(1)
                .setLongitude(1)
                .addAddress("1234 Main Street");
        }
    }
}
