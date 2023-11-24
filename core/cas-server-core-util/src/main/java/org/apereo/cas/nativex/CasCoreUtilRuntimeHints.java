package org.apereo.cas.nativex;

import org.apereo.cas.util.CasVersion;
import org.apereo.cas.util.LogMessageSummarizer;
import org.apereo.cas.util.cipher.JsonWebKeySetStringCipherExecutor;
import org.apereo.cas.util.cipher.RsaKeyPairCipherExecutor;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import org.apereo.cas.util.serialization.ComponentSerializationPlanConfigurer;
import org.apereo.cas.util.thread.Cleanable;
import com.fasterxml.jackson.annotation.ObjectIdGenerator;
import lombok.val;
import org.apache.commons.lang3.ClassUtils;
import org.springframework.aot.hint.MemberCategory;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.AbstractCollection;
import java.util.AbstractMap;
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
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.IntStream;

/**
 * This is {@link CasCoreUtilRuntimeHints}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public class CasCoreUtilRuntimeHints implements CasRuntimeHintsRegistrar {
    private static final int GROOVY_DGM_CLASS_COUNTER = 1500;

    @Override
    public void registerHints(final RuntimeHints hints, final ClassLoader classLoader) {
        hints.resources().registerType(CasVersion.class);

        registerProxiesHints(hints);
        registerSerializationHints(hints);

        registerDeclaredMethod(hints, Map.Entry.class, "getKey");
        registerDeclaredMethod(hints, Map.Entry.class, "getValue");
        registerDeclaredMethod(hints, Map.class, "isEmpty");

        hints.reflection()
            .registerType(Map.Entry.class,
                MemberCategory.INTROSPECT_PUBLIC_METHODS,
                MemberCategory.INTROSPECT_DECLARED_METHODS,
                MemberCategory.INTROSPECT_PUBLIC_METHODS)

            .registerType(TypeReference.of("java.util.LinkedHashMap$Entry"), MemberCategory.INTROSPECT_PUBLIC_METHODS)
            .registerType(TypeReference.of("java.util.TreeMap$Entry"), MemberCategory.INTROSPECT_PUBLIC_METHODS)
            .registerType(LinkedHashMap.class, MemberCategory.INTROSPECT_DECLARED_METHODS, MemberCategory.DECLARED_FIELDS)
            .registerType(TypeReference.of("java.util.HashMap$Node"))
            .registerType(TypeReference.of("java.util.HashMap$TreeNode"))

            .registerType(HashMap.class,
                MemberCategory.INTROSPECT_DECLARED_METHODS,
                MemberCategory.INTROSPECT_DECLARED_CONSTRUCTORS,
                MemberCategory.DECLARED_FIELDS)
            .registerType(AbstractCollection.class,
                MemberCategory.INTROSPECT_DECLARED_METHODS,
                MemberCategory.INTROSPECT_PUBLIC_METHODS,
                MemberCategory.INTROSPECT_DECLARED_CONSTRUCTORS,
                MemberCategory.DECLARED_FIELDS,
                MemberCategory.INVOKE_DECLARED_METHODS)
            .registerType(AbstractMap.class,
                MemberCategory.INTROSPECT_DECLARED_METHODS,
                MemberCategory.INTROSPECT_PUBLIC_METHODS,
                MemberCategory.INTROSPECT_DECLARED_CONSTRUCTORS,
                MemberCategory.DECLARED_FIELDS,
                MemberCategory.INVOKE_DECLARED_METHODS)
            .registerType(Callable.class,
                MemberCategory.INVOKE_DECLARED_METHODS,
                MemberCategory.INVOKE_PUBLIC_METHODS,
                MemberCategory.INTROSPECT_DECLARED_METHODS,
                MemberCategory.INTROSPECT_PUBLIC_METHODS,
                MemberCategory.INTROSPECT_DECLARED_CONSTRUCTORS,
                MemberCategory.DECLARED_FIELDS)
            .registerType(Map.class,
                MemberCategory.INTROSPECT_DECLARED_METHODS,
                MemberCategory.INTROSPECT_PUBLIC_METHODS,
                MemberCategory.INTROSPECT_DECLARED_CONSTRUCTORS,
                MemberCategory.DECLARED_FIELDS)

            .registerType(TypeReference.of("java.time.Ser"),
                MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                MemberCategory.INVOKE_DECLARED_METHODS)

            .registerType(TypeReference.of("java.time.Clock$SystemClock"),
                MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,
                MemberCategory.INVOKE_DECLARED_METHODS,
                MemberCategory.INTROSPECT_PUBLIC_CONSTRUCTORS,
                MemberCategory.INTROSPECT_DECLARED_CONSTRUCTORS);

        registerReflectionHintForMethods(hints,
            List.of(
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
                ServiceLoader.class
            ));

        registerReflectionHintForPublicOps(hints, List.of(
            RsaKeyPairCipherExecutor.class,
            JsonWebKeySetStringCipherExecutor.class,
            System.class));

        registerReflectionHintForConstructors(hints,
            List.of(
                PersistenceAnnotationBeanPostProcessor.class,
                ConfigurationClassPostProcessor.class,
                EventListenerMethodProcessor.class,
                DefaultEventListenerFactory.class,
                AutowiredAnnotationBeanPostProcessor.class,
                CommonAnnotationBeanPostProcessor.class
            ));

        registerReflectionHintForPublicOps(hints,
            findSubclassesInPackage(ObjectIdGenerator.class, "com.fasterxml.jackson"));

        registerReflectionHintForPublicOps(hints,
            findSubclassesInPackage(LogMessageSummarizer.class, "org.apereo.cas"));

        registerCaffeineHints(hints);
        registerGroovyDGMClasses(hints, classLoader);

        FunctionUtils.doAndHandle(__ -> {
            val clazz = ClassUtils.getClass("nonapi.io.github.classgraph.classloaderhandler.ClassLoaderHandler", false);
            registerReflectionHintForAll(hints,
                findSubclassesInPackage(clazz, "nonapi.io.github.classgraph.classloaderhandler"));
        });

        registerReflectionHintForAll(hints,
            List.of(
                "org.codehaus.groovy.transform.StaticTypesTransformation",
                "groovy.lang.GroovyClassLoader",
                "groovy.lang.Script",
                "java.util.Stack",
                "org.slf4j.LoggerFactory"));
    }

    private static void registerGroovyDGMClasses(final RuntimeHints hints, final ClassLoader classLoader) {
        IntStream.range(1, GROOVY_DGM_CLASS_COUNTER).forEach(idx ->
            hints.reflection().registerTypeIfPresent(classLoader, "org.codehaus.groovy.runtime.dgm$" + idx,
                MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,
                MemberCategory.INVOKE_DECLARED_METHODS,
                MemberCategory.INVOKE_PUBLIC_METHODS,
                MemberCategory.DECLARED_FIELDS,
                MemberCategory.PUBLIC_FIELDS));
    }

    private void registerCaffeineHints(final RuntimeHints hints) {
        FunctionUtils.doAndHandle(__ -> {
            var clazz = ClassUtils.getClass("com.github.benmanes.caffeine.cache.Node", false);
            registerReflectionHintForConstructors(hints,
                findSubclassesInPackage(clazz, "com.github.benmanes.caffeine.cache"));
            clazz = ClassUtils.getClass("com.github.benmanes.caffeine.cache.LocalCache", false);
            registerReflectionHintForConstructors(hints,
                findSubclassesInPackage(clazz, "com.github.benmanes.caffeine.cache"));
        });
    }

    private static void registerSerializationHints(final RuntimeHints hints) {
        hints.serialization()
            .registerType(Boolean.class)
            .registerType(Double.class)
            .registerType(Integer.class)
            .registerType(Long.class)
            .registerType(String.class)

            .registerType(ZonedDateTime.class)
            .registerType(LocalDateTime.class)
            .registerType(LocalDate.class)
            .registerType(ZoneId.class)
            .registerType(ZoneOffset.class)

            .registerType(ArrayList.class)
            .registerType(Vector.class)
            .registerType(CopyOnWriteArrayList.class)
            .registerType(LinkedList.class)

            .registerType(HashMap.class)
            .registerType(LinkedHashMap.class)
            .registerType(ConcurrentHashMap.class)
            .registerType(TreeMap.class)

            .registerType(ConcurrentSkipListSet.class)
            .registerType(HashSet.class)
            .registerType(LinkedHashSet.class)
            .registerType(CopyOnWriteArraySet.class)
            .registerType(TreeSet.class)

            .registerType(TypeReference.of("java.time.Clock$SystemClock"))
            .registerType(TypeReference.of("java.time.Clock$OffsetClock"))
            .registerType(TypeReference.of("java.time.Clock$FixedClock"))
            .registerType(TypeReference.of("java.lang.String$CaseInsensitiveComparator"));
    }

    private static void registerProxiesHints(final RuntimeHints hints) {
        hints.proxies()
            .registerJdkProxy(ComponentSerializationPlanConfigurer.class)
            .registerJdkProxy(InitializingBean.class)
            .registerJdkProxy(Supplier.class)
            .registerJdkProxy(Runnable.class)
            .registerJdkProxy(Function.class)
            .registerJdkProxy(Consumer.class)
            .registerJdkProxy(Cleanable.class)
            .registerJdkProxy(CorsConfigurationSource.class);
    }

    private static void registerReflectionHintForConstructors(final RuntimeHints hints, final Collection clazzes) {
        clazzes.forEach(clazz ->
            hints.reflection().registerType((Class) clazz,
                MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,
                MemberCategory.INVOKE_DECLARED_CONSTRUCTORS));
    }

    private static void registerReflectionHintForMethods(final RuntimeHints hints, final Collection clazzes) {
        clazzes.forEach(clazz ->
            hints.reflection().registerType((Class) clazz,
                MemberCategory.PUBLIC_FIELDS,
                MemberCategory.DECLARED_FIELDS,
                MemberCategory.INVOKE_DECLARED_METHODS,
                MemberCategory.INVOKE_PUBLIC_METHODS));
    }

    private static void registerReflectionHintForPublicOps(final RuntimeHints hints, final Collection clazzes) {
        clazzes.forEach(clazz ->
            hints.reflection().registerType((Class) clazz,
                MemberCategory.PUBLIC_FIELDS,
                MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,
                MemberCategory.INVOKE_PUBLIC_METHODS));
    }

    private void registerReflectionHintForAll(final RuntimeHints hints, final Collection clazzes) {
        val memberCategories = new MemberCategory[]{
            MemberCategory.INTROSPECT_DECLARED_METHODS,
            MemberCategory.INTROSPECT_PUBLIC_METHODS,
            MemberCategory.INTROSPECT_DECLARED_CONSTRUCTORS,
            MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
            MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,
            MemberCategory.INVOKE_DECLARED_METHODS,
            MemberCategory.INVOKE_PUBLIC_METHODS,
            MemberCategory.DECLARED_FIELDS,
            MemberCategory.PUBLIC_FIELDS
        };
        clazzes.forEach(entry -> {
            if (entry instanceof final String clazzName) {
                hints.reflection().registerTypeIfPresent(getClass().getClassLoader(), clazzName, memberCategories);
            }
            if (entry instanceof final Class clazz) {
                hints.reflection().registerType(clazz, memberCategories);
            }
        });

    }
}
