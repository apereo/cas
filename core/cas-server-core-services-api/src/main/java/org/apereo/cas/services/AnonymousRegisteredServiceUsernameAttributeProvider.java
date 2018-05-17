package org.apereo.cas.services;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apereo.cas.authentication.principal.PersistentIdGenerator;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.ShibbolethCompatiblePersistentIdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generates a persistent id as username for anonymous service access.
 * By default, the generation is handled by
 * {@link ShibbolethCompatiblePersistentIdGenerator}.
 * Generated ids are unique per service.
 *
 * @author Misagh Moayyed
 * @since 4.1.0
 */
public class AnonymousRegisteredServiceUsernameAttributeProvider extends BaseRegisteredServiceUsernameAttributeProvider {

    private static final long serialVersionUID = 7050462900237284803L;

    private static final Logger LOGGER = LoggerFactory.getLogger(AnonymousRegisteredServiceUsernameAttributeProvider.class);

    /**
     * Encoder to generate PseudoIds.
     */
    private PersistentIdGenerator persistentIdGenerator =
        new ShibbolethCompatiblePersistentIdGenerator(RandomStringUtils.randomAlphanumeric(16));

    /**
     * Init provider.
     */
    public AnonymousRegisteredServiceUsernameAttributeProvider() {
    }

    /**
     * Instantiates a new default registered service username provider.
     *
     * @param persistentIdGenerator the persistent id generator
     */
    public AnonymousRegisteredServiceUsernameAttributeProvider(final PersistentIdGenerator persistentIdGenerator) {
        this.persistentIdGenerator = persistentIdGenerator;
    }

    public PersistentIdGenerator getPersistentIdGenerator() {
        return this.persistentIdGenerator;
    }

    @Override
    protected String resolveUsernameInternal(final Principal principal, final Service service, final RegisteredService registeredService) {
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
        final AnonymousRegisteredServiceUsernameAttributeProvider rhs = (AnonymousRegisteredServiceUsernameAttributeProvider) obj;
        return this.persistentIdGenerator.equals(rhs.persistentIdGenerator);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(13, 113).toHashCode();
    }
}
