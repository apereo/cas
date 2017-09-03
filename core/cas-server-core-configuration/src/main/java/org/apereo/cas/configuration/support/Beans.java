package org.apereo.cas.configuration.support;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import groovy.lang.GroovyClassLoader;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apereo.cas.CipherExecutor;
import org.apereo.cas.configuration.model.core.authentication.PrincipalAttributesProperties;
import org.apereo.cas.configuration.model.core.util.EncryptionRandomizedSigningJwtCryptographyProperties;
import org.apereo.cas.configuration.model.support.ConnectionPoolingProperties;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.cipher.DefaultTicketCipherExecutor;
import org.apereo.cas.util.cipher.NoOpCipherExecutor;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.apereo.services.persondir.support.NamedStubPersonAttributeDao;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.scheduling.concurrent.ThreadPoolExecutorFactoryBean;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.regex.Pattern;


/**
 * A re-usable collection of utility methods for object instantiations and configurations used cross various
 * {@code @Bean} creation methods throughout CAS server.
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
public final class Beans {

    private static final Logger LOGGER = LoggerFactory.getLogger(Beans.class);

    protected Beans() {
    }

    /**
     * New thread pool executor factory bean.
     *
     * @param config the config
     * @return the thread pool executor factory bean
     */
    public static ThreadPoolExecutorFactoryBean newThreadPoolExecutorFactoryBean(final ConnectionPoolingProperties config) {
        final ThreadPoolExecutorFactoryBean bean = newThreadPoolExecutorFactoryBean(config.getMaxSize(), config.getMaxSize());
        bean.setCorePoolSize(config.getMinSize());
        return bean;
    }

    /**
     * New thread pool executor factory bean.
     *
     * @param keepAlive the keep alive
     * @param maxSize   the max size
     * @return the thread pool executor factory bean
     */
    public static ThreadPoolExecutorFactoryBean newThreadPoolExecutorFactoryBean(final long keepAlive,
                                                                                 final long maxSize) {
        final ThreadPoolExecutorFactoryBean bean = new ThreadPoolExecutorFactoryBean();
        bean.setMaxPoolSize((int) maxSize);
        bean.setKeepAliveSeconds((int) keepAlive);
        return bean;
    }

    /**
     * New attribute repository person attribute dao.
     *
     * @param p the properties
     * @return the person attribute dao
     */
    public static IPersonAttributeDao newStubAttributeRepository(final PrincipalAttributesProperties p) {
        try {
            final NamedStubPersonAttributeDao dao = new NamedStubPersonAttributeDao();
            final Map<String, List<Object>> pdirMap = new HashMap<>();
            p.getStub().getAttributes().forEach((key, value) -> {
                final String[] vals = org.springframework.util.StringUtils.commaDelimitedListToStringArray(value);
                pdirMap.put(key, CollectionUtils.wrap((Object[]) vals));
            });
            dao.setBackingMap(pdirMap);
            return dao;
        } catch (final Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
    

    /**
     * Transform principal attributes list into map map.
     *
     * @param list the list
     * @return the map
     */
    public static Map<String, Collection<String>> transformPrincipalAttributesListIntoMap(final List<String> list) {
        final Multimap<String, String> map = transformPrincipalAttributesListIntoMultiMap(list);
        return CollectionUtils.wrap(map);
    }
    
    /**
     * Transform principal attributes into map.
     * Items in the list are defined in the syntax of "cn", or "cn:commonName" for virtual renaming and maps.
     *
     * @param list the list
     * @return the map
     */
    public static Multimap<String, String> transformPrincipalAttributesListIntoMultiMap(final List<String> list) {

        final Multimap<String, String> multimap = ArrayListMultimap.create();
        if (list.isEmpty()) {
            LOGGER.debug("No principal attributes are defined");
        } else {
            list.forEach(a -> {
                final String attributeName = a.trim();
                if (attributeName.contains(":")) {
                    final String[] attrCombo = attributeName.split(":");
                    final String name = attrCombo[0].trim();
                    final String value = attrCombo[1].trim();
                    LOGGER.debug("Mapped principal attribute name [{}] to [{}]", name, value);
                    multimap.put(name, value);
                } else {
                    LOGGER.debug("Mapped principal attribute name [{}]", attributeName);
                    multimap.put(attributeName, attributeName);
                }
            });
        }
        return multimap;
    }

    /**
     * Gets credential selection predicate.
     *
     * @param selectionCriteria the selection criteria
     * @return the credential selection predicate
     */
    public static Predicate<org.apereo.cas.authentication.Credential> newCredentialSelectionPredicate(final String selectionCriteria) {
        try {
            if (StringUtils.isBlank(selectionCriteria)) {
                return credential -> true;
            }

            if (selectionCriteria.endsWith(".groovy")) {
                final ResourceLoader loader = new DefaultResourceLoader();
                final Resource resource = loader.getResource(selectionCriteria);
                if (resource != null) {
                    final String script = IOUtils.toString(resource.getInputStream(), StandardCharsets.UTF_8);
                    final GroovyClassLoader classLoader = new GroovyClassLoader(Beans.class.getClassLoader(),
                            new CompilerConfiguration(), true);
                    final Class<Predicate> clz = classLoader.parseClass(script);
                    return clz.newInstance();
                }
            }

            final Class predicateClazz = ClassUtils.getClass(selectionCriteria);
            return (Predicate<org.apereo.cas.authentication.Credential>) predicateClazz.newInstance();
        } catch (final Exception e) {
            final Predicate<String> predicate = Pattern.compile(selectionCriteria).asPredicate();
            return credential -> predicate.test(credential.getId());
        }
    }


    /**
     * New duration. If the provided length is duration,
     * it will be parsed accordingly, or if it's a numeric value
     * it will be pared as a duration assuming it's provided as seconds.
     *
     * @param length the length in seconds.
     * @return the duration
     */
    public static Duration newDuration(final String length) {
        try {
            if (NumberUtils.isCreatable(length)) {
                return Duration.ofSeconds(Long.parseLong(length));
            }
            return Duration.parse(length);
        } catch (final Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * New ticket registry cipher executor cipher executor.
     *
     * @param registry     the registry
     * @param registryName the registry name
     * @return the cipher executor
     */
    public static CipherExecutor newTicketRegistryCipherExecutor(final EncryptionRandomizedSigningJwtCryptographyProperties registry,
                                                                 final String registryName) {
        return newTicketRegistryCipherExecutor(registry, false, registryName);
    }

    /**
     * New ticket registry cipher executor cipher executor.
     *
     * @param registry         the registry
     * @param forceIfBlankKeys the force if blank keys
     * @param registryName     the registry name
     * @return the cipher executor
     */
    public static CipherExecutor newTicketRegistryCipherExecutor(final EncryptionRandomizedSigningJwtCryptographyProperties registry,
                                                                 final boolean forceIfBlankKeys,
                                                                 final String registryName) {
        if (registry.isEnabled() || forceIfBlankKeys) {
            return new DefaultTicketCipherExecutor(
                    registry.getEncryption().getKey(),
                    registry.getSigning().getKey(),
                    registry.getAlg(),
                    registry.getSigning().getKeySize(),
                    registry.getEncryption().getKeySize(),
                    registryName);
        }
        LOGGER.info("Ticket registry encryption/signing is turned off. This MAY NOT be safe in a clustered production environment. "
                + "Consider using other choices to handle encryption, signing and verification of "
                + "ticket registry tickets, and verify the chosen ticket registry does support this behavior.");
        return NoOpCipherExecutor.getInstance();
    }

}
