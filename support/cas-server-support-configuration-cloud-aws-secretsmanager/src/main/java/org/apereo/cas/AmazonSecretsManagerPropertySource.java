package org.apereo.cas;

import module java.base;
import org.apereo.cas.configuration.api.MutablePropertySource;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;
import org.springframework.core.env.EnumerablePropertySource;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.CreateSecretRequest;
import software.amazon.awssdk.services.secretsmanager.model.DeleteSecretRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.ListSecretsRequest;
import software.amazon.awssdk.services.secretsmanager.model.PutSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.SecretListEntry;

/**
 * This is {@link AmazonSecretsManagerPropertySource}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@EqualsAndHashCode(callSuper = true)
@SuppressWarnings("NullAway.Init")
@Slf4j
public class AmazonSecretsManagerPropertySource extends EnumerablePropertySource<Supplier<SecretsManagerClient>>
    implements MutablePropertySource<Supplier<SecretsManagerClient>> {
    private final Map<String, String> secrets = new ConcurrentHashMap<>();

    public AmazonSecretsManagerPropertySource(final String context, final Supplier<SecretsManagerClient> secretsManagerClientFactory) {
        super(context, secretsManagerClientFactory);
        refresh();
    }

    @Override
    public MutablePropertySource setProperty(final String name, final Object value) {
        withClient(client -> {
            if (secrets.containsKey(name)) {
                client.putSecretValue(PutSecretValueRequest.builder()
                    .secretId(name)
                    .secretString(value.toString())
                    .build());
            } else {
                client.createSecret(CreateSecretRequest.builder()
                    .name(name).secretString(value.toString()).build());
            }
        });
        secrets.put(name, value.toString());
        return this;
    }

    @Override
    public String[] getPropertyNames() {
        return secrets.keySet().toArray(ArrayUtils.EMPTY_STRING_ARRAY);
    }

    /**
     * Reloads all secrets and their values, following every page of the listing so that
     * no secret beyond the first page is left out. Values are kept in memory, so that looking
     * up a setting neither calls Secrets Manager each time nor depends on the client staying open
     * once the bootstrap context that created it is closed after a refresh.
     */
    @Override
    public void refresh() {
        val loaded = new HashMap<String, String>();
        withClient(client -> {
            var nextToken = (String) null;
            do {
                val listRequest = ListSecretsRequest.builder().nextToken(nextToken).build();
                val listResults = client.listSecrets(listRequest);
                val secretList = listResults.secretList();
                if (secretList != null) {
                    secretList.stream()
                        .map(SecretListEntry::name)
                        .forEach(name -> {
                            val value = client.getSecretValue(GetSecretValueRequest.builder().secretId(name).build());
                            if (value != null && value.secretString() != null) {
                                loaded.put(name, value.secretString());
                            }
                        });
                }
                nextToken = listResults.nextToken();
            } while (StringUtils.isNotBlank(nextToken));
        });
        LOGGER.debug("Fetched [{}] secret(s)", loaded.size());
        secrets.clear();
        secrets.putAll(loaded);
    }

    @Override
    public void removeAll() {
        withClient(client -> List.copyOf(secrets.keySet())
            .forEach(name -> client.deleteSecret(DeleteSecretRequest.builder().secretId(name).build())));
        secrets.clear();
    }

    @Override
    public void removeProperty(final String name) {
        withClient(client -> client.deleteSecret(DeleteSecretRequest.builder().secretId(name).build()));
        secrets.remove(name);
    }

    @Override
    public @Nullable Object getProperty(final String name) {
        return secrets.get(name);
    }

    /**
     * Runs the operation with a client of its own and closes the client afterwards. The bootstrap context that locates
     * this property source is closed once a refresh is done, so a client shared with it would be closed while the
     * property source is still in use.
     *
     * @param operation the operation
     */
    private void withClient(final Consumer<SecretsManagerClient> operation) {
        try (val client = getSource().get()) {
            operation.accept(client);
        }
    }
}
