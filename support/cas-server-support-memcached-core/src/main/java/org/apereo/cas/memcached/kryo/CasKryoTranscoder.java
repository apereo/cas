package org.apereo.cas.memcached.kryo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.DefaultSerializers;
import de.javakaffee.kryoserializers.CollectionsEmptyListSerializer;
import de.javakaffee.kryoserializers.CollectionsEmptyMapSerializer;
import de.javakaffee.kryoserializers.CollectionsEmptySetSerializer;
import de.javakaffee.kryoserializers.EnumMapSerializer;
import de.javakaffee.kryoserializers.EnumSetSerializer;
import de.javakaffee.kryoserializers.KryoReflectionFactorySupport;
import de.javakaffee.kryoserializers.RegexSerializer;
import de.javakaffee.kryoserializers.URISerializer;
import de.javakaffee.kryoserializers.UUIDSerializer;
import de.javakaffee.kryoserializers.UnmodifiableCollectionsSerializer;
import de.javakaffee.kryoserializers.guava.ImmutableListSerializer;
import de.javakaffee.kryoserializers.guava.ImmutableMapSerializer;
import de.javakaffee.kryoserializers.guava.ImmutableMultimapSerializer;
import de.javakaffee.kryoserializers.guava.ImmutableSetSerializer;
import net.spy.memcached.CachedData;
import net.spy.memcached.transcoders.Transcoder;
import org.apereo.cas.authentication.BasicCredentialMetaData;
import org.apereo.cas.authentication.DefaultAuthentication;
import org.apereo.cas.authentication.DefaultHandlerResult;
import org.apereo.cas.authentication.UsernamePasswordCredential;
import org.apereo.cas.authentication.principal.SimplePrincipal;
import org.apereo.cas.authentication.principal.SimpleWebApplicationServiceImpl;
import org.apereo.cas.memcached.kryo.serial.RegisteredServiceSerializer;
import org.apereo.cas.memcached.kryo.serial.SimpleWebApplicationServiceSerializer;
import org.apereo.cas.memcached.kryo.serial.URLSerializer;
import org.apereo.cas.memcached.kryo.serial.ZonedDateTimeTranscoder;
import org.apereo.cas.services.RegexRegisteredService;
import org.apereo.cas.ticket.ServiceTicketImpl;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.apereo.cas.ticket.registry.EncodedTicket;
import org.apereo.cas.ticket.support.AlwaysExpiresExpirationPolicy;
import org.apereo.cas.ticket.support.HardTimeoutExpirationPolicy;
import org.apereo.cas.ticket.support.MultiTimeUseOrTimeoutExpirationPolicy;
import org.apereo.cas.ticket.support.NeverExpiresExpirationPolicy;
import org.apereo.cas.ticket.support.RememberMeDelegatingExpirationPolicy;
import org.apereo.cas.ticket.support.ThrottledUseAndTimeoutExpirationPolicy;
import org.apereo.cas.ticket.support.TicketGrantingTicketExpirationPolicy;
import org.apereo.cas.ticket.support.TimeoutExpirationPolicy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.URL;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * {@link net.spy.memcached.MemcachedClient} transcoder implementation based on Kryo fast serialization framework
 * suited for efficient serialization of tickets.
 *
 * @author Marvin S. Addison
 * @since 3.0.0
 */
public class CasKryoTranscoder implements Transcoder<Object> {

    /**
     * Kryo serializer.
     */
    private final Kryo kryo;

    /**
     * Map of class to serializer that handles it.
     */
    private final Collection<Class<?>> serializerMap;

    /**
     * Creates a Kryo-based transcoder.
     */
    public CasKryoTranscoder() {
        this(new ArrayList());
    }

    public CasKryoTranscoder(final Collection map) {
        this.kryo = new KryoReflectionFactorySupport();
        this.serializerMap = map;
        
        setWarnUnregisteredClasses(true);
        setAutoReset(false);
        setReplaceObjectsByReferences(false);
        setRegistrationRequired(false);
    }
    
    /**
     * Initialize and register classes with kryo.
     */
    public void initialize() {
        registerCasAuthenticationWithKryo();
        registerExpirationPoliciesWithKryo();
        registerCasTicketsWithKryo();
        registerNativeJdkComponentsWithKryo();
        registerCasServicesWithKryo();
        registerImmutableOrEmptyCollectionsWithKryo();

        this.serializerMap.forEach(this.kryo::register);
    }

    /**
     * If true, kryo writes a warn log telling about the classes unregistered. Default is false.
     * If false, no log are written when unregistered classes are encountered.
     *
     * @param value the value
     */
    public void setWarnUnregisteredClasses(final boolean value) {
        this.kryo.setWarnUnregisteredClasses(value);
    }

    /**
     * Sets registration required.
     * Catch all for any classes not explicitly registered
     *
     * @param value the value
     */
    public void setRegistrationRequired(final boolean value) {
        this.kryo.setRegistrationRequired(value);
    }

