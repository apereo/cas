package org.apereo.cas.authentication.principal;

import module java.base;
import org.apereo.cas.services.RegisteredServiceAttributeReleasePolicyContext;
import lombok.RequiredArgsConstructor;
import lombok.val;

/**
 * This is {@link ChainingPrincipalAttributesRepository}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@RequiredArgsConstructor
public class ChainingPrincipalAttributesRepository implements RegisteredServicePrincipalAttributesRepository {
    @Serial
    private static final long serialVersionUID = 3132218595095989750L;

    private final List<RegisteredServicePrincipalAttributesRepository> repositories;

    @Override
    public Map<String, List<Object>> getAttributes(final RegisteredServiceAttributeReleasePolicyContext context) {
        val results = new LinkedHashMap<String, List<Object>>();
        repositories.forEach(repo -> results.putAll(repo.getAttributes(context)));
        return results;
    }

    @Override
    public Set<String> getAttributeRepositoryIds() {
        return repositories.stream()
            .map(RegisteredServicePrincipalAttributesRepository::getAttributeRepositoryIds)
            .filter(Objects::nonNull)
            .flatMap(Set::stream)
            .collect(Collectors.toSet());
    }

    @Override
    public void update(final String id, final Map<String, List<Object>> attributes,
                       final RegisteredServiceAttributeReleasePolicyContext context) {
        repositories.forEach(repo -> repo.update(id, attributes, context));
    }
}
