package org.apereo.cas.persondir;

import org.apereo.cas.authentication.principal.RegisteredServicePrincipalAttributesRepository;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceAttributeReleasePolicy;
import org.apereo.cas.services.ServicesManager;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apereo.services.persondir.IPersonAttributeDao;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * This is {@link DefaultAttributeRepositoryResolver}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@RequiredArgsConstructor
public class DefaultAttributeRepositoryResolver implements AttributeRepositoryResolver {
    private final ServicesManager servicesManager;

    @Override
    public Set<String> resolve(final AttributeRepositoryQuery query) {
        val repositoryIds = new HashSet<String>();
        Optional.ofNullable(query.getService())
            .map(servicesManager::findServiceBy)
            .map(RegisteredService::getAttributeReleasePolicy)
            .map(RegisteredServiceAttributeReleasePolicy::getPrincipalAttributesRepository)
            .map(RegisteredServicePrincipalAttributesRepository::getAttributeRepositoryIds)
            .filter(identifiers -> !identifiers.isEmpty())
            .ifPresentOrElse(repositoryIds::addAll, () -> repositoryIds.addAll(query.getActiveRepositoryIds()));
        if (repositoryIds.isEmpty()) {
            repositoryIds.add(IPersonAttributeDao.WILDCARD);
        }
        return repositoryIds;
    }
}
