package org.apereo.cas.authentication.principal;

import org.apereo.cas.services.RegisteredService;

import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This is {@link ChainingPrincipalAttributesRepository}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@RequiredArgsConstructor
public class ChainingPrincipalAttributesRepository implements RegisteredServicePrincipalAttributesRepository {
    private final List<RegisteredServicePrincipalAttributesRepository> repositories;

    @Override
    public Set<String> getAttributeRepositoryIds() {
        return repositories.stream()
            .map(RegisteredServicePrincipalAttributesRepository::getAttributeRepositoryIds)
            .filter(Objects::nonNull)
            .flatMap(Set::stream)
            .collect(Collectors.toSet());
    }

    @Override
    public Map<String, List<Object>> getAttributes(final Principal principal,
                                                   final RegisteredService registeredService) {
        val results = new LinkedHashMap<String, List<Object>>();
        repositories.forEach(repo -> results.putAll(repo.getAttributes(principal, registeredService)));
        return results;
    }

    @Override
    public void update(final String id, final Map<String, List<Object>> attributes,
                       final RegisteredService registeredService) {
        repositories.forEach(repo -> repo.update(id, attributes, registeredService));
    }
}
