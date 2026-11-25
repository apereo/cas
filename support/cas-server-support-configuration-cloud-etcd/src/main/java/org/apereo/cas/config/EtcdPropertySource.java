package org.apereo.cas.config;

import module java.base;
import org.apereo.cas.configuration.api.MutablePropertySource;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.options.GetOption;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.Strings;
import org.jooq.lambda.Unchecked;
import org.jspecify.annotations.Nullable;
import org.springframework.core.env.EnumerablePropertySource;


/**
 * This is {@link EtcdPropertySource}. Its source creates an etcd client for each refresh or change and the client is
 * closed right after; the bootstrap context that locates this property source is closed once a refresh is done, so a
 * client shared with it would be closed while the property source is still in use. Values are kept in memory, so that
 * looking up a setting neither calls etcd each time nor depends on a client staying open.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@EqualsAndHashCode(callSuper = true)
@Slf4j
@SuppressWarnings("NullAway.Init")
public class EtcdPropertySource extends EnumerablePropertySource<Supplier<Client>> implements MutablePropertySource<Supplier<Client>> {
    private Set<EtcdProperty> propertyNames;
    private final List<String> profiles;

    public EtcdPropertySource(final String name, final Supplier<Client> clientFactory, final List<String> profiles) {
        super(name, clientFactory);
        this.profiles = profiles;
        refresh();
    }

    @Override
    @CanIgnoreReturnValue
    public MutablePropertySource setProperty(final String name, final Object value) {
        val profileName = propertyNames
            .stream()
            .filter(property -> property.key().equalsIgnoreCase(name))
            .map(EtcdProperty::source)
            .findFirst()
            .orElse("/cas/config/default");
        val keyPath = "%s/%s".formatted(profileName, name);
        withClient(client -> client.getKVClient().put(ByteSequence.from(keyPath, StandardCharsets.UTF_8),
            ByteSequence.from(value.toString(), StandardCharsets.UTF_8)).join());

        propertyNames.removeIf(property -> property.key().equalsIgnoreCase(name));
        propertyNames.add(new EtcdProperty(profileName, name, value.toString()));

        return this;
    }

    @Override
    public void removeProperty(final String name) {
        withClient(client -> propertyNames.removeIf(property -> {
            if (property.key().equalsIgnoreCase(name)) {
                client.getKVClient().delete(ByteSequence.from(property.fullPath(), StandardCharsets.UTF_8)).join();
                return true;
            }
            return false;
        }));
    }

    @Override
    public void removeAll() {
        withClient(client -> {
            val kvClient = client.getKVClient();
            propertyNames.forEach(property ->
                kvClient.delete(ByteSequence.from(property.fullPath(), StandardCharsets.UTF_8)).join());
        });
        propertyNames.clear();
    }

    @Override
    public String[] getPropertyNames() {
        return propertyNames.stream().map(EtcdProperty::key).toArray(String[]::new);
    }

    @Override
    public @Nullable Object getProperty(final String name) {
        return propertyNames
            .stream()
            .filter(property -> property.key().equalsIgnoreCase(name))
            .findFirst()
            .map(EtcdProperty::value)
            .orElse(null);
    }

    @Override
    public void refresh() {
        val profilesToChoose = new ArrayList<>(this.profiles);
        profilesToChoose.add("default");

        val properties = new LinkedHashSet<EtcdProperty>();
        withClient(client -> profilesToChoose.forEach(Unchecked.consumer(profile -> {
            val profilePath = "/cas/config/%s".formatted(profile);
            val keyPath = ByteSequence.from(profilePath, StandardCharsets.UTF_8);
            val keyValues = client.getKVClient().get(keyPath, GetOption.builder().isPrefix(true).build()).get().getKvs();
            for (val keyValue : keyValues) {
                val key = keyValue.getKey().toString(StandardCharsets.UTF_8);
                val value = keyValue.getValue().toString(StandardCharsets.UTF_8);
                val propertyName = Strings.CI.removeStart(Strings.CI.removeStart(key, profilePath), "/");
                LOGGER.trace("Fetched etcd property [{}] with value [{}] from [{}]", propertyName, value, profilePath);
                properties.add(new EtcdProperty(profilePath, propertyName, value));
            }
        })));
        this.propertyNames = properties;
    }

    /**
     * Runs the operation with a client of its own and closes the client afterwards.
     *
     * @param operation the operation
     */
    private void withClient(final Consumer<Client> operation) {
        try (val client = getSource().get()) {
            operation.accept(client);
        }
    }

    private record EtcdProperty(String source, String key, String value) {

        String fullPath() {
            return Strings.CI.appendIfMissing(source, "/") + key;
        }
    }
}
