package org.apereo.cas;

import module java.base;
import org.apereo.cas.configuration.api.MutablePropertySource;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.ArrayUtils;
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
public class AmazonSecretsManagerPropertySource extends EnumerablePropertySource<SecretsManagerClient>
    implements MutablePropertySource<SecretsManagerClient> {
    private final Set<String> propertyNames = new HashSet<>();

    public AmazonSecretsManagerPropertySource(final String context, final SecretsManagerClient secretsManagerClient) {
        super(context, secretsManagerClient);
        refresh();
    }

    @Override
    public MutablePropertySource setProperty(final String name, final Object value) {
        if (propertyNames.contains(name)) {
            getSource().putSecretValue(PutSecretValueRequest.builder()
                .secretId(name)
                .secretString(value.toString())
                .build());
        } else {
            getSource().createSecret(CreateSecretRequest.builder()
                .name(name).secretString(value.toString()).build());
            propertyNames.add(name);
        }
        return this;
    }

    @Override
    public String[] getPropertyNames() {
        return propertyNames.toArray(ArrayUtils.EMPTY_STRING_ARRAY);
    }

    @Override
    public void refresh() {
        val listRequest = ListSecretsRequest.builder().build();
        val listResults = getSource().listSecrets(listRequest);
        val secretList = listResults.secretList();
        if (secretList != null && !secretList.isEmpty()) {
            LOGGER.debug("Fetched [{}] secret(s)", secretList.size());
            propertyNames.clear();
            propertyNames.addAll(
                secretList
                    .stream()
                    .map(SecretListEntry::name)
                    .toList()
            );
        }
    }

    @Override
    public void removeAll() {
        propertyNames.forEach(this::removeProperty);
        propertyNames.clear();
    }

    @Override
    public void removeProperty(final String name) {
        getSource().deleteSecret(DeleteSecretRequest.builder().secretId(name).build());
        propertyNames.remove(name);
    }

    @Override
    public @Nullable Object getProperty(final String name) {
        if (propertyNames.contains(name)) {
            val getRequest = GetSecretValueRequest.builder().secretId(name).build();
            val result = getSource().getSecretValue(getRequest);
            return result != null ? result.secretString() : null;
        }
        return null;
    }
}
