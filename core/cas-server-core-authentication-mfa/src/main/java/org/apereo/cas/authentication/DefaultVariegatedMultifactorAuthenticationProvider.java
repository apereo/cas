package org.apereo.cas.authentication;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.services.MultifactorAuthenticationProvider;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.VariegatedMultifactorAuthenticationProvider;

import java.util.Collection;
import java.util.HashSet;
import java.util.stream.Collectors;

/**
 * This is {@link DefaultVariegatedMultifactorAuthenticationProvider}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
@NoArgsConstructor
@Getter
public class DefaultVariegatedMultifactorAuthenticationProvider extends AbstractMultifactorAuthenticationProvider implements VariegatedMultifactorAuthenticationProvider {

    private static final long serialVersionUID = 4789727148134156909L;

    private Collection<MultifactorAuthenticationProvider> providers = new HashSet<>();

    /**
     * For variegated providers with multiple configured encapsulated variation ones, use `id` and `order`
     * props from the first one as the top level pieces of data
     * to be correctly used by downstream components e.g. get correct ranking and id
     * If in the future there will be an actual use case for utilizing concrete ids and ranking order for each individual
     * variation of provider within variegated wrapper, then we could refactor this code to introduce more pluggable
     * strategy API for configuring these parts.
     *
     * @param provider the provider
     */
    @Override
    public void addProvider(final MultifactorAuthenticationProvider provider) {
        if (this.providers.isEmpty()) {
            super.setId(provider.getId());
            super.setOrder(provider.getOrder());
        }
        this.providers.add(provider);
    }

    @Override
    public boolean isAvailable(final RegisteredService service) throws AuthenticationException {
        final long count = this.providers.stream().filter(p -> p.isAvailable(service)).count();
        return count == providers.size();
    }

    @Override
    public boolean matches(final String identifier) {
        return findProvider(identifier) != null;
    }

    @Override
    public MultifactorAuthenticationProvider findProvider(final String identifier) {
        return this.providers.stream()
            .filter(p -> p.matches(identifier))
            .findFirst()
            .orElse(null);
    }

    @Override
    public <T extends MultifactorAuthenticationProvider> T findProvider(final String identifier, @NonNull final Class<T> clazz) {

        final MultifactorAuthenticationProvider provider = findProvider(identifier);
        if (provider == null) {
            return null;
        }
        if (!clazz.isAssignableFrom(provider.getClass())) {
            throw new ClassCastException("MultifactorAuthenticationProvider ["
                + provider.getId() + " is of type " + provider.getClass() + " when we were expecting " + clazz);
        }
        return (T) provider;
    }

    @Override
    public String getFriendlyName() {
        return providers.stream().map(MultifactorAuthenticationProvider::getFriendlyName).collect(Collectors.joining("|"));
    }

}
