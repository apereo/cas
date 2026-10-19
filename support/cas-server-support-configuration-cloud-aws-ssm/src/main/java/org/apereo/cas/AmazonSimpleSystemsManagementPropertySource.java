package org.apereo.cas;

import module java.base;
import org.apereo.cas.configuration.api.MutablePropertySource;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.jspecify.annotations.Nullable;
import org.springframework.core.env.EnumerablePropertySource;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.GetParametersByPathRequest;
import software.amazon.awssdk.services.ssm.model.ParameterType;
import software.amazon.awssdk.services.ssm.model.PutParameterRequest;

/**
 * This is {@link AmazonSimpleSystemsManagementPropertySource}. Its source creates an SSM client for each refresh or
 * change and the client is closed right after; the bootstrap context that locates this property source is closed once a
 * refresh is done, so a client shared with it would be closed while the property source is still in use.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@EqualsAndHashCode(callSuper = true)
@SuppressWarnings("NullAway.Init")
@Slf4j
public class AmazonSimpleSystemsManagementPropertySource extends EnumerablePropertySource<Supplier<SsmClient>>
    implements MutablePropertySource<Supplier<SsmClient>> {

    private final Map<String, SsmProperty> properties = new LinkedHashMap<>();
    private final List<String> profiles;

    public AmazonSimpleSystemsManagementPropertySource(final String context,
                                                       final Supplier<SsmClient> ssmClientFactory,
                                                       final List<String> profiles) {
        super(context, ssmClientFactory);
        this.profiles = profiles;
        refresh();
    }

    @Override
    public @Nullable Object getProperty(final String name) {
        return properties.containsKey(name) ? properties.get(name).value() : null;
    }

    /**
     * Reloads every parameter under the CAS paths along with its decrypted value. Values are kept in memory,
     * so that looking up a setting neither calls SSM each time nor depends on the client staying open
     * once the bootstrap context that created it is closed after a refresh.
     */
    @Override
    public void refresh() {
        properties.clear();
        withClient(client -> profiles.forEach(profile -> {
            var nextToken = (String) null;
            do {
                val prefix = String.format("/cas/%s", profile);
                val request = GetParametersByPathRequest.builder()
                    .path(prefix)
                    .withDecryption(Boolean.TRUE)
                    .nextToken(nextToken)
                    .build();
                val result = client.getParametersByPath(request);
                nextToken = result.nextToken();
                LOGGER.trace("Fetched [{}] parameters with next token as [{}]", result.parameters().size(), result.nextToken());
                result.parameters().forEach(parameter -> {
                    var propKey = Strings.CI.removeStart(parameter.name(), prefix);
                    propKey = Strings.CI.removeStart(propKey, "/");
                    val key = Strings.CI.removeEnd(propKey, "/");
                    properties.put(key, new SsmProperty(parameter.name(), parameter.arn(), parameter.value()));
                });
            } while (nextToken != null);
        }));
    }

    @Override
    public void removeProperty(final String name) {
        val path = properties.containsKey(name)
            ? properties.get(name).path()
            : newParameterPath(name);
        withClient(client -> client.deleteParameter(builder -> builder.name(path).build()));
        properties.remove(name);
    }

    @Override
    public void removeAll() {
        withClient(client -> {
            val iterator = properties.keySet().iterator();
            while (iterator.hasNext()) {
                val path = Objects.requireNonNull(properties.get(iterator.next())).path();
                client.deleteParameter(builder -> builder.name(path).build());
                iterator.remove();
            }
        });
        properties.clear();
    }

    @Override
    public MutablePropertySource setProperty(final String name, final Object value) {
        val path = properties.containsKey(name)
            ? properties.get(name).path()
            : newParameterPath(name);
        withClient(client -> client.putParameter(
            PutParameterRequest.builder()
                .name(path)
                .type(ParameterType.SECURE_STRING)
                .value(value.toString())
                .overwrite(Boolean.TRUE)
                .build()
        ));
        properties.put(name, new SsmProperty(path, StringUtils.EMPTY, value.toString()));
        return this;
    }

    @Override
    public String[] getPropertyNames() {
        return properties.keySet().toArray(String[]::new);
    }

    /**
     * Builds the path of a new parameter under the last active profile, or directly under {@code /cas/} when no profile
     * is active, so that the path never carries an empty segment.
     *
     * @param name the setting name
     * @return the parameter path
     */
    private String newParameterPath(final String name) {
        val profile = profiles.getLast();
        return StringUtils.isBlank(profile) ? "/cas/" + name : StringUtils.join("/cas/", profile, "/", name);
    }

    /**
     * Runs the operation with a client of its own and closes the client afterwards.
     *
     * @param operation the operation
     */
    private void withClient(final Consumer<SsmClient> operation) {
        try (val client = getSource().get()) {
            operation.accept(client);
        }
    }

    private record SsmProperty(String path, String arn, String value) {
    }
}

