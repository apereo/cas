package org.apereo.cas.authentication;

import org.apereo.cas.services.MultifactorAuthenticationProvider;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.VariegatedMultifactorAuthenticationProvider;
import org.springframework.core.Ordered;
import org.springframework.util.Assert;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.stream.Collectors;

/**
 * This is {@link DefaultVariegatedMultifactorAuthenticationProvider}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class DefaultVariegatedMultifactorAuthenticationProvider extends AbstractMultifactorAuthenticationProvider
        implements VariegatedMultifactorAuthenticationProvider, Serializable {

    private static final long serialVersionUID = 4789727148134156909L;
    
    private Collection<MultifactorAuthenticationProvider> providers = new HashSet<>();

    public DefaultVariegatedMultifactorAuthenticationProvider() {
    }

    public DefaultVariegatedMultifactorAuthenticationProvider(final Collection<MultifactorAuthenticationProvider> providers) {
        this.providers = providers;
    }

    @Override
    public void addProvider(final MultifactorAuthenticationProvider provider) {
        this.providers.add(provider);
    }

    @Override
    public Collection<MultifactorAuthenticationProvider> getProviders() {
        return this.providers;
    }

    @Override
    public boolean isAvailable(final RegisteredService service) throws AuthenticationException {
        final long count = this.providers.stream().filter(p -> p.isAvailable(service)).count();
        return count == providers.size();
    }
    
    @Override
    protected boolean isAvailable() {
        return true;
    }

    @Override
    public String getId() {
        return providers.stream().map(MultifactorAuthenticationProvider::getId).collect(Collectors.joining("|"));
    }

    @Override
    public boolean matches(final String identifier) {
        return findProvider(identifier) != null;
    }
    
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    @Override
    public MultifactorAuthenticationProvider findProvider(final String identifier) {
        return this.providers.stream().filter(p -> p.matches(identifier)).findFirst().orElse(null);
    }

    @Override
    public <T extends MultifactorAuthenticationProvider> T findProvider(final String identifier, final Class<T> clazz) {
        Assert.notNull(clazz, "clazz cannot be null");

        final MultifactorAuthenticationProvider provider = findProvider(identifier);

        if (provider == null) {
            return null;
        }

        if (!clazz.isAssignableFrom(provider.getClass())) {
            throw new ClassCastException("MultifactorAuthenticationProvider [" + provider.getId()
                    + " is of type " + provider.getClass()
                    + " when we were expecting " + clazz);
        }

        return (T) provider;
    }

    @Override
    public String getFriendlyName() {
        return providers.stream().map(MultifactorAuthenticationProvider::getFriendlyName).collect(Collectors.joining("|"));
    }
}
