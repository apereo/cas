package org.apereo.cas.memcached.kryo;

import org.apereo.cas.memcached.kryo.serial.ImmutableNativeJavaListSerializer;
import org.apereo.cas.memcached.kryo.serial.ImmutableNativeJavaMapSerializer;
import org.apereo.cas.memcached.kryo.serial.ImmutableNativeJavaSetSerializer;
import org.apereo.cas.memcached.kryo.serial.ThrowableSerializer;
import org.apereo.cas.memcached.kryo.serial.URLSerializer;
import org.apereo.cas.memcached.kryo.serial.ZonedDateTimeSerializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.serializers.DefaultSerializers;
import com.esotericsoftware.kryo.util.DefaultInstantiatorStrategy;
import de.javakaffee.kryoserializers.ArraysAsListSerializer;
import de.javakaffee.kryoserializers.CollectionsEmptyListSerializer;
import de.javakaffee.kryoserializers.CollectionsEmptyMapSerializer;
import de.javakaffee.kryoserializers.CollectionsEmptySetSerializer;
import de.javakaffee.kryoserializers.DateSerializer;
import de.javakaffee.kryoserializers.EnumMapSerializer;
import de.javakaffee.kryoserializers.EnumSetSerializer;
import de.javakaffee.kryoserializers.GregorianCalendarSerializer;
import de.javakaffee.kryoserializers.RegexSerializer;
import de.javakaffee.kryoserializers.URISerializer;
import de.javakaffee.kryoserializers.UUIDSerializer;
import de.javakaffee.kryoserializers.UnmodifiableCollectionsSerializer;
import de.javakaffee.kryoserializers.guava.ImmutableListSerializer;
import de.javakaffee.kryoserializers.guava.ImmutableMapSerializer;
import de.javakaffee.kryoserializers.guava.ImmutableMultimapSerializer;
import de.javakaffee.kryoserializers.guava.ImmutableSetSerializer;
import de.javakaffee.kryoserializers.jodatime.JodaDateTimeSerializer;
import de.javakaffee.kryoserializers.jodatime.JodaLocalDateSerializer;
import de.javakaffee.kryoserializers.jodatime.JodaLocalDateTimeSerializer;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;
import org.objenesis.strategy.StdInstantiatorStrategy;
import org.springframework.beans.factory.FactoryBean;

import javax.security.auth.login.AccountNotFoundException;
import java.net.URI;
import java.net.URL;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * This is {@link CloseableKryoFactory}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
@Setter
@RequiredArgsConstructor
public class CloseableKryoFactory implements FactoryBean<CloseableKryo> {

    private final CasKryoPool kryoPool;

    private Collection<Class> classesToRegister = new ArrayList<>(0);

    private boolean warnUnregisteredClasses = true;

    private boolean registrationRequired;

    private boolean replaceObjectsByReferences;

    private boolean autoReset;

    @Override
    public CloseableKryo getObject() {
        val kryo = new CloseableKryo(this.kryoPool);
        kryo.setInstantiatorStrategy(new DefaultInstantiatorStrategy(new StdInstantiatorStrategy()));
        kryo.setWarnUnregisteredClasses(this.warnUnregisteredClasses);
        kryo.setAutoReset(this.autoReset);
        kryo.setReferences(this.replaceObjectsByReferences);
        kryo.setRegistrationRequired(this.registrationRequired);

        LOGGER.debug("Constructing a kryo instance with the following settings:");
        LOGGER.debug("warnUnregisteredClasses: [{}]", this.warnUnregisteredClasses);
        LOGGER.debug("autoReset: [{}]", this.autoReset);
        LOGGER.debug("replaceObjectsByReferences: [{}]", this.replaceObjectsByReferences);
        LOGGER.debug("registrationRequired: [{}]", this.registrationRequired);

        registerNativeJdkComponentsWithKryo(kryo);
        registerImmutableOrEmptyCollectionsWithKryo(kryo);

        val classes = new ArrayList<>(classesToRegister);
        classes.sort(Comparator.comparing(Class::getName));
        classes.forEach(c -> {
            LOGGER.trace("Registering serializable class [{}] with Kryo", c.getName());
            kryo.register(c);
        });
        return kryo;
    }

    @Override
    public Class<?> getObjectType() {
        return CloseableKryo.class;
    }

