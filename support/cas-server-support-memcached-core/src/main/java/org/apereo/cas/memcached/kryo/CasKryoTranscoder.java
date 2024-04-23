package org.apereo.cas.memcached.kryo;

import com.esotericsoftware.kryo.KryoException;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.spy.memcached.CachedData;
import net.spy.memcached.transcoders.Transcoder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

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
@Slf4j
@RequiredArgsConstructor
public class CasKryoTranscoder implements Transcoder<Object> {
    private final CasKryoPool kryoPool;

    @Override
    public boolean asyncDecode(final CachedData d) {
        return false;
    }

    @Override
    public CachedData encode(final Object obj) {
        try (val kryo = this.kryoPool.borrow();
             val byteStream = new ByteArrayOutputStream();
             val output = new Output(byteStream)) {
            if (obj != null) {
                LOGGER.trace("Writing object [{}] to memcached ", obj.getClass());
            }
            kryo.writeClassAndObject(output, obj);
            output.flush();
            val bytes = byteStream.toByteArray();
            return new CachedData(0, bytes, bytes.length);
        } catch (final Exception exception) {
            throw new KryoException(exception);
        }
    }

    @Override
    public Object decode(final CachedData d) {
        val bytes = d.getData();
        try (val kryo = this.kryoPool.borrow();
             val input = new Input(new ByteArrayInputStream(bytes))) {
            return kryo.readClassAndObject(input);
        } catch (final Exception exception) {
            throw new KryoException(exception);
        }
    }

    @Override
    public int getMaxSize() {
        return CachedData.MAX_SIZE;
    }

    /**
     * Gets kryo.
     *
     * @return the kryo
     */
    public CloseableKryo getKryo() {
        return this.kryoPool.borrow();
    }
}
