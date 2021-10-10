package org.apereo.cas.support.geo;

import org.apereo.cas.authentication.CasSSLContext;
import org.apereo.cas.authentication.adaptive.geo.GeoLocationRequest;
import org.apereo.cas.authentication.adaptive.geo.GeoLocationResponse;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import javax.net.ssl.HttpsURLConnection;
import java.net.InetAddress;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link GeoLocationServiceTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("Simple")
public class GeoLocationServiceTests {
    @Test
    public void verifyLocate() {
        HttpsURLConnection.setDefaultHostnameVerifier(CasSSLContext.disabled().getHostnameVerifier());
        HttpsURLConnection.setDefaultSSLSocketFactory(CasSSLContext.disabled().getSslContext().getSocketFactory());
        val svc = new DummyGeoLocationService();
        assertNotNull(svc.locate("1.2.3.4", new GeoLocationRequest(1, 1)));
        assertNotNull(svc.locate(new GeoLocationRequest(1, 1)));
    }

    @Test
    public void verifyLocateFails() {
        val svc = mock(AbstractGeoLocationService.class);
        when(svc.locate(anyString())).thenReturn(null);
        when(svc.locate(anyString(), any(GeoLocationRequest.class))).thenCallRealMethod();
        when(svc.locate(anyDouble(), anyDouble())).thenReturn(new GeoLocationResponse());
        assertNotNull(svc.locate("1.2.3.4", new GeoLocationRequest(1, 1)));
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
