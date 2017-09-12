package org.apereo.cas.memcached.kryo.serial;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.apereo.cas.util.DateTimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * @author Timur Duehr timur.duehr@nccgroup.trust
 * @since 5.0.0
 */
public class ZonedDateTimeTranscoder extends Serializer<ZonedDateTime> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ZonedDateTimeTranscoder.class);
    
    @Override
    public void write(final Kryo kryo, final Output output, final ZonedDateTime dateTime) {
        kryo.writeObject(output, dateTime.toInstant().toEpochMilli());
        final String id = dateTime.getZone().getId();
        kryo.writeObject(output, id);
    }

    @Override
    public ZonedDateTime read(final Kryo kryo, final Input input, final Class<ZonedDateTime> type) {
        final long time = kryo.readObject(input, Long.class);
        final String zoneId = input.readString();
        try {
            final ZoneId zone = ZoneId.of(zoneId);
            return DateTimeUtils.zonedDateTimeOf(time, zone);
        } catch (final Exception e) {
            LOGGER.warn("Unable to parse a zoned datetime instance with time [{}] and zone id [{}]: [{}]", time, zoneId, e.getMessage());
            return DateTimeUtils.zonedDateTimeOf(time, ZoneId.systemDefault());
        }
    }
}
