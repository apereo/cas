package org.apereo.cas.impl.mock;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apereo.cas.authentication.adaptive.geo.GeoLocationRequest;
import org.apereo.cas.support.events.ticket.CasTicketGrantingTicketCreatedEvent;
import org.apereo.cas.support.events.dao.CasEvent;
import org.apereo.cas.support.events.CasEventRepository;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.serialization.TicketIdSanitizationUtils;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

/**
 * This is {@link MockTicketGrantingTicketCreatedEventProducer}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class MockTicketGrantingTicketCreatedEventProducer {

    private static final List<String> ALL_USER_AGENTS = CollectionUtils.wrapList(
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.143 Safari/537.36",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X x.y; rv:10.0) Gecko/20100101 Firefox/10.0",
            "Mozilla/5.0 (Windows NT 10.1; rv:10.0) Gecko/20100101 Firefox/10.0",
            "Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; Trident/5.0)",
            "Mozilla/5.0 (Windows NT 6.3; Trident/7.0; rv:11.0) like Gecko",
            "Mozilla/5.0 (Android 4.4; Tablet; rv:41.0) Gecko/41.0 Firefox/41.0",
            "Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.2; Trident/6.0)"
    );

    private static final List<String> ALL_IP_ADDRS = Arrays.asList(
            "107.181.69.221",
            "85.90.227.224",
            "88.190.229.170",
            "84.112.33.25",
            "160.180.130.93",
            "91.185.129.0",
            "95.31.18.119",
            "5.190.113.226",
            "68.178.213.203",
            "17.173.254.223",
            "216.15.125.0",
            "196.25.255.250",
            "219.75.27.16",
            "201.83.41.11",
            "93.210.15.68",
            "217.31.113.162",
            "98.167.59.226",
            "70.114.164.59",
            "72.201.90.0",
            "119.235.235.85",
            "219.93.183.103"
    );

    private static final List<Pair<String, String>> ALL_GEOLOCS = Arrays.asList(
            Pair.of("40.71", "-74.005"),
            Pair.of("48.85", "2.35"),
            Pair.of("45.46", "9.18"),
            Pair.of("34.04", "-111.09"),
            Pair.of("36.204", "138.25"),
            Pair.of("29.59", "52.58"),
            Pair.of("55.75", "37.61"),
            Pair.of("41.902", "12.49"),
            Pair.of("35.68", "51.38"),
            Pair.of("42.36", "-71.05"),
            Pair.of("36.77", "-119.417"),
            Pair.of("36.169", "-115.13"),
            Pair.of("32.77", "-96.796"),
            Pair.of("43.65", "-79.38"),
            Pair.of("51.507", "-0.127"),
            Pair.of("53.48", "-2.242"),
            Pair.of("40.05", "-74.405"),
            Pair.of("35", "-97.09"),
            Pair.of("53.41", "-8.24"),
            Pair.of("-38.41", "-63.61")
    );

    protected MockTicketGrantingTicketCreatedEventProducer() {
    }

    private static String getMockUserAgent() {
        final int index = ThreadLocalRandom.current().nextInt(ALL_USER_AGENTS.size());
        return ALL_USER_AGENTS.get(index);
    }

    private static GeoLocationRequest getMockGeoLocation() {
        final int index = ThreadLocalRandom.current().nextInt(ALL_GEOLOCS.size());
        final GeoLocationRequest location = new GeoLocationRequest();
        final Pair<String, String> pair = ALL_GEOLOCS.get(index);
        location.setLatitude(pair.getKey());
        location.setLongitude(pair.getValue());
        location.setAccuracy("50");
        location.setTimestamp(String.valueOf(new Date().getTime()));
        return location;
    }

    private static String getMockClientIpAddress() {
        final int index = ThreadLocalRandom.current().nextInt(ALL_IP_ADDRS.size());
        return ALL_IP_ADDRS.get(index);
    }

    private static void createEvent(final int i, final CasEventRepository casEventRepository) {
        final CasEvent dto = new CasEvent();
        dto.setType(CasTicketGrantingTicketCreatedEvent.class.getName());
        dto.putTimestamp(new Date().getTime());
        final int days = ThreadLocalRandom.current().nextInt(0, 30);
        dto.setCreationTime(ZonedDateTime.now().minusDays(days).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        dto.putId(TicketIdSanitizationUtils.sanitize("TGT-" + i + "-" + RandomStringUtils.randomAlphanumeric(16)));
        dto.setPrincipalId("casuser");
        dto.putClientIpAddress(getMockClientIpAddress());
        dto.putServerIpAddress("127.0.0.1");
        dto.putAgent(getMockUserAgent());
        dto.putGeoLocation(getMockGeoLocation());
        casEventRepository.save(dto);
    }


    public static void createEvents(final CasEventRepository casEventRepository) {
        IntStream.range(1, 1000).forEach(i -> createEvent(i, casEventRepository));
    }

}
