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
package org.jasig.cas.util;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.joda.time.Instant;

/**
 * Helper class for common serialization operations. Commonly used with classes that implement
 * {@link java.io.Externalizable}.
 *
 * @author Marvin S. Addison
 * @since 4.0
 */
public final class SerialUtils {

    private static final String ASCII_CHARSET = "US-ASCII";

    private static final Map<Class<?>, Byte> WRITE_TYPE_MAP = new HashMap<Class<?>, Byte>();

    private static final Map<Byte, Class<?>> READ_TYPE_MAP = new HashMap<Byte, Class<?>>();

    static {
        // Register VOID type first so it gets special ID 0
        registerType(Void.TYPE);
        registerType(String.class);
        registerType(Byte.class);
        registerType(Short.class);
        registerType(Integer.class);
        registerType(Long.class);
        registerType(Float.class);
        registerType(Double.class);
        registerType(AtomicInteger.class);
        registerType(AtomicLong.class);
        registerType(BigInteger.class);
        registerType(Date.class);
        registerType(Instant.class);
        registerType(URL.class);
        registerType(URI.class);
        registerType(Collections.emptyList().getClass());
        registerType(Collections.emptyMap().getClass());
        registerType(Collections.emptySet().getClass());
        registerType(Collections.singleton(null).getClass());
        registerType(Collections.singletonList(null).getClass());
        registerType(Collections.singletonMap(null, null).getClass());
        registerType(java.util.ArrayList.class);
        registerType(java.util.HashMap.class);
        registerType(java.util.HashSet.class);
        registerType(java.util.LinkedHashMap.class);
        registerType(java.util.TreeMap.class);
        registerType(java.util.TreeSet.class);
    }

    public static <T> void writeCollection(final Collection<T> collection, final ObjectOutput out) throws IOException {
        if (collection == null) {
            out.writeByte(0);
            return;
        }
        out.writeByte(getTypeId(collection.getClass()));
        final Class<?> collectionType = collection.getClass();
        if (Collections.emptyList().getClass().equals(collectionType) ||
                Collections.emptySet().getClass().equals(collectionType)) {
            return;
        } else if (Collections.singleton(null).getClass().equals(collectionType) ||
                Collections.singletonList(null).getClass().equals(collectionType)) {
            writeObject(collection.iterator().next(), out);
        } else {
            out.writeInt(collection.size());
            for (final T item : collection) {
                writeObject(item, out);
            }
        }
    }

