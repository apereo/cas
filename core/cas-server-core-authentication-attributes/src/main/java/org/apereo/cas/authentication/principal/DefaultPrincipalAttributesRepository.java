package org.apereo.cas.authentication.principal;

import org.apereo.cas.authentication.CoreAuthenticationUtils;
import org.apereo.cas.authentication.principal.cache.AbstractPrincipalAttributesRepository;
import org.apereo.cas.services.RegisteredServiceAttributeReleasePolicyContext;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.io.Serial;
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
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@Accessors(chain = true)
public class DefaultPrincipalAttributesRepository extends AbstractPrincipalAttributesRepository {

    @Serial
    private static final long serialVersionUID = -4535358847021241725L;

    @Override
    public Map<String, List<Object>> getAttributes(final RegisteredServiceAttributeReleasePolicyContext context) {
        val mergeStrategy = determineMergingStrategy();
        val principal = context.getPrincipal();
        val principalAttributes = getPrincipalAttributes(principal);

        LOGGER.trace("Operating principal attributes for processing are [{}]", principalAttributes);
        if (areAttributeRepositoryIdsDefined()) {
            val personDirectoryAttributes = retrievePersonAttributesFromAttributeRepository(context);
            LOGGER.debug("Merging current principal attributes with that of the repository via strategy [{}]", mergeStrategy);
            val mergedAttributes = CoreAuthenticationUtils.getAttributeMerger(mergeStrategy)
                .mergeAttributes(principalAttributes, personDirectoryAttributes);
            LOGGER.debug("Merged current principal attributes are [{}]", mergedAttributes);
            return convertAttributesToPrincipalAttributesAndCache(mergedAttributes, context);
        }
        return convertAttributesToPrincipalAttributesAndCache(principalAttributes, context);
    }

}