    private static void registerImmutableOrEmptyCollectionsWithKryo(final Kryo kryo) {
        LOGGER.trace("Registering immutable/empty collections with Kryo");

        UnmodifiableCollectionsSerializer.registerSerializers(kryo);

        ImmutableListSerializer.registerSerializers(kryo);
        kryo.register(List.of().getClass(), new ImmutableNativeJavaListSerializer());
        kryo.register(List.class, new ImmutableNativeJavaListSerializer());
        kryo.register(List.of("1").getClass(), new ImmutableNativeJavaListSerializer());
        kryo.register(List.of("1", "2").getClass(), new ImmutableNativeJavaListSerializer());
        kryo.register(List.of("1", "2", "3", "4").getClass(), new ImmutableNativeJavaListSerializer());

        ImmutableSetSerializer.registerSerializers(kryo);
        kryo.register(Set.of().getClass(), new ImmutableNativeJavaSetSerializer());
        kryo.register(Set.class, new ImmutableNativeJavaSetSerializer());
        kryo.register(Set.of("1", "2").getClass(), new ImmutableNativeJavaSetSerializer());
        kryo.register(Set.of("1", "2", "3", "4").getClass(), new ImmutableNativeJavaSetSerializer());

        ImmutableMapSerializer.registerSerializers(kryo);
        kryo.register(Map.of().getClass(), new ImmutableNativeJavaMapSerializer());
        kryo.register(Map.class, new ImmutableNativeJavaMapSerializer());
        kryo.register(Map.of("1", "2").getClass(), new ImmutableNativeJavaMapSerializer());
        kryo.register(Map.of("1", "2", "3", "4", "5", "6").getClass(), new ImmutableNativeJavaMapSerializer());

        ImmutableMultimapSerializer.registerSerializers(kryo);

        kryo.register(Collections.EMPTY_LIST.getClass(), new CollectionsEmptyListSerializer());
        kryo.register(Collections.EMPTY_MAP.getClass(), new CollectionsEmptyMapSerializer());
        kryo.register(Collections.EMPTY_SET.getClass(), new CollectionsEmptySetSerializer());

        /*
         * Can't directly access Collections classes (private class),
         * so instantiate one and do a getClass().
         */
        val singletonSet = Collections.singleton("key");
        kryo.register(singletonSet.getClass());

        val singletonMap = Collections.singletonMap("key", "value");
        kryo.register(singletonMap.getClass());

        val singletonList = Collections.singletonList("key");
        kryo.register(singletonList.getClass());

        val list = Arrays.asList("key");
        kryo.register(list.getClass(), new ArraysAsListSerializer());

        kryo.register(String.CASE_INSENSITIVE_ORDER.getClass());
    }

    private static void registerNativeJdkComponentsWithKryo(final Kryo kryo) {
        kryo.register(GeneralSecurityException.class, new ThrowableSerializer());
        kryo.register(AccountNotFoundException.class, new ThrowableSerializer());

        kryo.register(Class.class, new DefaultSerializers.ClassSerializer());
        kryo.register(ArrayList.class);
        kryo.register(LinkedList.class);
        kryo.register(HashMap.class);
        kryo.register(LinkedHashMap.class);
        kryo.register(LinkedHashSet.class);
        kryo.register(TreeMap.class);
        kryo.register(TreeSet.class);
        kryo.register(HashSet.class);
        kryo.register(EnumMap.class, new EnumMapSerializer());
        kryo.register(EnumSet.class, new EnumSetSerializer());

        kryo.register(Object[].class);
        kryo.register(String[].class);
        kryo.register(Long[].class);
        kryo.register(Integer[].class);
        kryo.register(Double[].class);
        kryo.register(double[].class);
        kryo.register(float[].class);
        kryo.register(long[].class);
        kryo.register(int[].class);
        kryo.register(byte[].class);
        kryo.register(ByteBuffer.class);

        kryo.register(URL.class, new URLSerializer());
        kryo.register(URI.class, new URISerializer());
        kryo.register(Pattern.class, new RegexSerializer());
        kryo.register(UUID.class, new UUIDSerializer());

        kryo.register(ZonedDateTime.class, new ZonedDateTimeSerializer());
        kryo.register(Date.class, new DateSerializer(Date.class));
        kryo.register(Calendar.class, new GregorianCalendarSerializer());
        kryo.register(GregorianCalendar.class, new GregorianCalendarSerializer());
        kryo.register(LocalDate.class, new JodaLocalDateSerializer());
        kryo.register(DateTime.class, new JodaDateTimeSerializer());
        kryo.register(LocalDateTime.class, new JodaLocalDateTimeSerializer());
        kryo.register(Clock.systemUTC().getClass());
        kryo.register(ZoneOffset.class);
        kryo.register(EnumSet.class, new EnumSetSerializer());
    }
}
