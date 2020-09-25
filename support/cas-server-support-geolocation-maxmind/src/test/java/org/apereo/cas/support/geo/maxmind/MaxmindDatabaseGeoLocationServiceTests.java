package org.apereo.cas.support.geo.maxmind;

import com.maxmind.geoip2.DatabaseReader;
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
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.net.InetAddress;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link MaxmindDatabaseGeoLocationServiceTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("Simple")
public class MaxmindDatabaseGeoLocationServiceTests {

    @Test
    public void verifyCity() throws Exception {
        val cityReader = mock(DatabaseReader.class);
        val cityResponse = new CityResponse(new City(), new Continent(), new Country(),
            new Location(10, 100, 40D, 70D, 1, 1, "UTC"), new MaxMind(), new Postal(),
            new Country(), new RepresentedCountry(), new ArrayList<>(), new Traits());
        when(cityReader.city(any())).thenReturn(cityResponse);
        val service = new MaxmindDatabaseGeoLocationService(cityReader, null);
        val response = service.locate("127.0.0.1");
        assertNotNull(response);
    }

    @Test
    public void verifyCityUnknown() throws Exception {
        val cityReader = mock(DatabaseReader.class);
        when(cityReader.city(any())).thenThrow(new AddressNotFoundException("Unknown"));
        val service = new MaxmindDatabaseGeoLocationService(cityReader, null);
        val response = service.locate("127.0.0.1");
        assertNull(response);
    }

    @Test
    public void verifyNoReader() {
        val service = new MaxmindDatabaseGeoLocationService(null, null);
        val response = service.locate("127.0.0.1");
        assertNull(response);
    }

    @Test
    public void verifyLocate() {
        val service = new MaxmindDatabaseGeoLocationService(null, null);
        val response = service.locate("abcedf");
        assertNull(response);
    }

    @Test
    public void verifyOperation() throws Exception {
        val city = mock(DatabaseReader.class);
        val cityResponse = new CityResponse(new City(), new Continent(), new Country(),
            new Location(), new MaxMind(), new Postal(),
            new Country(), new RepresentedCountry(), new ArrayList<>(), new Traits());
        when(city.city(any(InetAddress.class))).thenReturn(cityResponse);

        val country = mock(DatabaseReader.class);
        val countryResponse = new CountryResponse(new Continent(), new Country(),
            new MaxMind(), new Country(),
            new RepresentedCountry(), new Traits());
        when(country.country(any(InetAddress.class))).thenReturn(countryResponse);

        val service = new MaxmindDatabaseGeoLocationService(city, country);
        val response = service.locate("127.0.0.1");
        assertNotNull(response);
        val response2 = service.locate(100D, 100D);
        assertNull(response2);
    }
}
