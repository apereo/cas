package org.apereo.cas.configuration.support;

import module java.base;
import org.apereo.cas.configuration.api.MutablePropertySource;
import org.apereo.cas.util.crypto.CipherExecutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jspecify.annotations.Nullable;
import org.springframework.cloud.bootstrap.config.BootstrapPropertySource;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;

/**
 * Decrypts the encrypted values of dynamic configuration sources, such as MongoDb, JDBC or REST, which hand their
 * values over as they are stored, unlike configuration files whose values are decrypted while they load.
 * The decrypted values are kept in a property source of their own, ahead of all others, so that they are what
 * CAS binds; a property that a source of higher precedence defines in plain text is left alone. The values are
 * decrypted once the bootstrap property sources are in place, and again whenever the environment changes, before
 * configuration properties are bound again, so a value stored and refreshed at runtime takes effect too.
 * A value that cannot be decrypted is left as it is.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@Slf4j
@RequiredArgsConstructor
public class CasConfigurationDecryptionInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext>, Ordered {
    /**
     * Name of the property source that holds the decrypted values.
     */
    public static final String PROPERTY_SOURCE_NAME = "casConfigurationDecryptedProperties";
    private static final int ORDER_OFFSET = 20;

    private final CipherExecutor<String, String> cipherExecutor;

    private final Map<String, String> decryptedValues = new ConcurrentHashMap<>();

    @Override
    public void initialize(final ConfigurableApplicationContext applicationContext) {
        decrypt(applicationContext.getEnvironment());
        applicationContext.addApplicationListener(new EnvironmentChangeListener(this, applicationContext.getEnvironment()));
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + ORDER_OFFSET;
    }

    /**
     * Decrypt the encrypted values of the dynamic sources of the environment into the property source of decrypted
     * values, in the order of precedence of the sources, and remove that property source when nothing is encrypted.
     *
     * @param environment the environment
     */
    public void decrypt(final ConfigurableEnvironment environment) {
        val sources = environment.getPropertySources();
        val values = new LinkedHashMap<String, Object>();
        val seen = new HashSet<String>();
        sources.stream()
            .filter(source -> !PROPERTY_SOURCE_NAME.equals(source.getName()))
            .filter(EnumerablePropertySource.class::isInstance)
            .map(EnumerablePropertySource.class::cast)
            .forEach(source -> {
                val dynamic = isDynamic(source);
                for (val name : source.getPropertyNames()) {
                    if (seen.add(name) && dynamic) {
                        decryptValue(source.getProperty(name)).ifPresent(value -> values.put(name, value));
                    }
                }
            });
        if (values.isEmpty()) {
            sources.remove(PROPERTY_SOURCE_NAME);
            return;
        }
        val decrypted = new MapPropertySource(PROPERTY_SOURCE_NAME, values);
        if (sources.contains(PROPERTY_SOURCE_NAME)) {
            sources.replace(PROPERTY_SOURCE_NAME, decrypted);
        } else {
            sources.addFirst(decrypted);
        }
        LOGGER.debug("Decrypted [{}] configuration properties from dynamic configuration sources", values.size());
    }

    private Optional<String> decryptValue(final @Nullable Object value) {
        if (value instanceof final String text && CasConfigurationJasyptCipherExecutor.isValueEncrypted(text)) {
            val cached = decryptedValues.get(text);
            if (cached != null) {
                return Optional.of(cached);
            }
            val decrypted = cipherExecutor.decode(text);
            if (decrypted != null) {
                decryptedValues.put(text, decrypted);
            }
            return Optional.ofNullable(decrypted);
        }
        return Optional.empty();
    }

    private static boolean isDynamic(final PropertySource<?> source) {
        return source instanceof MutablePropertySource
            || (source instanceof final BootstrapPropertySource<?> bootstrap && bootstrap.getDelegate() instanceof MutablePropertySource);
    }

    private record EnvironmentChangeListener(CasConfigurationDecryptionInitializer initializer, ConfigurableEnvironment environment)
        implements ApplicationListener<EnvironmentChangeEvent>, Ordered {
        @Override
        public void onApplicationEvent(final EnvironmentChangeEvent event) {
            initializer.decrypt(environment);
        }

        @Override
        public int getOrder() {
            return Ordered.HIGHEST_PRECEDENCE;
        }
    }
}
