package org.apereo.cas.support.saml.services.idp.metadata.cache.resolver;

import module java.base;
import org.apereo.cas.support.saml.services.idp.metadata.SamlMetadataDocument;

/**
 * This is {@link InMemorySamlRegisteredServiceMetadataManager}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
public class InMemorySamlRegisteredServiceMetadataManager implements SamlRegisteredServiceMetadataManager {
    private final Map<Long, SamlMetadataDocument> store = new LinkedHashMap<>();

    @Override
    public List<SamlMetadataDocument> load() {
        return new ArrayList<>(store.values());
    }

    @Override
    public SamlMetadataDocument store(final SamlMetadataDocument document) {
        document.assignIdIfNecessary();
        store.put(document.getId(), document);
        return document;
    }

    @Override
    public void removeById(final long id) {
        store.remove(id);
    }

    @Override
    public void removeByName(final String name) {
        store.values().removeIf(doc -> doc.getName().equals(name));
    }

    @Override
    public Optional<SamlMetadataDocument> findByName(final String name) {
        return store.values().stream()
            .filter(doc -> doc.getName().equals(name))
            .findFirst();
    }

    @Override
    public Optional<SamlMetadataDocument> findById(final long id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public void removeAll() {
        store.clear();
    }

    @Override
    public String getSourceId() {
        return "memory://";
    }

    @Override
    public String getName() {
        return "InMemoryMetadataManager";
    }
}
