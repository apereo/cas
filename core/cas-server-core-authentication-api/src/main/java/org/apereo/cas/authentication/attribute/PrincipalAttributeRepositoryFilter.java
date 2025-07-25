package org.apereo.cas.authentication.attribute;

import org.apereo.cas.authentication.principal.attribute.PersonAttributeDao;
import org.apereo.cas.authentication.principal.attribute.PersonAttributeDaoFilter;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.Strings;
import java.util.Arrays;

/**
 * This is {@link PrincipalAttributeRepositoryFilter}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@RequiredArgsConstructor(staticName = "of", access = AccessLevel.PROTECTED)
@Slf4j
final class PrincipalAttributeRepositoryFilter implements PersonAttributeDaoFilter {
    private final PrincipalAttributeRepositoryFetcher fetcher;

    @Override
    public boolean choosePersonAttributeDao(final PersonAttributeDao repository) {
        val activeAttributeRepositoryIdentifiers = fetcher.getActiveAttributeRepositoryIdentifiers();
        if (activeAttributeRepositoryIdentifiers.isEmpty()) {
            return false;
        }
        if (activeAttributeRepositoryIdentifiers.contains(PersonAttributeDao.WILDCARD)) {
            return true;
        }

        val repoIdsArray = activeAttributeRepositoryIdentifiers.toArray(ArrayUtils.EMPTY_STRING_ARRAY);
        LOGGER.trace("Active attribute repository identifiers [{}] to compare with [{}]",
            activeAttributeRepositoryIdentifiers, repository.getId());
        val result = Arrays.stream(repository.getId()).anyMatch(daoId -> daoId.equalsIgnoreCase(PersonAttributeDao.WILDCARD)
            || Strings.CI.equalsAny(daoId, repoIdsArray)
            || Strings.CI.equalsAny(PersonAttributeDao.WILDCARD, repoIdsArray));
        LOGGER.debug("Selecting attribute repository [{}]", ArrayUtils.toString(repository.getId()));
        return result;
    }
}
