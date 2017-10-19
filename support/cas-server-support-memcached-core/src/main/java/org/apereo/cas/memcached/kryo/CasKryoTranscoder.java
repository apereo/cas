package org.apereo.cas.memcached.kryo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.DefaultSerializers;
import de.javakaffee.kryoserializers.ArraysAsListSerializer;
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
import org.apereo.cas.authentication.BasicIdentifiableCredential;
import org.apereo.cas.authentication.DefaultAuthentication;
import org.apereo.cas.authentication.DefaultHandlerResult;
import org.apereo.cas.authentication.UsernamePasswordCredential;
import org.apereo.cas.authentication.principal.SimplePrincipal;
import org.apereo.cas.authentication.principal.SimpleWebApplicationServiceImpl;
import org.apereo.cas.authentication.principal.cache.AbstractPrincipalAttributesRepository;
import org.apereo.cas.authentication.principal.cache.CachingPrincipalAttributesRepository;
import org.apereo.cas.memcached.kryo.serial.RegisteredServiceSerializer;
import org.apereo.cas.memcached.kryo.serial.SimpleWebApplicationServiceSerializer;
import org.apereo.cas.memcached.kryo.serial.URLSerializer;
import org.apereo.cas.memcached.kryo.serial.ZonedDateTimeTranscoder;
import org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy;
import org.apereo.cas.services.DefaultRegisteredServiceContact;
import org.apereo.cas.services.DefaultRegisteredServiceExpirationPolicy;
import org.apereo.cas.services.DefaultRegisteredServiceMultifactorPolicy;
import org.apereo.cas.services.PrincipalAttributeRegisteredServiceUsernameProvider;
import org.apereo.cas.services.RegexMatchingRegisteredServiceProxyPolicy;
import org.apereo.cas.services.RegexRegisteredService;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceMultifactorPolicy;
import org.apereo.cas.services.RegisteredServicePublicKeyImpl;
import org.apereo.cas.services.ReturnAllAttributeReleasePolicy;
import org.apereo.cas.services.ReturnAllowedAttributeReleasePolicy;
import org.apereo.cas.services.ReturnMappedAttributeReleasePolicy;
import org.apereo.cas.services.consent.DefaultRegisteredServiceConsentPolicy;
import org.apereo.cas.services.support.RegisteredServiceRegexAttributeFilter;
import org.apereo.cas.ticket.ProxyGrantingTicketImpl;
import org.apereo.cas.ticket.ProxyTicketImpl;
import org.apereo.cas.ticket.ServiceTicketImpl;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.apereo.cas.ticket.registry.EncodedTicket;
import org.apereo.cas.ticket.support.AlwaysExpiresExpirationPolicy;
import org.apereo.cas.ticket.support.BaseDelegatingExpirationPolicy;
import org.apereo.cas.ticket.support.HardTimeoutExpirationPolicy;
import org.apereo.cas.ticket.support.MultiTimeUseOrTimeoutExpirationPolicy;
import org.apereo.cas.ticket.support.NeverExpiresExpirationPolicy;
import org.apereo.cas.ticket.support.RememberMeDelegatingExpirationPolicy;
import org.apereo.cas.ticket.support.ThrottledUseAndTimeoutExpirationPolicy;
import org.apereo.cas.ticket.support.TicketGrantingTicketExpirationPolicy;
import org.apereo.cas.ticket.support.TimeoutExpirationPolicy;
import org.apereo.cas.util.crypto.PublicKeyFactoryBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.AccountNotFoundException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.URL;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * {@link net.spy.memcached.MemcachedClient} transcoder implementation based on Kryo fast serialization framework
 * suited for efficient serialization of tickets.
 * <p>
 * We can’t use an auto-register type approach, because the sequence of class registration has to be deterministic.
 * So – for example – if we register in the sequence in which we encounter classes, then if you have two (or more)
 * instances of CAS running (e.g. for HA or horizontal scaling), then they would likely register classes in a different sequence.
 * The sequence is a factor in assigning an internal Kryo integer id to each class.
 * And, Kryo serializes be embedding that class id into the object, rather than the class name
 * (makes for a much smaller serialized entity).  So – if the same class gets registered in both instances,
 * but get different id’s, then the same cached object would deserialize differently (if we’re lucky it would throw an exception,
 * if we’re unlucky it would contain bad data).  Or – a class gets registered in one instance, but not the other.
 * So – it needs to be pre-registered, and in a deterministic sequence.
 * </p>
 *
 * @author Marvin S. Addison
 * @author Misagh Moayyed
 * @since 3.0.0
 */
public class CasKryoTranscoder implements Transcoder<Object> {
    private static final Logger LOGGER = LoggerFactory.getLogger(CasKryoTranscoder.class);

    /**
     * Kryo serializer.
     */
    private final Kryo kryo;

