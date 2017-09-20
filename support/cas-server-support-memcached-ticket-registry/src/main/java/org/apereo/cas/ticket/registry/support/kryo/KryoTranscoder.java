package org.apereo.cas.ticket.registry.support.kryo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
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
import org.apereo.cas.authentication.principal.cache.AbstractPrincipalAttributesRepository;
import org.apereo.cas.authentication.principal.cache.CachingPrincipalAttributesRepository;
import org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy;
import org.apereo.cas.services.LogoutType;
import org.apereo.cas.services.PrincipalAttributeRegisteredServiceUsernameProvider;
import org.apereo.cas.services.RegexMatchingRegisteredServiceProxyPolicy;
import org.apereo.cas.services.RegexRegisteredService;
import org.apereo.cas.services.RegisteredServicePublicKeyImpl;
import org.apereo.cas.services.ReturnAllowedAttributeReleasePolicy;
import org.apereo.cas.services.support.RegisteredServiceRegexAttributeFilter;
import org.apereo.cas.ticket.ServiceTicketImpl;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.apereo.cas.ticket.registry.EncodedTicket;
import org.apereo.cas.ticket.registry.support.kryo.serial.RegisteredServiceSerializer;
import org.apereo.cas.ticket.registry.support.kryo.serial.SimpleWebApplicationServiceSerializer;
import org.apereo.cas.ticket.registry.support.kryo.serial.URLSerializer;
import org.apereo.cas.ticket.registry.support.kryo.serial.ZonedDateTimeTranscoder;
import org.apereo.cas.ticket.support.AlwaysExpiresExpirationPolicy;
import org.apereo.cas.ticket.support.HardTimeoutExpirationPolicy;
import org.apereo.cas.ticket.support.MultiTimeUseOrTimeoutExpirationPolicy;
import org.apereo.cas.ticket.support.NeverExpiresExpirationPolicy;
import org.apereo.cas.ticket.support.RememberMeDelegatingExpirationPolicy;
import org.apereo.cas.ticket.support.ThrottledUseAndTimeoutExpirationPolicy;
import org.apereo.cas.ticket.support.TicketGrantingTicketExpirationPolicy;
import org.apereo.cas.ticket.support.TimeoutExpirationPolicy;
import org.apereo.cas.util.crypto.PublicKeyFactoryBean;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.URL;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * {@link net.spy.memcached.MemcachedClient} transcoder implementation based on Kryo fast serialization framework
 * suited for efficient serialization of tickets.
 *
 * @author Marvin S. Addison
 * @since 3.0.0
 */
@SuppressWarnings("rawtypes")
public class KryoTranscoder implements Transcoder<Object> {

    /**
     * Kryo serializer.
     */
    private final Kryo kryo = new KryoReflectionFactorySupport();

    /**
     * Map of class to serializer that handles it.
     */
    private Map<Class<?>, Serializer> serializerMap;

    /**
     * Creates a Kryo-based transcoder.
     */
    public KryoTranscoder() {
    }

    /**
     * Sets a map of additional types that should be registered with Kryo,
     * for example GoogleAccountsService and OpenIdService.
     *
     * @param map Map of class to the serializer instance that handles it.
     */
    public void setSerializerMap(final Map<Class<?>, Serializer> map) {
        this.serializerMap = map;
    }

