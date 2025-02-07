package org.apereo.cas.web.report;

import org.apereo.cas.authentication.principal.attribute.PersonAttributeDao;
import org.apereo.cas.authentication.principal.attribute.PersonAttributes;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.persondir.PersonDirectoryAttributeRepositoryPlan;
import org.apereo.cas.persondir.cache.CachingPersonAttributeDaoImpl;
import org.apereo.cas.web.BaseCasRestActuatorEndpoint;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.endpoint.Access;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * This is {@link CasPersonDirectoryEndpoint}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Endpoint(id = "personDirectory", defaultAccess = Access.NONE)
public class CasPersonDirectoryEndpoint extends BaseCasRestActuatorEndpoint {
    private final ObjectProvider<PersonAttributeDao> cachingAttributeRepository;
    private final ObjectProvider<PersonDirectoryAttributeRepositoryPlan> attributeRepositoryPlan;

    public CasPersonDirectoryEndpoint(
        final CasConfigurationProperties casProperties,
        final ConfigurableApplicationContext applicationContext,
        final ObjectProvider<PersonAttributeDao> cachingAttributeRepository,
        final ObjectProvider<PersonDirectoryAttributeRepositoryPlan> attributeRepositoryPlan) {
        super(casProperties, applicationContext);
        this.cachingAttributeRepository = cachingAttributeRepository;
        this.attributeRepositoryPlan = attributeRepositoryPlan;
    }

    /**
     * Show cached attributes for person.
     *
     * @param username the username
     * @return the person attributes
     */
    @GetMapping(value = "/cache/{username}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Display cached attributes in the attribute repository for user. If attributes are found in the cache, they are returned. "
        + "Otherwise, attribute repositories will be contacted to fetch and cache person attributes again",
        parameters = @Parameter(name = "username", required = true, in = ParameterIn.PATH, description = "The username to look up"))
    public PersonAttributes showCachedAttributesFor(@PathVariable("username") final String username) {
        val cachingRepository = getCachingPersonAttributeDao();
        return cachingRepository.getPerson(username);
    }

    /**
     * Remove cached attributes.
     *
     * @param username the username
     */
    @DeleteMapping(value = "/cache/{username}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Remove cached attributes in the attribute repository for user",
        parameters = @Parameter(name = "username", required = true, in = ParameterIn.PATH, description = "The username to look up"))
    public void removeCachedAttributesFor(@PathVariable("username") final String username) {
        val cachingRepository = getCachingPersonAttributeDao();
        cachingRepository.removeUserAttributes(username);
    }

    /**
     * Show registered attribute repositories.
     *
     * @return the person attributes
     */
    @GetMapping(value = "/repositories", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Display available attribute repositories and their order of execution")
    public List<AttributeRepository> showAttributeRepositories() {
        return attributeRepositoryPlan.getObject()
            .getAttributeRepositories()
            .stream()
            .map(repository -> new AttributeRepository(
                List.of(repository.getId()),
                repository.getOrder(),
                repository.getTags()))
            .toList();
    }

    private CachingPersonAttributeDaoImpl getCachingPersonAttributeDao() {
        val cachingRepository = (CachingPersonAttributeDaoImpl) cachingAttributeRepository.getObject();
        Objects.requireNonNull(cachingRepository, "Unable to locate caching attribute repository from application context");
        return cachingRepository;
    }

    public record AttributeRepository(List<String> id, int order, Map<String, Object> tags) {
    }
}

