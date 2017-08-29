package org.apereo.cas.configuration.support;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import groovy.lang.GroovyClassLoader;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apereo.cas.CipherExecutor;
import org.apereo.cas.authentication.handler.PrincipalNameTransformer;
import org.apereo.cas.configuration.model.core.authentication.PasswordEncoderProperties;
import org.apereo.cas.configuration.model.core.authentication.PrincipalAttributesProperties;
import org.apereo.cas.configuration.model.core.authentication.PrincipalTransformationProperties;
import org.apereo.cas.configuration.model.core.util.EncryptionRandomizedSigningJwtCryptographyProperties;
import org.apereo.cas.configuration.model.support.ConnectionPoolingProperties;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.cipher.DefaultTicketCipherExecutor;
import org.apereo.cas.util.cipher.NoOpCipherExecutor;
import org.apereo.cas.util.crypto.DefaultPasswordEncoder;
import org.apereo.cas.util.transforms.ConvertCasePrincipalNameTransformer;
import org.apereo.cas.util.transforms.PrefixSuffixPrincipalNameTransformer;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.apereo.services.persondir.support.NamedStubPersonAttributeDao;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.scheduling.concurrent.ThreadPoolExecutorFactoryBean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;
import org.springframework.security.crypto.password.StandardPasswordEncoder;
import org.springframework.security.crypto.scrypt.SCryptPasswordEncoder;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
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
     * New thread pool executor factory bean thread pool executor factory bean.
     *
     * @param config the config
     * @return the thread pool executor factory bean
     */
    public static ThreadPoolExecutorFactoryBean newThreadPoolExecutorFactoryBean(final ConnectionPoolingProperties config) {
        final ThreadPoolExecutorFactoryBean bean = new ThreadPoolExecutorFactoryBean();
        bean.setCorePoolSize(config.getMinSize());
        bean.setMaxPoolSize(config.getMaxSize());
        bean.setKeepAliveSeconds((int) config.getMaxWait());
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
     * New password encoder password encoder.
     *
     * @param properties the properties
     * @return the password encoder
     */
    public static PasswordEncoder newPasswordEncoder(final PasswordEncoderProperties properties) {
        final String type = properties.getType();
        if (StringUtils.isBlank(type)) {
            LOGGER.debug("No password encoder type is defined, and so none shall be created");
            return NoOpPasswordEncoder.getInstance();
        }

        if (type.contains(".")) {
            try {
                LOGGER.debug("Configuration indicates use of a custom password encoder [{}]", type);
                final Class<PasswordEncoder> clazz = (Class<PasswordEncoder>) Class.forName(type);
                return clazz.newInstance();
            } catch (final Exception e) {
                LOGGER.error("Falling back to a no-op password encoder as CAS has failed to create "
                        + "an instance of the custom password encoder class " + type, e);
                return NoOpPasswordEncoder.getInstance();
            }
        }

        final PasswordEncoderProperties.PasswordEncoderTypes encoderType = PasswordEncoderProperties.PasswordEncoderTypes.valueOf(type);
        switch (encoderType) {
            case DEFAULT:
                LOGGER.debug("Creating default password encoder with encoding alg [{}] and character encoding [{}]",
                        properties.getEncodingAlgorithm(), properties.getCharacterEncoding());
                return new DefaultPasswordEncoder(properties.getEncodingAlgorithm(), properties.getCharacterEncoding());
            case STANDARD:
                LOGGER.debug("Creating standard password encoder with the secret defined in the configuration");
                return new StandardPasswordEncoder(properties.getSecret());
            case BCRYPT:
                LOGGER.debug("Creating BCRYPT password encoder given the strength [{}] and secret in the configuration",
                        properties.getStrength());
                if (StringUtils.isBlank(properties.getSecret())) {
                    LOGGER.debug("Creating BCRYPT encoder without secret");
                    return new BCryptPasswordEncoder(properties.getStrength());
                }
                LOGGER.debug("Creating BCRYPT encoder with secret");
                return new BCryptPasswordEncoder(properties.getStrength(),
                        new SecureRandom(properties.getSecret().getBytes(StandardCharsets.UTF_8)));
            case SCRYPT:
                LOGGER.debug("Creating SCRYPT encoder");
                return new SCryptPasswordEncoder();
            case PBKDF2:
                if (StringUtils.isBlank(properties.getSecret())) {
                    LOGGER.debug("Creating PBKDF2 encoder without secret");
                    return new Pbkdf2PasswordEncoder();
                }
                final int hashWidth = 256;
                return new Pbkdf2PasswordEncoder(properties.getSecret(), properties.getStrength(), hashWidth);
            case NONE:
            default:
                LOGGER.debug("No password encoder shall be created given the requested encoder type [{}]", type);
                return NoOpPasswordEncoder.getInstance();
        }
    }
    
    /**
     * New principal name transformer.
     *
     * @param p the p
     * @return the principal name transformer
     */
    public static PrincipalNameTransformer newPrincipalNameTransformer(final PrincipalTransformationProperties p) {

        final PrincipalNameTransformer res;
        if (StringUtils.isNotBlank(p.getPrefix()) || StringUtils.isNotBlank(p.getSuffix())) {
            final PrefixSuffixPrincipalNameTransformer t = new PrefixSuffixPrincipalNameTransformer();
            t.setPrefix(p.getPrefix());
            t.setSuffix(p.getSuffix());
            res = t;
        } else {
            res = formUserId -> formUserId;
        }

        switch (p.getCaseConversion()) {
            case UPPERCASE:
                final ConvertCasePrincipalNameTransformer t = new ConvertCasePrincipalNameTransformer(res);
                t.setToUpperCase(true);
                return t;

            case LOWERCASE:
                final ConvertCasePrincipalNameTransformer t1 = new ConvertCasePrincipalNameTransformer(res);
                t1.setToUpperCase(false);
                return t1;
            default:
                //nothing
        }
        return res;
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