    /**
     * Sets replace objects by references.
     *
     * @param value the value
     */
    public void setReplaceObjectsByReferences(final boolean value) {
        this.kryo.setReferences(false);
    }

    /**
     * Sets auto reset.
     * Re-init the registered classes after every write or read.
     *
     * @param value the value
     */
    public void setAutoReset(final boolean value) {
        this.kryo.setAutoReset(false);
    }

    private void registerImmutableOrEmptyCollectionsWithKryo() {
        UnmodifiableCollectionsSerializer.registerSerializers(this.kryo);
        ImmutableListSerializer.registerSerializers(this.kryo);
        ImmutableSetSerializer.registerSerializers(this.kryo);
        ImmutableMapSerializer.registerSerializers(this.kryo);
        ImmutableMultimapSerializer.registerSerializers(this.kryo);

        this.kryo.register(Collections.EMPTY_LIST.getClass(), new CollectionsEmptyListSerializer());
        this.kryo.register(Collections.EMPTY_MAP.getClass(), new CollectionsEmptyMapSerializer());
        this.kryo.register(Collections.EMPTY_SET.getClass(), new CollectionsEmptySetSerializer());
    }

    private void registerCasServicesWithKryo() {
        this.kryo.register(RegexRegisteredService.class, new RegisteredServiceSerializer());
    }

    private void registerCasAuthenticationWithKryo() {
        this.kryo.register(SimpleWebApplicationServiceImpl.class, new SimpleWebApplicationServiceSerializer());
        this.kryo.register(BasicCredentialMetaData.class);
        this.kryo.register(DefaultHandlerResult.class);
        this.kryo.register(DefaultAuthentication.class);
        this.kryo.register(UsernamePasswordCredential.class);
        this.kryo.register(SimplePrincipal.class);
    }

    private void registerCasTicketsWithKryo() {
        this.kryo.register(TicketGrantingTicketImpl.class);
        this.kryo.register(ServiceTicketImpl.class);
        this.kryo.register(EncodedTicket.class);
    }

    private void registerNativeJdkComponentsWithKryo() {
        this.kryo.register(Class.class, new DefaultSerializers.ClassSerializer());
        this.kryo.register(ZonedDateTime.class, new ZonedDateTimeTranscoder());
        this.kryo.register(ArrayList.class);
        this.kryo.register(HashMap.class);
        this.kryo.register(LinkedHashMap.class);
        this.kryo.register(HashSet.class);
        this.kryo.register(URL.class, new URLSerializer());
        this.kryo.register(URI.class, new URISerializer());
        this.kryo.register(Pattern.class, new RegexSerializer());
        this.kryo.register(UUID.class, new UUIDSerializer());
        this.kryo.register(EnumMap.class, new EnumMapSerializer());
        this.kryo.register(EnumSet.class, new EnumSetSerializer());
    }

    private void registerExpirationPoliciesWithKryo() {
        this.kryo.register(MultiTimeUseOrTimeoutExpirationPolicy.class);
        this.kryo.register(MultiTimeUseOrTimeoutExpirationPolicy.ServiceTicketExpirationPolicy.class);
        this.kryo.register(MultiTimeUseOrTimeoutExpirationPolicy.ProxyTicketExpirationPolicy.class);
        this.kryo.register(NeverExpiresExpirationPolicy.class);
        this.kryo.register(RememberMeDelegatingExpirationPolicy.class);
        this.kryo.register(TimeoutExpirationPolicy.class);
        this.kryo.register(HardTimeoutExpirationPolicy.class);
        this.kryo.register(AlwaysExpiresExpirationPolicy.class);
        this.kryo.register(ThrottledUseAndTimeoutExpirationPolicy.class);
        this.kryo.register(TicketGrantingTicketExpirationPolicy.class);
    }

    /**
     * Asynchronous decoding is not supported.
     *
     * @param d Data to decode.
     * @return False.
     */
    @Override
    public boolean asyncDecode(final CachedData d) {
        return false;
    }

    @Override
    public CachedData encode(final Object obj) {
        final ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        try (Output output = new Output(byteStream)) {
            this.kryo.writeClassAndObject(output, obj);
            output.flush();
            final byte[] bytes = byteStream.toByteArray();
            return new CachedData(0, bytes, bytes.length);
        }
    }

    @Override
    public Object decode(final CachedData d) {
        final byte[] bytes = d.getData();
        try (Input input = new Input(new ByteArrayInputStream(bytes))) {
            final Object obj = this.kryo.readClassAndObject(input);
            return obj;
        }
    }

    /**
     * Maximum size of encoded data supported by this transcoder.
     *
     * @return {@code net.spy.memcached.CachedData#MAX_SIZE}.
     */
    @Override
    public int getMaxSize() {
        return CachedData.MAX_SIZE;
    }

    /**
     * Gets the kryo object that provides encoding and decoding services for this instance.
     *
     * @return Underlying Kryo instance.
     */
    public Kryo getKryo() {
        return this.kryo;
    }
}
