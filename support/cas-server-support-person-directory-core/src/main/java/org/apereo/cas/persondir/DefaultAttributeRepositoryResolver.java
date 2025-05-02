package org.apereo.cas.persondir;

import org.apereo.cas.authentication.attribute.AttributeRepositoryQuery;
import org.apereo.cas.authentication.attribute.AttributeRepositoryResolver;
import org.apereo.cas.authentication.principal.RegisteredServicePrincipalAttributesRepository;
import org.apereo.cas.authentication.principal.attribute.PersonAttributeDao;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.multitenancy.TenantExtractor;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceAttributeReleasePolicy;
import org.apereo.cas.services.ServicesManager;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.util.StringUtils;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * This is {@link DefaultAttributeRepositoryResolver}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@RequiredArgsConstructor
@Getter
public class DefaultAttributeRepositoryResolver implements AttributeRepositoryResolver {
    private final ServicesManager servicesManager;
    private final TenantExtractor tenantExtractor;
    private final CasConfigurationProperties casProperties;

    @Override
    public Set<String> resolve(final AttributeRepositoryQuery query) {
        val repositoryIds = new HashSet<String>();
        determineRegisteredService(query)
            .map(RegisteredService::getAttributeReleasePolicy)
            .map(RegisteredServiceAttributeReleasePolicy::getPrincipalAttributesRepository)
            .map(RegisteredServicePrincipalAttributesRepository::getAttributeRepositoryIds)
            .filter(identifiers -> !identifiers.isEmpty())
            .ifPresentOrElse(repositoryIds::addAll,
                () -> {
                    val selectionMap = casProperties.getPersonDirectory().getAttributeRepositorySelection();
                    if (Objects.nonNull(query.getAuthenticationHandler()) && selectionMap.containsKey(query.getAuthenticationHandler().getName())) {
                        val assignedRepositories = StringUtils.commaDelimitedListToSet(selectionMap.get(query.getAuthenticationHandler().getName()));
                        repositoryIds.addAll(assignedRepositories);
                    } else if (Objects.nonNull(query.getActiveRepositoryIds())) {
                        repositoryIds.addAll(query.getActiveRepositoryIds());
                    }
                });

        if (repositoryIds.isEmpty()) {
            repositoryIds.add(PersonAttributeDao.WILDCARD);
        }
        return repositoryIds;
    }

    protected Optional<RegisteredService> determineRegisteredService(final AttributeRepositoryQuery query) {
        val result = Optional.ofNullable(query.getService()).map(servicesManager::findServiceBy).orElseGet(query::getRegisteredService);
        return Optional.ofNullable(result);
    }
}
