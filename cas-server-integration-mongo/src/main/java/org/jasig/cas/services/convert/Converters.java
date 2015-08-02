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
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;

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
    private Converters() {}

    public static class NullConverter<I, O> implements Converter<I, O> {
        protected final Logger logger = LoggerFactory.getLogger(this.getClass());

        @Override
        public O convert(final I i) {
            return null;
        }
    }

    public static class LoggerConverter extends NullConverter<Logger, DBObject> {
    }

    public static class ClassConverter extends NullConverter<Class, DBObject> {
    }

    public static class CommonsLogConveter extends NullConverter<Log, DBObject> {
    }

    public static class PersonAttributesConveter extends NullConverter<IPersonAttributes, DBObject> {
    }

    public static class MutableConfigurationConverter
            extends NullConverter<MutableConfiguration, DBObject> {
    }

    public static class CacheLoaderConverter
            extends NullConverter<CacheLoader, DBObject> {
    }

    public static class CacheWriterConverter
            extends NullConverter<CacheWriter, DBObject> {
    }

    public static class RunnableConverter
            extends NullConverter<Runnable, DBObject> {
    }

    public static class ReferenceQueueConverter
            extends NullConverter<ReferenceQueue, DBObject> {
    }

    public static class ThreadLocalConverter
            extends NullConverter<ThreadLocal, DBObject> {
    }

    public static class CertPathConverter
            extends NullConverter<CertPath, DBObject> {
    }

    public static class CacheManagerConverter
            extends NullConverter<CacheManager, DBObject> {
    }

    public static class CacheConverter
            extends NullConverter<Cache, DBObject> {
    }
}
