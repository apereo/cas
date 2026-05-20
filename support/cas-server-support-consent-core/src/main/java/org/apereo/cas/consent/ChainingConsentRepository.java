package org.apereo.cas.consent;

import module java.base;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredService;
import lombok.RequiredArgsConstructor;
import org.jooq.lambda.Unchecked;
import org.jspecify.annotations.Nullable;

/**
 * This is {@link ChainingConsentRepository}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@RequiredArgsConstructor
public class ChainingConsentRepository implements ConsentRepository {
    @Serial
    private static final long serialVersionUID = -3921783863834236863L;
    
    private final List<ConsentRepository> repositories;
        
    @Override
    public @Nullable ConsentDecision findConsentDecision(final Service service,
                                                         final RegisteredService registeredService,
                                                         final Authentication authentication) {
        return repositories
            .stream()
            .map(repo -> repo.findConsentDecision(service, registeredService, authentication))
            .filter(Objects::nonNull)
            .findFirst()
            .orElse(null);
    }

    @Override
    public Collection<? extends ConsentDecision> findConsentDecisions(final String principal) {
        return repositories
            .stream()
            .map(repo -> repo.findConsentDecisions(principal))
            .flatMap(Collection::stream)
            .collect(Collectors.toList());
    }

    @Override
    public Collection<? extends ConsentDecision> findConsentDecisions() {
        return repositories
            .stream()
            .map(ConsentRepository::findConsentDecisions)
            .flatMap(Collection::stream)
            .collect(Collectors.toList());
    }

    @Override
    public @Nullable ConsentDecision storeConsentDecision(final ConsentDecision decision) throws Throwable {
        ConsentDecision result = null;
        for (final var repo : repositories) {
            result = repo.storeConsentDecision(decision);
        }
        return result;
    }

    @Override
    public boolean deleteConsentDecision(final long id, final String principal) throws Throwable {
        return repositories
            .stream()
            .anyMatch(Unchecked.predicate(repository -> repository.deleteConsentDecision(id, principal)));
    }

    @Override
    public boolean deleteConsentDecisions(final String principal) throws Throwable {
        return repositories
            .stream()
            .anyMatch(Unchecked.predicate(repository -> repository.deleteConsentDecisions(principal)));
    }

    @Override
    public void deleteAll() throws Throwable {
        repositories.forEach(Unchecked.consumer(ConsentRepository::deleteAll));
    }
}
