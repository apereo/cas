package org.jasig.cas.services;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.jasig.cas.authentication.principal.PersistentIdGenerator;
import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.authentication.principal.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

/**
 * Generates a persistent id as username for anonymous service access.
 * By default, the generation is handled by
 * {@link org.jasig.cas.authentication.principal.ShibbolethCompatiblePersistentIdGenerator}.
 * Generated ids are unique per service.
 * @author Misagh Moayyed
 * @since 4.1.0
 */
public final class AnonymousRegisteredServiceUsernameAttributeProvider implements RegisteredServiceUsernameAttributeProvider {

    private static final long serialVersionUID = 7050462900237284803L;

    private static final Logger LOGGER = LoggerFactory.getLogger(AnonymousRegisteredServiceUsernameAttributeProvider.class);

    /** Encoder to generate PseudoIds. */
    @Nullable
    @Autowired(required = false)
    @Qualifier("persistentIdGenerator")
    private PersistentIdGenerator persistentIdGenerator;

    /** Init provider. */
    public AnonymousRegisteredServiceUsernameAttributeProvider() {}

    /**
     * Instantiates a new default registered service username provider.
     *
     * @param persistentIdGenerator the persistent id generator
     */
    public AnonymousRegisteredServiceUsernameAttributeProvider(@NotNull final PersistentIdGenerator persistentIdGenerator) {
        this.persistentIdGenerator = persistentIdGenerator;
    }

    public PersistentIdGenerator getPersistentIdGenerator() {
        return this.persistentIdGenerator;
    }

    @Override
    public String resolveUsername(final Principal principal, final Service service) {
        if (this.persistentIdGenerator == null) {
            throw new IllegalArgumentException("No persistent id generator is defined");
        }
        final String id = this.persistentIdGenerator.generate(principal, service);
        LOGGER.debug("Resolved username [{}] for anonymous access", id);
        return id;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        final AnonymousRegisteredServiceUsernameAttributeProvider rhs =
                (AnonymousRegisteredServiceUsernameAttributeProvider) obj;
        return this.persistentIdGenerator.equals(rhs.persistentIdGenerator);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(13, 113).toHashCode();
    }
}
