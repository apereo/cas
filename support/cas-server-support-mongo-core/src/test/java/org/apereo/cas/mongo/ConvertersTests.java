package org.apereo.cas.mongo;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.bson.BsonReader;
import org.bson.BsonTimestamp;
import org.bson.BsonWriter;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.codecs.configuration.CodecRegistry;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.time.Clock;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link ConvertersTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("MongoDb")
public class ConvertersTests {

    @Test
    public void verifyOperation() {
        assertNull(new BaseConverters.NullConverter().convert(new Object()));
        assertNull(new BaseConverters.StringToZonedDateTimeConverter().convert(StringUtils.EMPTY));
        assertNull(new BaseConverters.StringToPatternConverter().convert(StringUtils.EMPTY));
        assertNotNull(new BaseConverters.StringToZonedDateTimeConverter().convert(ZonedDateTime.now(Clock.systemUTC()).toString()));
        assertNotNull(new BaseConverters.ZonedDateTimeToStringConverter().convert(ZonedDateTime.now(Clock.systemUTC())));
        assertNotNull(new BaseConverters.BsonTimestampToDateConverter().convert(new BsonTimestamp()));
        assertNotNull(new BaseConverters.BsonTimestampToStringConverter().convert(new BsonTimestamp()));
        assertNotNull(new BaseConverters.ZonedDateTimeTransformer().transform(ZonedDateTime.now(Clock.systemUTC())));
        val codec = new BaseConverters.ZonedDateTimeCodecProvider().get(ZonedDateTime.class, mock(CodecRegistry.class));
        assertNotNull(codec);

        assertDoesNotThrow(new Executable() {
            @Override
            public void execute() {
                codec.encode(mock(BsonWriter.class), ZonedDateTime.now(Clock.systemUTC()), mock(EncoderContext.class));

                val r = mock(BsonReader.class);
                when(r.readTimestamp()).thenReturn(new BsonTimestamp());
                codec.decode(r, mock(DecoderContext.class));
            }
        });
        assertNotNull(codec.getEncoderClass());
    }

}
