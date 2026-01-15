package org.apereo.cas.services;

import module java.base;
import org.apereo.cas.authentication.principal.PersistentIdGenerator;
import org.apereo.cas.authentication.principal.ShibbolethCompatiblePersistentIdGenerator;
import org.apereo.cas.util.RandomUtils;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
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
@Accessors(chain = true)
@AllArgsConstructor
@Setter
public class AnonymousRegisteredServiceUsernameAttributeProvider extends BaseRegisteredServiceUsernameAttributeProvider {

    @Serial
    private static final long serialVersionUID = 7050462900237284803L;

    /**
     * Encoder to generate PseudoIds.
     */
    private PersistentIdGenerator persistentIdGenerator = new ShibbolethCompatiblePersistentIdGenerator(RandomUtils.randomAlphanumeric(16));

    @Override
    protected String resolveUsernameInternal(final RegisteredServiceUsernameProviderContext context) {
        val id = this.persistentIdGenerator.generate(context.getPrincipal(), context.getService());
        LOGGER.debug("Resolved username [{}] for anonymous access", id);
        return id;
    }
}
