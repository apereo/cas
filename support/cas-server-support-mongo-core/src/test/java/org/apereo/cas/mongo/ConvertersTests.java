package org.apereo.cas.mongo;

import module java.base;
import org.apereo.cas.authentication.OneTimeTokenAccount;
import org.apereo.cas.configuration.model.support.mongo.SingleCollectionMongoDbProperties;
import org.apereo.cas.util.cipher.JasyptNumberCipherExecutor;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.bson.BsonDocument;
import org.bson.BsonDocumentWriter;
import org.bson.BsonReader;
import org.bson.BsonTimestamp;
import org.bson.BsonWriter;
import org.bson.Document;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link ConvertersTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("MongoDb")
class ConvertersTests {

    @Test
    void verifyOperation() {
        assertNull(new BaseConverters.NullConverter().convert(new Object()));
        assertNull(new BaseConverters.StringToZonedDateTimeConverter().convert(StringUtils.EMPTY));
        assertNull(new BaseConverters.StringToPatternConverter().convert(StringUtils.EMPTY));
        assertNotNull(new BaseConverters.StringToZonedDateTimeConverter().convert(ZonedDateTime.now(Clock.systemUTC()).toString()));
        assertNotNull(new BaseConverters.ZonedDateTimeToStringConverter().convert(ZonedDateTime.now(Clock.systemUTC())));
        assertNotNull(new BaseConverters.BsonTimestampToDateConverter().convert(new BsonTimestamp()));
        assertNotNull(new BaseConverters.BsonTimestampToStringConverter().convert(new BsonTimestamp()));
        assertNotNull(new BaseConverters.ZonedDateTimeTransformer().transform(ZonedDateTime.now(Clock.systemUTC())));
        assertNull(new BaseConverters.StringToBigIntegerConverter().convert(StringUtils.EMPTY));
        assertEquals(BigInteger.TEN.toString(), new BaseConverters.BigIntegerToStringConverter().convert(BigInteger.TEN));
        assertEquals(BigInteger.TEN, new BaseConverters.StringToBigIntegerConverter().convert(BigInteger.TEN.toString()));
        val codec = new BaseConverters.ZonedDateTimeCodecProvider().get(ZonedDateTime.class, mock(CodecRegistry.class));
        assertNotNull(codec);

        assertDoesNotThrow(() -> {
            codec.encode(mock(BsonWriter.class), ZonedDateTime.now(Clock.systemUTC()), mock(EncoderContext.class));

            val r = mock(BsonReader.class);
            when(r.readTimestamp()).thenReturn(new BsonTimestamp());
            codec.decode(r, mock(DecoderContext.class));
        });
        assertNotNull(codec.getEncoderClass());
    }

    @Test
    void verifyEncryptedScratchCodesAreEncodable() {
        val properties = new SingleCollectionMongoDbProperties();
        properties.setDatabaseName("cas");
        val mongoTemplate = new MongoDbConnectionFactory().buildMongoTemplate(mock(MongoClient.class), properties);

        val cipher = new JasyptNumberCipherExecutor(UUID.randomUUID().toString(), "scratchCodes");
        val scratchCode = 12345678;
        val account = OneTimeTokenAccount.builder()
            .username("casuser")
            .name("casuser")
            .secretKey(UUID.randomUUID().toString())
            .validationCode(123456)
            .scratchCodes(new ArrayList<>(List.of(cipher.encode(scratchCode))))
            .build();
        assertInstanceOf(BigInteger.class, account.getScratchCodes().getFirst());

        val document = new Document();
        mongoTemplate.getConverter().write(account, document);

        val codecRegistry = CodecRegistries.fromRegistries(
            CodecRegistries.fromProviders(new BaseConverters.ZonedDateTimeCodecProvider()),
            MongoClientSettings.getDefaultCodecRegistry());
        assertDoesNotThrow(() -> codecRegistry.get(Document.class)
            .encode(new BsonDocumentWriter(new BsonDocument()), document, EncoderContext.builder().build()));
        assertInstanceOf(String.class, document.getList("scratchCodes", Object.class).getFirst());

        val readAccount = mongoTemplate.getConverter().read(OneTimeTokenAccount.class, document);
        assertEquals(scratchCode, cipher.decode(readAccount.getScratchCodes().getFirst()).intValue());
    }
}
