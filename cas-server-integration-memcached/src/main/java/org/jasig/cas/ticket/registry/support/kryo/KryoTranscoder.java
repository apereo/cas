/*
  $Id: $

  Copyright (C) 2012 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: $
  Updated: $Date: $
*/
package org.jasig.cas.ticket.registry.support.kryo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.serialize.DateSerializer;
import com.esotericsoftware.kryo.serialize.FieldSerializer;
import net.spy.memcached.CachedData;
import net.spy.memcached.transcoders.Transcoder;
import org.apache.commons.pool.BasePoolableObjectFactory;
import org.apache.commons.pool.impl.StackObjectPool;
import org.jasig.cas.authentication.ImmutableAuthentication;
import org.jasig.cas.authentication.MutableAuthentication;
import org.jasig.cas.authentication.principal.SamlService;
import org.jasig.cas.authentication.principal.SimplePrincipal;
import org.jasig.cas.authentication.principal.SimpleWebApplicationServiceImpl;
import org.jasig.cas.ticket.ServiceTicketImpl;
import org.jasig.cas.ticket.TicketGrantingTicketImpl;
import org.jasig.cas.ticket.registry.support.kryo.serial.*;
import org.jasig.cas.ticket.support.*;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * {@link net.spy.memcached.MemcachedClient} transcoder implementation based on Kryo fast serialization framework
 * suited for efficient serialization of tickets.
 *
 * @author Middleware Services
 * @version $Revision: $
 */
public class KryoTranscoder implements Transcoder<Object> {

    /** Ticket granting ticket type flag. */
    public static int TGT_TYPE = 314159265;

    /** Service ticket type flag. */
    public static int ST_TYPE = 271828183;

    /** Kryo serializer */
    private final Kryo kryo = new Kryo();

    /** Maximum size of single encoded object in bytes. */
    private final int maxSize;

    /** Soft-limit pool to hold buffers that hold encoded data. */
    private final StackObjectPool<ByteBuffer> bufferPool;

    /** Field reflection helper class. */
    private final FieldHelper fieldHelper = new FieldHelper();

    /** Map of class to serializer that handles it. */
    private Map<Class<?>, Serializer> serializerMap;


    /**
     * Creates a new Kryo-based transcoder for serializing/deserializing tickets.
     *
     * @param maxEncodableSize Maximum size of any one encoded object.
     * @param bufferPoolSize Nominal size of pool that holds reusable buffers for storing temporary encoded data.
     */
    public KryoTranscoder(final int maxEncodableSize, final int bufferPoolSize) {
        maxSize = maxEncodableSize;
        bufferPool = new StackObjectPool<ByteBuffer>(new ByteBufferFactory(), bufferPoolSize, bufferPoolSize);
    }


    /**
     * Sets a map of additional types that should be regisetered with Kryo,
     * for example GoogleAccountsService and OpenIdService.
     *
     * @param map Map of class to the serializer instance that handles it.
     */
    public void setSerializerMap(final Map<Class<?>, Serializer> map) {
        this.serializerMap = map;
    }

    public void initialize() {
        // Register types we know about and do not require external configuration
        kryo.register(ArrayList.class);
        kryo.register(Date.class, new DateSerializer());
        kryo.register(HardTimeoutExpirationPolicy.class, new HardTimeoutExpirationPolicySerializer(kryo, fieldHelper));
        kryo.register(HashMap.class);
        kryo.register(ImmutableAuthentication.class, new ImmutableAuthenticationSerializer(kryo, fieldHelper));
        kryo.register(
                MultiTimeUseOrTimeoutExpirationPolicy.class,
                new MultiTimeUseOrTimeoutExpirationPolicySerializer(kryo, fieldHelper));
        kryo.register(MutableAuthentication.class, new MutableAuthenticationSerializer(kryo, fieldHelper));
        kryo.register(
                NeverExpiresExpirationPolicy.class,
                new FieldSerializer(kryo, NeverExpiresExpirationPolicy.class));
        kryo.register(
                RememberMeDelegatingExpirationPolicy.class,
                new FieldSerializer(kryo, RememberMeDelegatingExpirationPolicy.class));
        kryo.register(SamlService.class, new SamlServiceSerializer(kryo, fieldHelper));
        kryo.register(ServiceTicketImpl.class);
        kryo.register(SimplePrincipal.class, new SimplePrincipalSerializer(kryo));
        kryo.register(SimpleWebApplicationServiceImpl.class, new SimpleWebApplicationServiceSerializer(kryo));
        kryo.register(TicketGrantingTicketImpl.class);
        kryo.register(
                ThrottledUseAndTimeoutExpirationPolicy.class,
                new FieldSerializer(kryo, ThrottledUseAndTimeoutExpirationPolicy.class));
        kryo.register(
                TicketGrantingTicketExpirationPolicy.class,
                new FieldSerializer(kryo, TicketGrantingTicketExpirationPolicy.class));
        kryo.register(TimeoutExpirationPolicy.class, new TimeoutExpirationPolicySerializer(kryo, fieldHelper));

        // Register other types
        if (serializerMap != null) {
            for (Class<?> clazz : serializerMap.keySet()) {
                kryo.register(clazz, serializerMap.get(clazz));
            }
        }

        // Catchall for any classes not explicitly registered
        kryo.setRegistrationOptional(true);
    }


    /**
     * Asynchronous decoding is not supported.
     *
     * @param d Data to decode.
     * @return False.
     */
    public boolean asyncDecode(CachedData d) {
        return false;
    }


    /** {@inheritDoc} */
    public CachedData encode(final Object o) {
        // Assume ticket to be encoded is never null
        final ByteBuffer buffer;
        try {
            buffer = bufferPool.borrowObject();
        } catch (Exception e) {
            throw new IllegalStateException("Failed borrowing byte buffer from pool.", e);
        }
        try {
            kryo.writeObjectData(buffer, o);
        } finally {
            try {
                bufferPool.returnObject(buffer);
            } catch (Exception e) {
                throw new IllegalStateException("Failed returning byte buffer to pool.", e);
            }
        }
        final int flag;
        if (o instanceof TicketGrantingTicketImpl) {
            flag = TGT_TYPE;
        } else if (o instanceof ServiceTicketImpl) {
            flag = ST_TYPE;
        } else {
            throw new IllegalArgumentException("Unsupported object " + o);
        }
        return new CachedData(flag, buffer.array(), maxSize);
    }


    /** {@inheritDoc} */
    public Object decode(final CachedData d) {
        final Class<?> clazz;
        if (d.getFlags() == TGT_TYPE) {
            clazz = TicketGrantingTicketImpl.class;
        } else if (d.getFlags() == ST_TYPE) {
            clazz = ServiceTicketImpl.class;
        } else {
            throw new IllegalArgumentException("Unsupported flags " + d.getFlags());
        }
        return kryo.readObjectData(ByteBuffer.wrap(d.getData()), clazz);
    }


    /**
     * Maximum size of encoded data supported by this transcoder.
     *
     * @return  Maximum size specified at creation time.
     */
    public int getMaxSize() {
        return maxSize;
    }


    class ByteBufferFactory extends BasePoolableObjectFactory<ByteBuffer> {

        public ByteBuffer makeObject() throws Exception {
            return ByteBuffer.allocate(maxSize);
        }
       
        @Override
        public void activateObject(final ByteBuffer buffer) {
            buffer.clear();
        }
    }
}
