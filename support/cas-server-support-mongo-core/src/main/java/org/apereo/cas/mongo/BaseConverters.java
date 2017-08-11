package org.apereo.cas.mongo;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.mongodb.DBObject;
import org.apache.commons.logging.Log;
import org.apereo.services.persondir.IPersonAttributes;
import org.slf4j.Logger;
import org.springframework.core.convert.converter.Converter;

import java.lang.ref.ReferenceQueue;
import java.security.cert.CertPath;

/**
 * Collection of mongo converters that map objects to
 * DB objects. Able to exclude types.
 * @author Misagh Moayyed
 * @since 4.1
 */
public abstract class BaseConverters {
    /**
     * Instantiates a new BaseConverters.
     */
    private BaseConverters() {}

    /**
     * The type Null converter.
     * @param <I>  the type parameter
     * @param <O>  the type parameter
     */
    public static class NullConverter<I, O> implements Converter<I, O> {
        @Override
        public O convert(final I i) {
            return null;
        }
    }

    /**
     * The type Logger converter.
     */
    public static class LoggerConverter extends NullConverter<Logger, DBObject> {
    }

    /**
     * The type Class converter.
     * @since 4.1
     */
    public static class ClassConverter extends NullConverter<Class, DBObject> {
    }

    /**
     * The type Commons log converter.
     * @since 4.1
     */
    public static class CommonsLogConverter extends NullConverter<Log, DBObject> {
    }

    /**
     * The type Person attributes converter.
     * @since 4.1
     */
    public static class PersonAttributesConverter extends NullConverter<IPersonAttributes, DBObject> {
    }

    /**
     * The type Cache loader converter.
     * @since 4.1
     */
    public static class CacheLoaderConverter
            extends NullConverter<CacheLoader, DBObject> {
    }

    /**
     * The type Cache converter.
     * @since 4.1
     */
    public static class CacheConverter
            extends NullConverter<Cache, DBObject> {
    }

    /**
     * The type Caffein cache converter.
     */
    public static class CaffeinCacheConverter extends NullConverter<com.github.benmanes.caffeine.cache.Cache, DBObject> {}

    /**
     * The type Caffein cache loader converter.
     */
    public static class CaffeinCacheLoaderConverter extends NullConverter<com.github.benmanes.caffeine.cache.CacheLoader, DBObject> {}
    
    /**
     * The type Cache builder converter.
     * @since 4.1
     */
    public static class CacheBuilderConverter
            extends NullConverter<CacheBuilder, DBObject> {
    }


    /**
     * The type Runnable converter.
     * @since 4.1
     */
    public static class RunnableConverter
            extends NullConverter<Runnable, DBObject> {
    }

    /**
     * The type Reference queue converter.
     * @since 4.1
     */
    public static class ReferenceQueueConverter
            extends NullConverter<ReferenceQueue, DBObject> {
    }

    /**
     * The type Thread local converter.
     * @since 4.1
     */
    public static class ThreadLocalConverter
            extends NullConverter<ThreadLocal, DBObject> {
    }

    /**
     * The type Cert path converter.
     * @since 4.1
     */
    public static class CertPathConverter
            extends NullConverter<CertPath, DBObject> {
    }
    
    
    
}
