package org.apereo.cas.memcached.kryo.serial;

import module java.base;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.ImmutableSerializer;

/**
 * Kryo serializer for the system clocks of {@link Clock#system(ZoneId)}. The clock is written as its zone id and read
 * back as the system clock of that zone, so the private zone field of the clock is not read through reflection,
 * which would require opening {@code java.time} to the application.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
public class SystemClockSerializer extends ImmutableSerializer<Clock> {

    @Override
    public void write(final Kryo kryo, final Output output, final Clock clock) {
        output.writeString(clock.getZone().getId());
    }

    @Override
    public Clock read(final Kryo kryo, final Input input, final Class<? extends Clock> type) {
        return Clock.system(ZoneId.of(input.readString()));
    }
}
