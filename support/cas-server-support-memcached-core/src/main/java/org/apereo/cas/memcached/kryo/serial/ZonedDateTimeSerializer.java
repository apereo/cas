package org.apereo.cas.memcached.kryo.serial;

import org.apereo.cas.util.DateTimeUtils;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.RegExUtils;

import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * @author Timur Duehr timur.duehr@nccgroup.trust
 * @since 5.0.0
 */
@Slf4j
public class ZonedDateTimeSerializer extends Serializer<ZonedDateTime> {


    @Override
    public void write(final Kryo kryo, final Output output, final ZonedDateTime dateTime) {
        LOGGER.trace("Writing date/time [{}] to memcached", dateTime);
        val epochMilli = dateTime.toInstant().toEpochMilli();
        LOGGER.trace("Writing date/time epoch milliseconds [{}] to memcached", epochMilli);
        kryo.writeObject(output, epochMilli);

        val id = dateTime.getZone().getId();
        LOGGER.trace("Writing date/time zone id [{}] to memcached", id);
        kryo.writeObject(output, id);
    }

    @Override
    public ZonedDateTime read(final Kryo kryo, final Input input, final Class<ZonedDateTime> type) {
        val time = kryo.readObject(input, Long.class);
        val zoneId = RegExUtils.removeAll(input.readString().trim(), "\\p{C}");
        try {
            LOGGER.trace("Reading zoned date time instance with time [{}] and zone id [{}]", time, zoneId);
            val zone = ZoneId.of(zoneId);
            return DateTimeUtils.zonedDateTimeOf(time, zone);
        } catch (final Exception e) {
            LOGGER.warn("Unable to parse a zoned datetime instance with time [{}] and zone id [{}]: [{}]", time, zoneId, e.getMessage());
            return DateTimeUtils.zonedDateTimeOf(time, ZoneId.systemDefault());
        }
    }
}
