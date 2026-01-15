package org.apereo.cas.support.geo.maxmind;

import module java.base;
import org.apereo.cas.configuration.model.support.geo.maxmind.MaxmindProperties;
import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.WebServiceClient;
import com.maxmind.geoip2.exception.AddressNotFoundException;
import com.maxmind.geoip2.model.CityResponse;
import com.maxmind.geoip2.model.CountryResponse;
import com.maxmind.geoip2.record.City;
import com.maxmind.geoip2.record.Continent;
import com.maxmind.geoip2.record.Country;
import com.maxmind.geoip2.record.Location;
import com.maxmind.geoip2.record.MaxMind;
import com.maxmind.geoip2.record.Postal;
import com.maxmind.geoip2.record.RepresentedCountry;
import com.maxmind.geoip2.record.Traits;
import lombok.val;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link MaxmindDatabaseGeoLocationServiceTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("GeoLocation")
class MaxmindDatabaseGeoLocationServiceTests {

    @Test
    void verifyWebServices() throws Throwable {
        var service = new MaxmindDatabaseGeoLocationService(new MaxmindProperties()
            .setAccountId(123456).setLicenseKey("abcdefghi"));
        assertNull(service.locate("5.194.132.155"));
        val client = mock(WebServiceClient.class);
        when(client.city(any())).thenReturn(getCityResponse());
        when(client.country(any())).thenReturn(getCountryResponse());
        service = service.withWebServiceClient(client);
        assertNotNull(service.locate("5.194.132.155"));
    }

    @Test
    void verifyCity() throws Throwable {
        val cityReader = mock(DatabaseReader.class);
        when(cityReader.city(any())).thenReturn(getCityResponse());
        val service = new MaxmindDatabaseGeoLocationService(new MaxmindProperties()).withCityDatabaseReader(cityReader);
        val response = service.locate("127.0.0.1");
        assertNotNull(response);
    }

    private static CityResponse getCityResponse() {
        val location = new Location(10, 100, 40.0D, 70.0D, 1, "UTC");
        return new CityResponse(new City(), new Continent(), new Country(),
            location, new MaxMind(), new Postal(),
            new Country(), new RepresentedCountry(), new ArrayList<>(), new Traits());
    }

    @Test
    void verifyCityUnknown() throws Throwable {
        val cityReader = mock(DatabaseReader.class);
        when(cityReader.city(any())).thenThrow(new AddressNotFoundException("Unknown"));
        val service = new MaxmindDatabaseGeoLocationService(new MaxmindProperties()).withCityDatabaseReader(cityReader);
        val response = service.locate("127.0.0.1");
        assertEquals(0, response.getLatitude());
        assertEquals(0, response.getLongitude());
    }

    @Test
    void verifyNoReader() {
        val service = new MaxmindDatabaseGeoLocationService(new MaxmindProperties());
        val response = service.locate("127.0.0.1");
        assertEquals(0, response.getLatitude());
        assertEquals(0, response.getLongitude());
    }

    @Test
    void verifyLocate() {
        val service = new MaxmindDatabaseGeoLocationService(new MaxmindProperties());
        val response = service.locate("abcedf");
        assertNull(response);
    }

    @Test
    void verifyOperation() throws Throwable {
        val city = mock(DatabaseReader.class);
        when(city.city(any(InetAddress.class))).thenReturn(getCityResponse());

        val country = mock(DatabaseReader.class);
        when(country.country(any(InetAddress.class))).thenReturn(getCountryResponse());

        val service = new MaxmindDatabaseGeoLocationService(new MaxmindProperties())
            .withCityDatabaseReader(city)
            .withCountryDatabaseReader(country);
        val response = service.locate("127.0.0.1");
        assertNotNull(response);
        val response2 = service.locate(100.0D, 100.0D);
        assertNull(response2);
    }

    @NonNull
    private static CountryResponse getCountryResponse() {
        return new CountryResponse(new Continent(), new Country(),
            new MaxMind(), new Country(),
            new RepresentedCountry(), new Traits());
    }
}
