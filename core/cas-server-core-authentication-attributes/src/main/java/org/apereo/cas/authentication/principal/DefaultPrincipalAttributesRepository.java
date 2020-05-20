package org.apereo.cas.authentication.principal;

import org.apereo.cas.authentication.principal.cache.AbstractPrincipalAttributesRepository;
import org.apereo.cas.services.RegisteredService;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.List;
import java.util.Map;

/**
 * Default implementation of {@link RegisteredServicePrincipalAttributesRepository}
 * that just returns the attributes as it receives them.
 *
 * @author Misagh Moayyed
 * @since 4.1
 */
@Slf4j
@ToString
@EqualsAndHashCode(callSuper = true)
public class DefaultPrincipalAttributesRepository extends AbstractPrincipalAttributesRepository {

    private static final long serialVersionUID = -4535358847021241725L;

    @Override
    public Map<String, List<Object>> getAttributes(final Principal principal, final RegisteredService registeredService) {
        val mergeStrategy = determineMergingStrategy();
        val principalAttributes = getPrincipalAttributes(principal);

        if (areAttributeRepositoryIdsDefined()) {
            val personDirectoryAttributes = retrievePersonAttributesFromAttributeRepository(principal);
            LOGGER.debug("Merging current principal attributes with that of the repository via strategy [{}]", mergeStrategy);
            val mergedAttributes = mergeStrategy.getAttributeMerger().mergeAttributes(principalAttributes, personDirectoryAttributes);
            LOGGER.debug("Merged current principal attributes are [{}]", mergedAttributes);
            return convertAttributesToPrincipalAttributesAndCache(principal, mergedAttributes, registeredService);
        }
        return convertAttributesToPrincipalAttributesAndCache(principal, principalAttributes, registeredService);
    }

    @Override
    protected void addPrincipalAttributes(final String id, final Map<String, List<Object>> attributes,
                                          final RegisteredService registeredService) {
        LOGGER.debug("Using [{}], no caching takes place for [{}] to add attributes.", id, this.getClass().getSimpleName());
    }
}
