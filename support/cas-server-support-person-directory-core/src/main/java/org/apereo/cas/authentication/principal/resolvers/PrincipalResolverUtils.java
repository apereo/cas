package org.apereo.cas.authentication.principal.resolvers;

import org.apereo.cas.configuration.model.core.authentication.PersonDirectoryPrincipalResolverProperties;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This is {@link PrincipalResolverUtils}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@UtilityClass
public class PrincipalResolverUtils {
    /**
     * Build active attribute repository ids.
     *
     * @param personDirectory the person directory
     * @return the set
     */
    public static Set<String> buildActiveAttributeRepositoryIds(
        final PersonDirectoryPrincipalResolverProperties... personDirectory) {
        return Arrays.stream(personDirectory)
            .filter(p -> StringUtils.isNotBlank(p.getActiveAttributeRepositoryIds()))
            .map(p -> org.springframework.util.StringUtils.commaDelimitedListToSet(p.getActiveAttributeRepositoryIds()))
            .filter(p -> !p.isEmpty())
            .flatMap(Set::stream)
            .collect(Collectors.toSet());
    }
}
