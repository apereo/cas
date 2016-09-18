package org.apereo.cas.ticket.registry.support.kryo.serial;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.apereo.cas.util.DateTimeUtils;

import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * @author Timur Duehr timur.duehr@nccgroup.trust
 * @since 5.0.0
 */
public class ZonedDateTimeTranscoder extends Serializer<ZonedDateTime> {
    @Override
    public void write(final Kryo kryo, final Output output, final ZonedDateTime dateTime) {
        kryo.writeObject(output, dateTime.toInstant().toEpochMilli());
        kryo.writeObject(output, dateTime.getZone().getId());
    }

    @Override
    public ZonedDateTime read(final Kryo kryo, final Input input, final Class<ZonedDateTime> type) {
        final long time = kryo.readObject(input, Long.class);
        final ZoneId zone = ZoneId.of(input.readString());
        return DateTimeUtils.zonedDateTimeOf(time, zone);
    }
}
