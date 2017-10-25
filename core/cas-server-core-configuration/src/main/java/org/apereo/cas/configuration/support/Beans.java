package org.apereo.cas.configuration.support;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ThreadPoolExecutorFactoryBean;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


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
                pdirMap.put(key, CollectionUtils.wrap(vals));
            });
            dao.setBackingMap(pdirMap);
            return dao;
        } catch (final Exception e) {
            throw new RuntimeException(e.getMessage(), e);
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

        boolean enabled = registry.isEnabled();
        if (!enabled && (StringUtils.isNotBlank(registry.getEncryption().getKey())) && StringUtils.isNotBlank(registry.getSigning().getKey())) {
            LOGGER.warn("Ticket registry encryption/signing for [{}] is not enabled explicitly in the configuration, yet signing/encryption keys "
                    + "are defined for ticket operations. CAS will proceed to enable the ticket registry encryption/signing functionality. "
                    + "If you intend to turn off this behavior, consider removing/disabling the signing/encryption keys defined in settings", registryName);
            enabled = true;
        }

        if (enabled || forceIfBlankKeys) {
            LOGGER.debug("Ticket registry encryption/signing is enabled for [{}]", registryName);
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
