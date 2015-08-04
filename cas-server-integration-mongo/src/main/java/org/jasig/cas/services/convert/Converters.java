/*
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
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

package org.jasig.cas.services.convert;

import com.google.common.cache.CacheLoader;
import com.mongodb.DBObject;
import org.apache.commons.logging.Log;
import org.jasig.services.persondir.IPersonAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.converter.Converter;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.integration.CacheWriter;
import java.lang.ref.ReferenceQueue;
import java.security.cert.CertPath;

/**
 * Collection of mongo converters that map objects to
 * DB objects. Able to exclude types.
 * @author Misagh Moayyed
 * @since 4.1
 */
public final class Converters {
    /**
     * Instantiates a new Converters.
     */
    private Converters() {}

    /**
     * The type Null converter.
     * @param <I>  the type parameter
     * @param <O>  the type parameter
     */
    public static class NullConverter<I, O> implements Converter<I, O> {
        /**
         * The Logger.
         */
        protected final Logger logger = LoggerFactory.getLogger(this.getClass());

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
     * The type Mutable configuration converter.
     * @since 4.1
     */
    public static class MutableConfigurationConverter
            extends NullConverter<MutableConfiguration, DBObject> {
    }

    /**
     * The type Cache loader converter.
     * @since 4.1
     */
    public static class CacheLoaderConverter
            extends NullConverter<CacheLoader, DBObject> {
    }

    /**
     * The type Cache writer converter.
     * @since 4.1
     */
    public static class CacheWriterConverter
            extends NullConverter<CacheWriter, DBObject> {
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

    /**
     * The type Cache manager converter.
     * @since 4.1
     */
    public static class CacheManagerConverter
            extends NullConverter<CacheManager, DBObject> {
    }

    /**
     * The type Cache converter.
     * @since 4.1
     */
    public static class CacheConverter
            extends NullConverter<Cache, DBObject> {
    }
}