    /**
     * Instantiates a new Cas kryo transcoder.
     *
     * @param classesToRegister the classes to register
     */
    public CasKryoTranscoder(final Collection<Class> classesToRegister) {
        this.kryo = new KryoReflectionFactorySupport();

        registerCasAuthenticationWithKryo();
        registerExpirationPoliciesWithKryo();
        registerCasTicketsWithKryo();
        registerNativeJdkComponentsWithKryo();
        registerCasServicesWithKryo();
        registerImmutableOrEmptyCollectionsWithKryo();

        classesToRegister.stream().forEach(c -> {
            LOGGER.debug("Registering serializable class [{}] with Kryo", c.getName());
            this.kryo.register(c);
        });

        setWarnUnregisteredClasses(true);
        setAutoReset(false);
        setReplaceObjectsByReferences(false);
        setRegistrationRequired(false);
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
        this.kryo.setReferences(value);
    }

    /**
     * Sets auto reset.
     * Re-init the registered classes after every write or read.
     *
     * @param value the value
     */
    public void setAutoReset(final boolean value) {
        this.kryo.setAutoReset(value);
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

        // Can't directly access Collections classes (private class), so instantiate one and do a getClass().
        final Set singletonSet = Collections.singleton("key");
        this.kryo.register(singletonSet.getClass());
        final Map singletonMap = Collections.singletonMap("key", "value");
        this.kryo.register(singletonMap.getClass());
        final List list = Arrays.asList("key");
        this.kryo.register(list.getClass(), new ArraysAsListSerializer());
    }

    private void registerCasServicesWithKryo() {
        this.kryo.register(RegexRegisteredService.class, new RegisteredServiceSerializer());
        this.kryo.register(RegisteredService.LogoutType.class);
        this.kryo.register(RegisteredServicePublicKeyImpl.class);
        this.kryo.register(DefaultRegisteredServiceContact.class);
        this.kryo.register(DefaultRegisteredServiceExpirationPolicy.class);
        this.kryo.register(RegisteredServiceRegexAttributeFilter.class);
        this.kryo.register(PrincipalAttributeRegisteredServiceUsernameProvider.class);
        this.kryo.register(DefaultRegisteredServiceAccessStrategy.class);
        this.kryo.register(RegexMatchingRegisteredServiceProxyPolicy.class);
        this.kryo.register(RegisteredServiceMultifactorPolicy.FailureModes.class);
    }

    private void registerCasAuthenticationWithKryo() {
        
        this.kryo.register(SimpleWebApplicationServiceImpl.class, new SimpleWebApplicationServiceSerializer());
        this.kryo.register(BasicCredentialMetaData.class);
        this.kryo.register(BasicIdentifiableCredential.class);
        this.kryo.register(DefaultHandlerResult.class);
        this.kryo.register(DefaultAuthentication.class);
        this.kryo.register(UsernamePasswordCredential.class);
        this.kryo.register(SimplePrincipal.class);

        this.kryo.register(PublicKeyFactoryBean.class);
        this.kryo.register(ReturnAllowedAttributeReleasePolicy.class);
        this.kryo.register(ReturnAllAttributeReleasePolicy.class);
        this.kryo.register(ReturnMappedAttributeReleasePolicy.class);
        this.kryo.register(CachingPrincipalAttributesRepository.class);
        this.kryo.register(AbstractPrincipalAttributesRepository.class);
        this.kryo.register(AbstractPrincipalAttributesRepository.MergingStrategy.class);
        this.kryo.register(DefaultRegisteredServiceConsentPolicy.class);
        this.kryo.register(AccountNotFoundException.class);
        this.kryo.register(DefaultRegisteredServiceMultifactorPolicy.class);
    }

    private void registerCasTicketsWithKryo() {
        this.kryo.register(TicketGrantingTicketImpl.class);
        this.kryo.register(ServiceTicketImpl.class);
        this.kryo.register(ProxyGrantingTicketImpl.class);
        this.kryo.register(ProxyTicketImpl.class);
        this.kryo.register(EncodedTicket.class);
    }

    private void registerNativeJdkComponentsWithKryo() {
        this.kryo.register(Class.class, new DefaultSerializers.ClassSerializer());
        this.kryo.register(ZonedDateTime.class, new ZonedDateTimeTranscoder());
        this.kryo.register(ArrayList.class);
        this.kryo.register(HashMap.class);
        this.kryo.register(LinkedHashMap.class);
        this.kryo.register(LinkedHashSet.class);
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
        this.kryo.register(BaseDelegatingExpirationPolicy.class);
    }

    /**
     * Asynchronous decoding is not supported.
     *
     * @param d Data to decode.
     * @return false.
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
     * Keep this package-private to only allow access to various tests that might need it.
     * @return Underlying Kryo instance.
     */
    Kryo getKryo() {
        return this.kryo;
    }
}
