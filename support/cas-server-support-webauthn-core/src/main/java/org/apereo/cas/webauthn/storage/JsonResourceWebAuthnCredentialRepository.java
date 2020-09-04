package org.apereo.cas.webauthn.storage;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.crypto.CipherExecutor;

import com.fasterxml.jackson.core.type.TypeReference;
import com.yubico.webauthn.data.CredentialRegistration;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

/**
 * This is {@link JsonResourceWebAuthnCredentialRepository}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Slf4j
public class JsonResourceWebAuthnCredentialRepository extends BaseWebAuthnCredentialRepository implements InitializingBean {
    private final Resource location;

    public JsonResourceWebAuthnCredentialRepository(final CasConfigurationProperties properties,
                                                    final Resource location,
                                                    final CipherExecutor<String, String> cipherExecutor) {
        super(properties, cipherExecutor);
        this.location = location;
    }

    @Override
    public void afterPropertiesSet() {
        readFromJsonRepository();
    }

    @Override
    public Collection<CredentialRegistration> getRegistrationsByUsername(final String username) {
        val storage = readFromJsonRepository();
        return storage.containsKey(username)
            ? storage.get(username)
            : new HashSet<>(0);
    }

    @SneakyThrows
    private Map<String, Set<CredentialRegistration>> readFromJsonRepository() {
        LOGGER.trace("Ensuring JSON repository file exists at [{}]", location.getFile());
        val result = location.getFile().createNewFile();
        if (result) {
            LOGGER.trace("Created JSON repository file at [{}]", location.getFile());
        }
        if (location.getFile().length() > 0) {
            LOGGER.trace("Reading JSON repository file at [{}]", location.getFile());
            return new ConcurrentHashMap<>(getObjectMapper().readValue(location.getFile(), new TypeReference<>() {
            }));
        }
        return new ConcurrentHashMap<>(0);
    }

    @Override
    protected Stream<CredentialRegistration> load() {
        return readFromJsonRepository().values().stream().flatMap(Collection::stream);
    }

    @Override
    @SneakyThrows
    protected void update(final String username, final Collection<CredentialRegistration> records) {
        val storage = readFromJsonRepository();
        storage.put(username, new LinkedHashSet<>(records));
        getObjectMapper().writerWithDefaultPrettyPrinter().writeValue(location.getFile(), storage);
    }
}
