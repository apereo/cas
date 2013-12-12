/*
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.cas.ticket.registry.support.kryo;

import java.net.URL;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.SerializationException;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.serialize.ClassSerializer;
import com.esotericsoftware.kryo.serialize.DateSerializer;
import net.spy.memcached.CachedData;
import net.spy.memcached.transcoders.Transcoder;
import org.jasig.cas.authentication.BasicCredentialMetaData;
import org.jasig.cas.authentication.HandlerResult;
import org.jasig.cas.authentication.ImmutableAuthentication;
import org.jasig.cas.authentication.principal.SimpleWebApplicationServiceImpl;
import org.jasig.cas.ticket.ServiceTicketImpl;
import org.jasig.cas.ticket.TicketGrantingTicketImpl;
import org.jasig.cas.ticket.registry.support.kryo.serial.SimpleWebApplicationServiceSerializer;
import org.jasig.cas.ticket.registry.support.kryo.serial.URLSerializer;
import org.jasig.cas.ticket.support.HardTimeoutExpirationPolicy;
import org.jasig.cas.ticket.support.MultiTimeUseOrTimeoutExpirationPolicy;
import org.jasig.cas.ticket.support.NeverExpiresExpirationPolicy;
import org.jasig.cas.ticket.support.RememberMeDelegatingExpirationPolicy;
import org.jasig.cas.ticket.support.ThrottledUseAndTimeoutExpirationPolicy;
import org.jasig.cas.ticket.support.TicketGrantingTicketExpirationPolicy;
import org.jasig.cas.ticket.support.TimeoutExpirationPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link net.spy.memcached.MemcachedClient} transcoder implementation based on Kryo fast serialization framework
 * suited for efficient serialization of tickets.
 *
 * @author Marvin S. Addison
 */
public class KryoTranscoder implements Transcoder<Object> {

    /** Kryo serializer. */
    private final Kryo kryo = new Kryo();

    /** Logging instance. */
    private final Logger logger = LoggerFactory.getLogger(getClass());

    /** Maximum size of single encoded object in bytes. */
    private final int bufferSize;

    /** Map of class to serializer that handles it. */
    private Map<Class<?>, Serializer> serializerMap;


    /**
     * Creates a Kryo-based transcoder.
     *
     * @param initialBufferSize Initial size for buffer holding encoded object data.
     */
    public KryoTranscoder(final int initialBufferSize) {
        bufferSize = initialBufferSize;
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
        kryo.register(BasicCredentialMetaData.class);
        kryo.register(Class.class, new ClassSerializer(kryo));
        kryo.register(Date.class, new DateSerializer());
        kryo.register(HardTimeoutExpirationPolicy.class);
        kryo.register(HashMap.class);
        kryo.register(HandlerResult.class);
        kryo.register(ImmutableAuthentication.class);
        kryo.register(MultiTimeUseOrTimeoutExpirationPolicy.class);
        kryo.register(NeverExpiresExpirationPolicy.class);
        kryo.register(RememberMeDelegatingExpirationPolicy.class);
        kryo.register(ServiceTicketImpl.class);
        kryo.register(SimpleWebApplicationServiceImpl.class, new SimpleWebApplicationServiceSerializer(kryo));
        kryo.register(ThrottledUseAndTimeoutExpirationPolicy.class);
        kryo.register(TicketGrantingTicketExpirationPolicy.class);
        kryo.register(TicketGrantingTicketImpl.class);
        kryo.register(TimeoutExpirationPolicy.class);
        kryo.register(URL.class, new URLSerializer(kryo));

        // Register other types
        if (serializerMap != null) {
            for (final Class<?> clazz : serializerMap.keySet()) {
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
    public boolean asyncDecode(final CachedData d) {
        return false;
    }


    public CachedData encode(final Object o) {
        final byte[] bytes = encodeToBytes(o);
        return new CachedData(0, bytes, bytes.length);
    }


    public Object decode(final CachedData d) {
        return kryo.readClassAndObject(ByteBuffer.wrap(d.getData()));
    }


    /**
     * Maximum size of encoded data supported by this transcoder.
     *
     * @return  <code>net.spy.memcached.CachedData#MAX_SIZE</code>.
     */
    public int getMaxSize() {
        return CachedData.MAX_SIZE;
    }


    /**
     * Gets the kryo object that provides encoding and decoding services for this instance.
     *
     * @return Underlying Kryo instance.
     */
    public Kryo getKryo() {
        return kryo;
    }


    /**
     * Encodes the given object using registered Kryo serializers.  Provides explicit buffer overflow protection, but
     * careful buffer sizing should be employed to reduce the need for this facility.
     *
     * @param o Object to encode.
     *
     * @return Encoded bytes.
     */
    private byte[] encodeToBytes(final Object o) {
        int factor = 1;
        byte[] result = null;
        ByteBuffer buffer = Kryo.getContext().getBuffer(bufferSize * factor);
        while (result == null) {
            try {
                kryo.writeClassAndObject(buffer, o);
                result = new byte[buffer.flip().limit()];
                buffer.get(result);
            } catch (final SerializationException e) {
                Throwable rootCause = e;
                while (rootCause.getCause() != null) {
                    rootCause = rootCause.getCause();
                }
                if (rootCause instanceof BufferOverflowException) {
                    buffer = ByteBuffer.allocate(bufferSize * ++factor);
                    logger.warn("Buffer overflow while encoding {}", o);
                } else {
                    throw e;
                }
            }
        }
        return result;
    }
}