    public static <K, V> void writeMap(final Map<K, V> map, final ObjectOutput out) throws IOException {
        if (map == null) {
            out.writeByte(0);
            return;
        }
        out.writeByte(getTypeId(map.getClass()));
        final Class<?> mapType = map.getClass();
        if (Collections.emptyMap().getClass().equals(mapType)) {
            return;
        } else if (Collections.singletonMap(null, null).getClass().equals(mapType)) {
            final K key = map.keySet().iterator().next();
            writeObject(key, out);
            writeObject(map.get(key), out);
        } else {
            out.writeInt(map.size());
            for (final K key : map.keySet()) {
                writeObject(key, out);
                writeObject(map.get(key), out);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> Collection<T> readCollection(final Class<T> itemType, final ObjectInput in)
            throws IOException, ClassNotFoundException {
        final byte id = in.readByte();
        if (id == 0) {
            return null;
        }
        final Class<?> collectionType = getType(id);
        final Collection<T> collection;
        if (Collections.emptyList().getClass().equals(collectionType)) {
            collection = Collections.emptyList();
        } else if (Collections.emptySet().getClass().equals(collectionType)) {
            collection = Collections.emptySet();
        } else if (Collections.singleton(null).getClass().equals(collectionType)) {
            collection = Collections.singleton(readObject(itemType, in));
        } else if (Collections.singletonList(null).getClass().equals(collectionType)) {
            collection = Collections.singletonList(readObject(itemType, in));
        } else {
            try {
                collection = (Collection<T>) collectionType.newInstance();
            } catch (Exception e) {
                throw new IllegalArgumentException("Cannot instantiate " + collectionType, e);
            }
            final int count = in.readInt();
            for (int i = 0; i < count; i++) {
                collection.add(readObject(itemType, in));
            }
        }
        return collection;
    }

    @SuppressWarnings("unchecked")
    public static <T> List<T> readList(final Class<T> itemType, final ObjectInput in)
            throws ClassNotFoundException, IOException {
        return (List) readCollection(itemType, in);
    }

    @SuppressWarnings("unchecked")
    public static <T> Set<T> readSet(final Class<T> itemType, final ObjectInput in)
            throws ClassNotFoundException, IOException {
        return (Set) readCollection(itemType, in);
    }

    @SuppressWarnings("unchecked")
    public static <K, V> Map<K, V> readMap(final Class<K> keyType, final Class<V> valueType, final ObjectInput in)
            throws IOException, ClassNotFoundException {
        final byte id = in.readByte();
        if (id == 0) {
            return null;
        }

        final Class<?> mapType = getType(id);
        final Map<K, V> map;
        if (Collections.emptyMap().getClass().equals(mapType)) {
            map = Collections.emptyMap();
        } else if (Collections.singletonMap(null, null).getClass().equals(mapType)) {
            map = Collections.singletonMap(readObject(keyType, in), readObject(valueType, in));
        } else {
            try {
                map = (Map<K, V>) mapType.newInstance();
            } catch (Exception e) {
                throw new IllegalArgumentException("Cannot instantiate " + mapType, e);
            }
            final int count = in.readInt();
            for (int i = 0; i < count; i++) {
                map.put(readObject(keyType, in), readObject(valueType, in));
            }
        }
        return map;
    }

    public static void registerCollection(final Class<? extends Collection> clazz) {
        registerType(clazz);
    }

    public static void registerMap(final Class<? extends Map> clazz) {
        registerType(clazz);
    }


    public static void writeObject(final Object item, final ObjectOutput out) throws IOException {
        if (item == null) {
            out.writeByte(0);
            return;
        }
        final Byte id = WRITE_TYPE_MAP.get(item.getClass());
        if (id == null) {
            out.writeByte(-1);
            out.writeObject(item);
            return;
        }
        out.writeByte(id);
        if (item instanceof String) {
            out.writeUTF((String) item);
        } else if (item instanceof Byte) {
            out.writeByte((Byte) item);
        } else if (item instanceof Short) {
            out.writeInt((Short) item);
        } else if (item instanceof Integer) {
            out.writeInt((Integer) item);
        } else if (item instanceof Long) {
            out.writeLong((Long) item);
        } else if (item instanceof Float) {
            out.writeInt(Float.floatToIntBits((Float) item));
        } else if (item instanceof Double) {
            out.writeLong(Double.doubleToLongBits((Double) item));
        } else if (item instanceof AtomicInteger) {
            out.writeInt(((AtomicInteger) item).intValue());
        } else if (item instanceof AtomicLong) {
            out.writeLong(((AtomicLong) item).longValue());
        } else if (item instanceof BigInteger) {
            writeAscii(item.toString(), out);
        } else if (item instanceof Date) {
            out.writeLong(((Date) item).getTime());
        } else if (item instanceof Instant) {
            out.writeLong(((Instant) item).getMillis());
        } else if (item instanceof URI) {
            out.writeUTF(item.toString());
        } else if (item instanceof URL) {
            out.writeUTF(((URL) item).toExternalForm());
        } else {
            throw new IllegalStateException(item + " not supported.");
        }
    }

    public static <T> T readObject(final Class<T> clazz, final ObjectInput in)
            throws IOException, ClassNotFoundException {
        if (clazz == null) {
            throw new IllegalArgumentException("Class to read cannot be null.");
        }
        final Class<T> type = (Class<T>) READ_TYPE_MAP.get(in.readByte());
        if (type == null) {
            return clazz.cast(in.readObject());
        }
        if (Void.TYPE.equals(type)) {
            return null;
        }
        if (!clazz.isAssignableFrom(type)) {
            throw new IllegalArgumentException("Incompatible type found for " + clazz);
        }
        final Object o;
        if (String.class.isAssignableFrom(type)) {
            o = in.readUTF();
        } else if (Byte.class.isAssignableFrom(type)) {
            o = in.readByte();
        } else if (Short.class.isAssignableFrom(type)) {
            o = ((Integer) in.readInt()).shortValue();
        } else if (Integer.class.isAssignableFrom(type)) {
            o = in.readInt();
        } else if (Long.class.isAssignableFrom(type)) {
            o = in.readLong();
        } else if (Float.class.isAssignableFrom(type)) {
            o = Float.intBitsToFloat(in.readInt());
        } else if (Double.class.isAssignableFrom(type)) {
            o = Double.longBitsToDouble(in.readLong());
        } else if (AtomicInteger.class.isAssignableFrom(type)) {
            o = new AtomicInteger(in.readInt());
        } else if (AtomicLong.class.isAssignableFrom(type)) {
            o = new AtomicLong(in.readLong());
        } else if (BigInteger.class.isAssignableFrom(type)) {
            o = new BigInteger(readAscii(in));
        } else if (Date.class.isAssignableFrom(type)) {
            o = new Date(in.readLong());
        } else if (Instant.class.isAssignableFrom(type)) {
            o = new Instant(in.readLong());
        } else if (URI.class.isAssignableFrom(type)) {
            try {
                o = new URI(in.readUTF());
            } catch (URISyntaxException e) {
                throw new IOException("Error reading URI", e);
            }
        } else if (URL.class.isAssignableFrom(type)) {
            o = new URL(in.readUTF());
        } else {
            throw new IllegalStateException(clazz + " not supported.");
        }
        return clazz.cast(o);
    }


    private static void registerType(final Class<?> clazz) {
        final byte nextId = (byte) WRITE_TYPE_MAP.size();
        WRITE_TYPE_MAP.put(clazz, nextId);
        READ_TYPE_MAP.put(nextId, clazz);
    }

    private static byte getTypeId(final Class<?> clazz) {
        final Byte id = WRITE_TYPE_MAP.get(clazz);
        if (id == null) {
            throw new IllegalArgumentException(clazz + " not supported by SerialUtils.");
        }
        return id;
    }

    private static Class<?> getType(final byte typeId) {
        final Class<?> type = READ_TYPE_MAP.get(typeId);
        if (type == null) {
            throw new IllegalArgumentException("Unknown type identifier " + typeId);
        }
        return type;
    }

    private static void writeAscii(final String s, final ObjectOutput out) throws IOException {
        out.writeInt(s.length());
        out.write(s.getBytes(ASCII_CHARSET));
    }

    private static String readAscii(final ObjectInput in) throws IOException {
        final byte[] charBytes = new byte[in.readInt()];
        in.read(charBytes);
        return new String(charBytes, Charset.forName(ASCII_CHARSET));
    }
}
