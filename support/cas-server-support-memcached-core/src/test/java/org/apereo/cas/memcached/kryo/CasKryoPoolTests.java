package org.apereo.cas.memcached.kryo;

import module java.base;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasKryoPoolTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("Memcached")
@EnabledIfListeningOnPort(port = 11211)
class CasKryoPoolTests {
    @Test
    void verifyRunOperation() {
        val pool = new CasKryoPool();
        try (val kryo = pool.borrow()) {
            assertNotNull(kryo);
            pool.free(kryo);
        }
    }

    @Test
    @SuppressWarnings("JdkObsolete")
    void verifyJdkCollectionsWithoutReflection() {
        val pool = new CasKryoPool(List.of(TimeUnit.class), true, true, false, false);
        val emptyEnumMap = new EnumMap<TimeUnit, String>(TimeUnit.class);
        val enumMap = new EnumMap<TimeUnit, String>(TimeUnit.class);
        enumMap.put(TimeUnit.SECONDS, "seconds");
        enumMap.put(TimeUnit.DAYS, "days");
        val calendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        calendar.setTimeInMillis(1_700_000_000_000L);
        val values = List.<Object>of(emptyEnumMap, enumMap,
            Collections.unmodifiableList(new ArrayList<>(List.of("a", "b"))),
            Collections.unmodifiableList(new LinkedList<>(List.of("a", "b"))),
            Collections.unmodifiableSet(new HashSet<>(Set.of("a"))),
            Collections.unmodifiableSortedSet(new TreeSet<>(Set.of("b", "a"))),
            Collections.unmodifiableMap(new HashMap<>(Map.of("a", "b"))),
            Collections.unmodifiableSortedMap(new TreeMap<>(Map.of("a", "b"))),
            Arrays.asList("a", "b"), calendar, Clock.systemUTC());
        try (val kryo = pool.borrow()) {
            for (val value : values) {
                val output = new Output(4096, -1);
                kryo.writeClassAndObject(output, value);
                val result = kryo.readClassAndObject(new Input(output.toBytes()));
                assertEquals(value.getClass(), result.getClass());
                assertEquals(value, result);
            }
            val output = new Output(4096, -1);
            kryo.writeObject(output, emptyEnumMap);
            val emptyResult = kryo.readObject(new Input(output.toBytes()), EnumMap.class);
            emptyResult.put(TimeUnit.HOURS, "hours");
            assertEquals("hours", emptyResult.get(TimeUnit.HOURS));
            assertThrows(ClassCastException.class, () -> emptyResult.put("hours", "hours"));
        }
    }

}
