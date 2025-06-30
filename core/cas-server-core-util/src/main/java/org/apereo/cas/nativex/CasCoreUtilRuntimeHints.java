package org.apereo.cas.nativex;

import org.apereo.cas.configuration.support.TriStateBoolean;
import org.apereo.cas.util.CasVersion;
import org.apereo.cas.util.LogMessageSummarizer;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import org.apereo.cas.util.serialization.ComponentSerializationPlanConfigurer;
import org.apereo.cas.util.spring.RestActuatorEndpointFilter;
import org.apereo.cas.util.thread.Cleanable;
import com.fasterxml.jackson.annotation.ObjectIdGenerator;
import lombok.val;
import org.apache.commons.lang3.ClassUtils;
import org.slf4j.LoggerFactory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.TypeReference;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor;
import org.springframework.beans.factory.config.ListFactoryBean;
import org.springframework.beans.factory.config.SetFactoryBean;
import org.springframework.context.annotation.CommonAnnotationBeanPostProcessor;
import org.springframework.context.annotation.ConfigurationClassPostProcessor;
import org.springframework.context.event.DefaultEventListenerFactory;
import org.springframework.context.event.EventListenerMethodProcessor;
import org.springframework.orm.jpa.support.PersistenceAnnotationBeanPostProcessor;
import org.springframework.web.cors.CorsConfigurationSource;
import java.lang.module.Configuration;
import java.lang.module.ResolvedModule;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.sql.ResultSet;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * This is {@link CasCoreUtilRuntimeHints}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public class CasCoreUtilRuntimeHints implements CasRuntimeHintsRegistrar {

    @Override
    public void registerHints(final RuntimeHints hints, final ClassLoader classLoader) {
        hints.resources().registerType(CasVersion.class);

        registerProxyHints(hints, List.of(
            ComponentSerializationPlanConfigurer.class,
            InitializingBean.class,
            Supplier.class,
            Runnable.class,
            Function.class,
            Consumer.class,
            Cleanable.class,
            CorsConfigurationSource.class
        ));

        registerSerializationHints(hints);

        registerReflectionHintForDeclaredMethod(hints, Map.Entry.class, "getKey");
        registerReflectionHintForDeclaredMethod(hints, Map.Entry.class, "getValue");
        registerReflectionHintForDeclaredMethod(hints, Map.class, "isEmpty");

        registerReflectionHintsForMethodsAndFields(hints, List.of(
            BigDecimal.class,
            BigInteger.class,
            Math.class,
            URL.class,
            URI.class,
            SetFactoryBean.class,
            ListFactoryBean.class,
            CasVersion.class,
            Module.class,
            Class.class,
            Arrays.class,
            Collections.class,
            Collection.class,
            List.class,
            Iterator.class,
            Iterable.class,
            Queue.class,
            Set.class,
            Comparator.class,
            Comparable.class,
            ResultSet.class,
            Calendar.class,
            Date.class,
            SortedMap.class,
            SortedSet.class,
            TimeZone.class,
            BiPredicate.class,
            BiFunction.class,
            Predicate.class,
            Function.class,
            Consumer.class,
            Supplier.class,
            ModuleLayer.class,
            Configuration.class,
            ResolvedModule.class,
            ServiceLoader.class,
            Callable.class,
            Map.class,
            Locale.class
        ));

        registerReflectionHintsForPublicElements(hints, List.of(System.class));
        
        registerReflectionHintsForDeclaredElements(hints, List.of(
            HashMap.class,
            LinkedHashMap.class,
            TypeReference.of("java.time.Ser")
        ));

        registerReflectionHintsForIntrospectedPublicElements(hints, List.of(
            TypeReference.of("java.util.LinkedHashMap$Entry"),
            TypeReference.of("java.util.TreeMap$Entry")
        ));

        registerReflectionHints(hints, List.of(
            ClassUtils.class,
            LoggerFactory.class
        ));

        registerReflectionHintsForConstructors(hints,
            List.of(
                TriStateBoolean.Deserializer.class,
                PersistenceAnnotationBeanPostProcessor.class,
                ConfigurationClassPostProcessor.class,
                EventListenerMethodProcessor.class,
                DefaultEventListenerFactory.class,
                AutowiredAnnotationBeanPostProcessor.class,
                CommonAnnotationBeanPostProcessor.class,
                RestActuatorEndpointFilter.class
            ));

        registerReflectionHintsForTypes(hints,
            List.of(
                TypeReference.of("java.util.HashMap$Node"),
                TypeReference.of("java.util.HashMap$TreeNode"))
        );

        registerReflectionHintsForTypes(hints, findSubclassesInPackage(Clock.class, Clock.class.getPackageName()));
        registerReflectionHintsForPublicElements(hints, findSubclassesInPackage(ObjectIdGenerator.class, "com.fasterxml.jackson"));
        registerReflectionHintsForPublicElements(hints, findSubclassesInPackage(LogMessageSummarizer.class, "org.apereo.cas"));
        registerReflectionHintsForPublicElements(hints, findSubclassesInPackage(CipherExecutor.class, "org.apereo.cas"));

        registerCaffeineHints(hints);
        FunctionUtils.doAndHandle(__ -> {
            val clazz = ClassUtils.getClass("nonapi.io.github.classgraph.classloaderhandler.ClassLoaderHandler", false);
            registerReflectionHints(hints, findSubclassesInPackage(clazz, "nonapi.io.github.classgraph.classloaderhandler"));
        });
    }
    
    private void registerCaffeineHints(final RuntimeHints hints) {
        FunctionUtils.doAndHandle(__ -> {
            var clazz = ClassUtils.getClass("com.github.benmanes.caffeine.cache.Node", false);
            registerReflectionHintsForConstructors(hints, findSubclassesInPackage(clazz, "com.github.benmanes.caffeine.cache"));
            clazz = ClassUtils.getClass("com.github.benmanes.caffeine.cache.LocalCache", false);
            registerReflectionHintsForConstructors(hints, findSubclassesInPackage(clazz, "com.github.benmanes.caffeine.cache"));
        });
    }

    private void registerSerializationHints(final RuntimeHints hints) {
        registerSerializationHints(hints,
            Boolean.class,
            Double.class,
            Integer.class,
            Long.class,
            String.class,
            Float.class,

            ZonedDateTime.class,
            LocalDateTime.class,
            LocalDate.class,
            LocalTime.class,
            ZoneId.class,
            ZoneOffset.class,
            Instant.class,
            Locale.class,
            
            ArrayList.class,
            Vector.class,
            CopyOnWriteArrayList.class,
            LinkedList.class,

            HashMap.class,
            LinkedHashMap.class,
            ConcurrentHashMap.class,
            TreeMap.class,

            ConcurrentSkipListSet.class,
            ConcurrentLinkedQueue.class,
            ConcurrentHashMap.class,
            HashSet.class,
            LinkedHashSet.class,
            CopyOnWriteArraySet.class,
            TreeSet.class,

            TypeReference.of("java.lang.String$CaseInsensitiveComparator"));

        registerSerializationHints(hints, findSubclassesInPackage(Clock.class, Clock.class.getPackageName()));
    }
}
