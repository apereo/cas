package org.apereo.cas.persondir;

import org.apereo.cas.authentication.attribute.SimpleUsernameAttributeProvider;
import org.apereo.cas.authentication.principal.attribute.PersonAttributeDao;
import org.apereo.cas.configuration.model.core.authentication.AttributeRepositoryStates;
import org.apereo.cas.configuration.model.core.authentication.RestPrincipalAttributesProperties;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.function.FunctionUtils;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import java.util.List;
import java.util.Objects;

/**
 * This is {@link PersonDirectoryUtils}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
@UtilityClass
@Slf4j
public class PersonDirectoryUtils {
    /**
     * New rest attribute repository list.
     *
     * @param properties the properties
     * @return the list
     */
    public static List<? extends PersonAttributeDao> newRestAttributeRepository(final List<RestPrincipalAttributesProperties> properties) {
        return properties
            .stream()
            .filter(rest -> StringUtils.hasText(rest.getUrl()))
            .map(rest -> {
                val dao = new RestfulPersonAttributeDao();
                dao.setOrder(rest.getOrder());
                FunctionUtils.doIfNotNull(rest.getId(), id -> dao.setId(id));
                dao.setUrl(rest.getUrl());
                dao.setMethod(Objects.requireNonNull(HttpMethod.valueOf(rest.getMethod())).name());
                dao.setEnabled(rest.getState() != AttributeRepositoryStates.DISABLED);

                val headers = CollectionUtils.<String, String>wrap(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
                headers.putAll(rest.getHeaders());
                dao.setHeaders(headers);
                dao.setUsernameAttributeProvider(new SimpleUsernameAttributeProvider(rest.getUsernameAttribute()));
                dao.putTag(PersonDirectoryAttributeRepositoryPlanConfigurer.class.getSimpleName(),
                    rest.getState() == AttributeRepositoryStates.ACTIVE);

                if (StringUtils.hasText(rest.getBasicAuthPassword()) && StringUtils.hasText(rest.getBasicAuthUsername())) {
                    dao.setBasicAuthPassword(rest.getBasicAuthPassword());
                    dao.setBasicAuthUsername(rest.getBasicAuthUsername());
                    LOGGER.debug("Basic authentication credentials are located for REST endpoint [{}]", rest.getUrl());
                } else {
                    LOGGER.debug("Basic authentication credentials are not defined for REST endpoint [{}]", rest.getUrl());
                }
                return dao;
            })
            .toList();
    }
}
