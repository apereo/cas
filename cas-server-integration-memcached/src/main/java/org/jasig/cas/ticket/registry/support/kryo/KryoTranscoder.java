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

import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.SerializationException;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.serialize.DateSerializer;
import com.esotericsoftware.kryo.serialize.FieldSerializer;
import net.spy.memcached.CachedData;
import net.spy.memcached.transcoders.Transcoder;
import org.jasig.cas.authentication.ImmutableAuthentication;
import org.jasig.cas.authentication.MutableAuthentication;
import org.jasig.cas.authentication.SimplePrincipal;
import org.jasig.cas.authentication.service.SamlService;
import org.jasig.cas.authentication.service.SimpleWebApplicationServiceImpl;
import org.jasig.cas.ticket.ServiceTicketImpl;
import org.jasig.cas.ticket.TicketGrantingTicketImpl;
import org.jasig.cas.ticket.registry.support.kryo.serial.HardTimeoutExpirationPolicySerializer;
import org.jasig.cas.ticket.registry.support.kryo.serial.MultiTimeUseOrTimeoutExpirationPolicySerializer;
import org.jasig.cas.ticket.registry.support.kryo.serial.SamlServiceSerializer;
import org.jasig.cas.ticket.registry.support.kryo.serial.SimpleWebApplicationServiceSerializer;
import org.jasig.cas.ticket.registry.support.kryo.serial.TimeoutExpirationPolicySerializer;
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
 * @version $Revision: $
 */
public class KryoTranscoder implements Transcoder<Object> {

    /** Kryo serializer */
    private final Kryo kryo = new Kryo();

    /** Logging instance. */
    private final Logger logger = LoggerFactory.getLogger(getClass());

    /** Maximum size of single encoded object in bytes. */
    private final int bufferSize;

    /** Field reflection helper class. */
    private final FieldHelper fieldHelper = new FieldHelper();

    /** Map of class to serializer that handles it. */
    private Map<Class<?>, Serializer> serializerMap;


    /**
     * Creates a Kryo-based transcoder.
     *
     * @param initialBufferSize Initial size for buffer holding encoded object data.
     */
    public KryoTranscoder(final int initialBufferSize) {
        this.bufferSize = initialBufferSize;
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
        this.kryo.register(ArrayList.class);
        this.kryo.register(Date.class, new DateSerializer());
        this.kryo.register(HardTimeoutExpirationPolicy.class, new HardTimeoutExpirationPolicySerializer(this.fieldHelper));
        this.kryo.register(HashMap.class);
        this.kryo.register(ImmutableAuthentication.class);
        this.kryo.register(
                MultiTimeUseOrTimeoutExpirationPolicy.class,
                new MultiTimeUseOrTimeoutExpirationPolicySerializer(this.fieldHelper));
        this.kryo.register(MutableAuthentication.class);
        this.kryo.register(
                NeverExpiresExpirationPolicy.class,
                new FieldSerializer(this.kryo, NeverExpiresExpirationPolicy.class));
        this.kryo.register(
                RememberMeDelegatingExpirationPolicy.class,
                new FieldSerializer(this.kryo, RememberMeDelegatingExpirationPolicy.class));
        this.kryo.register(SamlService.class, new SamlServiceSerializer(this.kryo, this.fieldHelper));
        this.kryo.register(ServiceTicketImpl.class);
        this.kryo.register(SimplePrincipal.class);
        this.kryo.register(SimpleWebApplicationServiceImpl.class, new SimpleWebApplicationServiceSerializer(this.kryo));
        this.kryo.register(TicketGrantingTicketImpl.class);
        this.kryo.register(
                ThrottledUseAndTimeoutExpirationPolicy.class,
                new FieldSerializer(this.kryo, ThrottledUseAndTimeoutExpirationPolicy.class));
        this.kryo.register(
                TicketGrantingTicketExpirationPolicy.class,
                new FieldSerializer(this.kryo, TicketGrantingTicketExpirationPolicy.class));
        this.kryo.register(TimeoutExpirationPolicy.class, new TimeoutExpirationPolicySerializer(this.fieldHelper));

        // Register other types
        if (this.serializerMap != null) {
            for (final Class<?> clazz : this.serializerMap.keySet()) {
                this.kryo.register(clazz, this.serializerMap.get(clazz));
            }
        }

        // Catchall for any classes not explicitly registered
        this.kryo.setRegistrationOptional(true);
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
        return this.kryo.readClassAndObject(ByteBuffer.wrap(d.getData()));
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
        return this.kryo;
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
        ByteBuffer buffer = Kryo.getContext().getBuffer(this.bufferSize * factor);
        while (result == null) {
            try {
                this.kryo.writeClassAndObject(buffer, o);
                result = new byte[buffer.flip().limit()];
                buffer.get(result);
            } catch (final SerializationException e) {
                Throwable rootCause = e;
                while (rootCause.getCause() != null) {
                    rootCause = rootCause.getCause();
                }
                if (rootCause instanceof BufferOverflowException) {
                    buffer = ByteBuffer.allocate(this.bufferSize * ++factor);
                    logger.warn("Buffer overflow while encoding " + o);
                } else {
                    throw e;
                }
            }
        }
        return result;
    }
}