    /**
     * Initialize and register classes with kryo.
     */
    @PostConstruct
    public void initialize() {
        // Register types we know about and do not require external configuration
        this.kryo.register(EncodedTicket.class);
        this.kryo.register(ArrayList.class);
        this.kryo.register(BasicCredentialMetaData.class);
        this.kryo.register(Class.class, new DefaultSerializers.ClassSerializer());
        this.kryo.register(ZonedDateTime.class, new ZonedDateTimeTranscoder());
        this.kryo.register(HardTimeoutExpirationPolicy.class);
        this.kryo.register(HashMap.class);
        this.kryo.register(LinkedHashMap.class);
        this.kryo.register(HashSet.class);
        Set singletonSet = Collections.singleton("test");
        this.kryo.register(singletonSet.getClass());
        Map singletonMap = Collections.singletonMap("key", "value");
        this.kryo.register(singletonMap.getClass());
        this.kryo.register(DefaultHandlerResult.class);
        this.kryo.register(DefaultAuthentication.class);
        this.kryo.register(MultiTimeUseOrTimeoutExpirationPolicy.class);
        this.kryo.register(MultiTimeUseOrTimeoutExpirationPolicy.ProxyTicketExpirationPolicy.class);
        this.kryo.register(MultiTimeUseOrTimeoutExpirationPolicy.ServiceTicketExpirationPolicy.class);
        this.kryo.register(NeverExpiresExpirationPolicy.class);
        this.kryo.register(AlwaysExpiresExpirationPolicy.class);
        this.kryo.register(RememberMeDelegatingExpirationPolicy.class);
        this.kryo.register(ServiceTicketImpl.class);
        this.kryo.register(SimpleWebApplicationServiceImpl.class, new SimpleWebApplicationServiceSerializer());
        this.kryo.register(ThrottledUseAndTimeoutExpirationPolicy.class);
        this.kryo.register(TicketGrantingTicketExpirationPolicy.class);
        this.kryo.register(TicketGrantingTicketImpl.class);
        this.kryo.register(TimeoutExpirationPolicy.class);
        this.kryo.register(UsernamePasswordCredential.class);
        this.kryo.register(SimplePrincipal.class);
        this.kryo.register(URL.class, new URLSerializer());
        this.kryo.register(URI.class, new URISerializer());
        this.kryo.register(Pattern.class, new RegexSerializer());
        this.kryo.register(UUID.class, new UUIDSerializer());
        this.kryo.register(EnumMap.class, new EnumMapSerializer());
        this.kryo.register(EnumSet.class, new EnumSetSerializer());
        this.kryo.register(LogoutType.class);
        this.kryo.register(RegisteredServicePublicKeyImpl.class);
        this.kryo.register(PublicKeyFactoryBean.class);
        this.kryo.register(RegexMatchingRegisteredServiceProxyPolicy.class);
        this.kryo.register(ReturnAllowedAttributeReleasePolicy.class);
        this.kryo.register(CachingPrincipalAttributesRepository.class);
        this.kryo.register(AbstractPrincipalAttributesRepository.class);
        this.kryo.register(RegisteredServiceRegexAttributeFilter.class);
        this.kryo.register(PrincipalAttributeRegisteredServiceUsernameProvider.class);
        this.kryo.register(DefaultRegisteredServiceAccessStrategy.class);

        // we add these ones for tests only
        this.kryo.register(RegexRegisteredService.class, new RegisteredServiceSerializer());

        // from the kryo-serializers library (https://github.com/magro/kryo-serializers)
        UnmodifiableCollectionsSerializer.registerSerializers(this.kryo);
        ImmutableListSerializer.registerSerializers(this.kryo);
        ImmutableSetSerializer.registerSerializers(this.kryo);
        ImmutableMapSerializer.registerSerializers(this.kryo);
        ImmutableMultimapSerializer.registerSerializers(this.kryo);

        this.kryo.register(Collections.EMPTY_LIST.getClass(), new CollectionsEmptyListSerializer());
        this.kryo.register(Collections.EMPTY_MAP.getClass(), new CollectionsEmptyMapSerializer());
        this.kryo.register(Collections.EMPTY_SET.getClass(), new CollectionsEmptySetSerializer());

        // Register other types
        if (this.serializerMap != null) {
            this.serializerMap.forEach(this.kryo::register);
        }

        // Note: There are classes from other modules (e.g. cas-server-support-oauth) which
        // *should* be registered (e.g. OAuthCodeExpirationPolicy), but cannot because it would
        // introduce some bad cross dependencies (i.e. right now OAuth is optional, we want it
        // to stay that way).
        // So - a more general mechanism for registering the Serializable classes from various
        // modules is required in order to resolve that.  Until that happens, then Kryo Deserialization
        // may break if any of these unregistered classes are referenced in any Tickets being pushed
        // to memcached.  To catch this early, we have Kryo log a warning at Serialization time
        // if an unregistered class is encountered.

        // don't reinit the registered classes after every write or read
        this.kryo.setAutoReset(false);
        // don't replace objects by references
        this.kryo.setReferences(false);
        // Catchall for any classes not explicitly registered.  Don't throw an exception
        // if an unregistered class is encountered.
        this.kryo.setRegistrationRequired(false);
        //  However, we would like at least a warning in the logs.
        this.kryo.setWarnUnregisteredClasses(true);
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
