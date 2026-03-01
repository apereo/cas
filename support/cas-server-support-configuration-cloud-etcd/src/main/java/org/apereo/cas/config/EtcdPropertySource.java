package org.apereo.cas.config;

import module java.base;
import org.apereo.cas.configuration.api.MutablePropertySource;
import org.apereo.cas.util.function.FunctionUtils;
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
 * This is {@link EtcdPropertySource}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@EqualsAndHashCode(callSuper = true)
@Slf4j
@SuppressWarnings("NullAway.Init")
public class EtcdPropertySource extends EnumerablePropertySource<Client> implements MutablePropertySource<Client> {
    private Set<EtcdProperty> propertyNames;
    private final List<String> profiles;

    public EtcdPropertySource(final String name, final Client source, final List<String> profiles) {
        super(name, source);
        this.profiles = profiles;
        refresh();
    }

    @Override
    public MutablePropertySource setProperty(final String name, final Object value) {
        val profileName = propertyNames
            .stream()
            .filter(property -> property.key().equalsIgnoreCase(name))
            .map(EtcdProperty::source)
            .findFirst()
            .orElse("/cas/config/default");
        val keyPath = "%s/%s".formatted(profileName, name);
        getSource().getKVClient().put(ByteSequence.from(keyPath, StandardCharsets.UTF_8),
            ByteSequence.from(value.toString(), StandardCharsets.UTF_8)).join();

        propertyNames.removeIf(property -> property.key().equalsIgnoreCase(name));
        propertyNames.add(new EtcdProperty(profileName, name, value.toString()));

        return this;
    }

    @Override
    public void removeProperty(final String name) {
        propertyNames.removeIf(property -> {
            if (property.key().equalsIgnoreCase(name)) {
                getSource().getKVClient().delete(ByteSequence.from(property.fullPath(), StandardCharsets.UTF_8)).join();
                return true;
            }
            return false;
        });
    }

    @Override
    public void removeAll() {
        val kvClient = getSource().getKVClient();
        propertyNames.forEach(property ->
            kvClient.delete(ByteSequence.from(property.fullPath(), StandardCharsets.UTF_8)).join());
        propertyNames.clear();
    }

    @Override
    public String[] getPropertyNames() {
        return propertyNames.stream().map(EtcdProperty::key).toArray(String[]::new);
    }

    @Override
    public @Nullable Object getProperty(final String name) {
        return FunctionUtils.doUnchecked(() -> {
            val property = propertyNames.stream().filter(p -> p.key().equalsIgnoreCase(name)).findFirst();
            if (property.isPresent()) {
                val keyPath = ByteSequence.from(Strings.CI.appendIfMissing(property.get().source(), "/") + name, StandardCharsets.UTF_8);
                val keyValues = getSource().getKVClient().get(keyPath).get().getKvs();
                if (!keyValues.isEmpty()) {
                    val keyValue = keyValues.getFirst();
                    return keyValue.getValue().toString(StandardCharsets.UTF_8);
                }
            }
            return null;
        });
    }

    @Override
    public void refresh() {
        val profilesToChoose = new ArrayList<>(this.profiles);
        profilesToChoose.add("default");

        this.propertyNames = new LinkedHashSet<>();
        profilesToChoose.forEach(Unchecked.consumer(profile -> {
            val profilePath = "/cas/config/%s".formatted(profile);
            val keyPath = ByteSequence.from(profilePath, StandardCharsets.UTF_8);
            val keyValues = getSource().getKVClient().get(keyPath, GetOption.builder().isPrefix(true).build()).get().getKvs();
            for (val keyValue : keyValues) {
                val key = keyValue.getKey().toString(StandardCharsets.UTF_8);
                val value = keyValue.getValue().toString(StandardCharsets.UTF_8);
                val propertyName = Strings.CI.removeStart(Strings.CI.removeStart(key, profilePath), "/");
                LOGGER.trace("Fetched etcd property [{}] with value [{}] from [{}]", propertyName, value, profilePath);
                propertyNames.add(new EtcdProperty(profilePath, propertyName, value));
            }
        }));
    }

    private record EtcdProperty(String source, String key, String value) {

        String fullPath() {
            return Strings.CI.appendIfMissing(source, "/") + key;
        }
    }
}
