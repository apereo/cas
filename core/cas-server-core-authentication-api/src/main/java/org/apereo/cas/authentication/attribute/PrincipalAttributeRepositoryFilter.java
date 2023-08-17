package org.apereo.cas.authentication.attribute;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.apereo.services.persondir.IPersonAttributeDaoFilter;
import java.util.Arrays;

/**
 * This is {@link PrincipalAttributeRepositoryFilter}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@RequiredArgsConstructor(staticName = "of", access = AccessLevel.PROTECTED)
@Slf4j
final class PrincipalAttributeRepositoryFilter implements IPersonAttributeDaoFilter {
    private final PrincipalAttributeRepositoryFetcher fetcher;

    @Override
    public boolean choosePersonAttributeDao(final IPersonAttributeDao repository) {
        val activeAttributeRepositoryIdentifiers = fetcher.getActiveAttributeRepositoryIdentifiers();
        if (activeAttributeRepositoryIdentifiers.isEmpty()) {
            return false;
        }
        if (activeAttributeRepositoryIdentifiers.contains(IPersonAttributeDao.WILDCARD)) {
            return true;
        }

        val repoIdsArray = activeAttributeRepositoryIdentifiers.toArray(ArrayUtils.EMPTY_STRING_ARRAY);
        LOGGER.trace("Active attribute repository identifiers [{}] to compare with [{}]",
            activeAttributeRepositoryIdentifiers, repository.getId());
        val result = Arrays.stream(repository.getId()).anyMatch(daoId -> daoId.equalsIgnoreCase(IPersonAttributeDao.WILDCARD)
            || StringUtils.equalsAnyIgnoreCase(daoId, repoIdsArray)
            || StringUtils.equalsAnyIgnoreCase(IPersonAttributeDao.WILDCARD, repoIdsArray));
        LOGGER.debug("Selecting attribute repository [{}]", ArrayUtils.toString(repository.getId()));
        return result;
    }
}
