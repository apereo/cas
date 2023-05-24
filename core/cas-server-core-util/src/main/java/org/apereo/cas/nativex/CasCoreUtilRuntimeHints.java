package org.apereo.cas.nativex;

import org.apereo.cas.util.CasVersion;
import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import org.apereo.cas.util.serialization.ComponentSerializationPlanConfigurer;

import lombok.val;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor;
import org.springframework.context.annotation.CommonAnnotationBeanPostProcessor;
import org.springframework.context.annotation.ConfigurationClassPostProcessor;
import org.springframework.context.event.DefaultEventListenerFactory;
import org.springframework.context.event.EventListenerMethodProcessor;
import org.springframework.orm.jpa.support.PersistenceAnnotationBeanPostProcessor;
import org.springframework.web.cors.CorsConfigurationSource;

import java.lang.module.Configuration;
import java.lang.module.ResolvedModule;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
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

        hints.proxies()
            .registerJdkProxy(ComponentSerializationPlanConfigurer.class)
            .registerJdkProxy(InitializingBean.class)
            .registerJdkProxy(CorsConfigurationSource.class);

        hints.serialization()
            .registerType(ArrayList.class)
            .registerType(LinkedList.class)
            .registerType(HashMap.class)
            .registerType(ConcurrentHashMap.class)
            .registerType(TreeMap.class)
            .registerType(HashSet.class)
            .registerType(TreeSet.class)
            .registerType(LinkedHashMap.class);

        hints.reflection()
            .registerType(CasVersion.class, MemberCategory.INVOKE_DECLARED_METHODS)
            .registerType(Map.class, MemberCategory.INVOKE_DECLARED_METHODS)

            .registerType(LinkedList.class, MemberCategory.INVOKE_DECLARED_CONSTRUCTORS, MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS)
            .registerType(ArrayList.class, MemberCategory.INVOKE_DECLARED_CONSTRUCTORS, MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS)
            .registerType(Vector.class, MemberCategory.INVOKE_DECLARED_CONSTRUCTORS, MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS)
            .registerType(HashMap.class, MemberCategory.INVOKE_DECLARED_CONSTRUCTORS, MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS)
            .registerType(HashSet.class, MemberCategory.INVOKE_DECLARED_CONSTRUCTORS, MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS)
            .registerType(TreeSet.class, MemberCategory.INVOKE_DECLARED_CONSTRUCTORS, MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS)
            .registerType(TreeMap.class, MemberCategory.INVOKE_DECLARED_CONSTRUCTORS, MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS)

            .registerType(Module.class, MemberCategory.INVOKE_DECLARED_METHODS)
            .registerType(Class.class, MemberCategory.INVOKE_DECLARED_METHODS)
            .registerType(ModuleLayer.class, MemberCategory.INVOKE_DECLARED_METHODS)
            .registerType(Configuration.class, MemberCategory.INVOKE_DECLARED_METHODS)
            .registerType(ResolvedModule.class, MemberCategory.INVOKE_DECLARED_METHODS)
            .registerType(ServiceLoader.class, MemberCategory.INVOKE_DECLARED_METHODS, MemberCategory.INVOKE_PUBLIC_METHODS)
            .registerType(System.class, MemberCategory.INVOKE_PUBLIC_METHODS, MemberCategory.PUBLIC_FIELDS)

            .registerType(PersistenceAnnotationBeanPostProcessor.class, MemberCategory.INVOKE_DECLARED_CONSTRUCTORS)
            .registerType(ConfigurationClassPostProcessor.class, MemberCategory.INVOKE_DECLARED_CONSTRUCTORS)
            .registerType(EventListenerMethodProcessor.class, MemberCategory.INVOKE_DECLARED_CONSTRUCTORS)
            .registerType(DefaultEventListenerFactory.class, MemberCategory.INVOKE_DECLARED_CONSTRUCTORS)
            .registerType(AutowiredAnnotationBeanPostProcessor.class, MemberCategory.INVOKE_DECLARED_CONSTRUCTORS)
            .registerType(CommonAnnotationBeanPostProcessor.class, MemberCategory.INVOKE_DECLARED_CONSTRUCTORS)

            .registerTypeIfPresent(classLoader, "com.github.benmanes.caffeine.cache.PSW", MemberCategory.INVOKE_DECLARED_CONSTRUCTORS)
            .registerTypeIfPresent(classLoader, "com.github.benmanes.caffeine.cache.PSWMS", MemberCategory.INVOKE_DECLARED_CONSTRUCTORS)
            .registerTypeIfPresent(classLoader, "com.github.benmanes.caffeine.cache.PSAMS", MemberCategory.INVOKE_DECLARED_CONSTRUCTORS)
            .registerTypeIfPresent(classLoader, "com.github.benmanes.caffeine.cache.SSLA", MemberCategory.INVOKE_DECLARED_CONSTRUCTORS)
            .registerTypeIfPresent(classLoader, "com.github.benmanes.caffeine.cache.SSLMSW", MemberCategory.INVOKE_DECLARED_CONSTRUCTORS)
            .registerTypeIfPresent(classLoader, "com.github.benmanes.caffeine.cache.SSLMSA", MemberCategory.INVOKE_DECLARED_CONSTRUCTORS)
            .registerTypeIfPresent(classLoader, "com.github.benmanes.caffeine.cache.SSMSW", MemberCategory.INVOKE_DECLARED_CONSTRUCTORS);


        IntStream.range(1, GROOVY_DGM_CLASS_COUNTER).forEach(idx -> {
            val el = "org.codehaus.groovy.runtime.dgm$" + idx;
            hints.reflection().registerTypeIfPresent(classLoader, el,
                MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,
                MemberCategory.INVOKE_DECLARED_METHODS,
                MemberCategory.INVOKE_PUBLIC_METHODS,
                MemberCategory.DECLARED_FIELDS,
                MemberCategory.PUBLIC_FIELDS);
        });

        List.of(
                "org.slf4j.LoggerFactory",
                "nonapi.io.github.classgraph.classloaderhandler.AntClassLoaderHandler",
                "nonapi.io.github.classgraph.classloaderhandler.ClassGraphClassLoaderHandler",
                "nonapi.io.github.classgraph.classloaderhandler.ClassLoaderHandler",
                "nonapi.io.github.classgraph.classloaderhandler.ClassLoaderHandlerRegistry",
                "nonapi.io.github.classgraph.classloaderhandler.CxfContainerClassLoaderHandler",
                "nonapi.io.github.classgraph.classloaderhandler.EquinoxClassLoaderHandler",
                "nonapi.io.github.classgraph.classloaderhandler.EquinoxContextFinderClassLoaderHandler",
                "nonapi.io.github.classgraph.classloaderhandler.FallbackClassLoaderHandler",
                "nonapi.io.github.classgraph.classloaderhandler.FelixClassLoaderHandler",
                "nonapi.io.github.classgraph.classloaderhandler.JBossClassLoaderHandler",
                "nonapi.io.github.classgraph.classloaderhandler.JPMSClassLoaderHandler",
                "nonapi.io.github.classgraph.classloaderhandler.OSGiDefaultClassLoaderHandler",
                "nonapi.io.github.classgraph.classloaderhandler.ParentLastDelegationOrderTestClassLoaderHandler",
                "nonapi.io.github.classgraph.classloaderhandler.PlexusClassWorldsClassRealmClassLoaderHandler",
                "nonapi.io.github.classgraph.classloaderhandler.QuarkusClassLoaderHandler",
                "nonapi.io.github.classgraph.classloaderhandler.SpringBootRestartClassLoaderHandler",
                "nonapi.io.github.classgraph.classloaderhandler.TomcatWebappClassLoaderBaseHandler",
                "nonapi.io.github.classgraph.classloaderhandler.UnoOneJarClassLoaderHandler",
                "nonapi.io.github.classgraph.classloaderhandler.URLClassLoaderHandler",
                "nonapi.io.github.classgraph.classloaderhandler.WeblogicClassLoaderHandler",
                "nonapi.io.github.classgraph.classloaderhandler.WebsphereLibertyClassLoaderHandler",
                "nonapi.io.github.classgraph.classloaderhandler.WebsphereTraditionalClassLoaderHandler")
            .forEach(clazz -> hints.reflection().registerTypeIfPresent(classLoader, clazz,
                MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,
                MemberCategory.INVOKE_DECLARED_METHODS,
                MemberCategory.INVOKE_PUBLIC_METHODS,
                MemberCategory.DECLARED_FIELDS,
                MemberCategory.PUBLIC_FIELDS));
    }
}
