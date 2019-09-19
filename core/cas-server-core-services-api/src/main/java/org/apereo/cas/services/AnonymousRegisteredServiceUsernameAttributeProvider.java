package org.apereo.cas.services;

import org.apereo.cas.authentication.principal.PersistentIdGenerator;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.ShibbolethCompatiblePersistentIdGenerator;
import org.apereo.cas.util.RandomUtils;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

/**
 * Generates a persistent id as username for anonymous service access.
 * By default, the generation is handled by
 * {@link ShibbolethCompatiblePersistentIdGenerator}.
 * Generated ids are unique per service.
 *
 * @author Misagh Moayyed
 * @since 4.1.0
 */
@Slf4j
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Getter
@AllArgsConstructor
@Setter
public class AnonymousRegisteredServiceUsernameAttributeProvider extends BaseRegisteredServiceUsernameAttributeProvider {

    private static final long serialVersionUID = 7050462900237284803L;

    /**
     * Encoder to generate PseudoIds.
     */
    private PersistentIdGenerator persistentIdGenerator = new ShibbolethCompatiblePersistentIdGenerator(RandomUtils.randomAlphanumeric(16));

    @Override
    protected String resolveUsernameInternal(final Principal principal, final Service service, final RegisteredService registeredService) {
        val id = this.persistentIdGenerator.generate(principal, service);
        LOGGER.debug("Resolved username [{}] for anonymous access", id);
        return id;
    }
}
