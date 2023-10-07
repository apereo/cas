package org.apereo.cas.web.report;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.BaseCasActuatorEndpoint;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import lombok.val;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.apereo.services.persondir.IPersonAttributes;
import org.apereo.services.persondir.support.CachingPersonAttributeDaoImpl;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.endpoint.web.annotation.RestControllerEndpoint;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.Objects;

/**
 * This is {@link CasPersonDirectoryEndpoint}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@RestControllerEndpoint(id = "personDirectory", enableByDefault = false)
public class CasPersonDirectoryEndpoint extends BaseCasActuatorEndpoint {
    private final ObjectProvider<IPersonAttributeDao> cachingAttributeRepository;

    public CasPersonDirectoryEndpoint(
        final CasConfigurationProperties casProperties,
        final ObjectProvider<IPersonAttributeDao> cachingAttributeRepository) {
        super(casProperties);
        this.cachingAttributeRepository = cachingAttributeRepository;
    }

    /**
     * Show cached attributes for person.
     *
     * @param username the username
     * @return the person attributes
     */
    @GetMapping("/cache/{username}")
    @Operation(summary = "Display cached attributes in the attribute repository for user. If attributes are found in the cache, they are returned. "
        + "Otherwise, attribute repositories will be contacted to fetch and cache person attributes again",
        parameters = @Parameter(name = "username", required = true, in = ParameterIn.PATH))
    public IPersonAttributes showCachedAttributesFor(@PathVariable("username") final String username) {
        val cachingRepository = getCachingPersonAttributeDao();
        return cachingRepository.getPerson(username);
    }

    /**
     * Remove cached attributes.
     *
     * @param username the username
     */
    @DeleteMapping("/cache/{username}")
    @Operation(summary = "Remove cached attributes in the attribute repository for user",
        parameters = @Parameter(name = "username", required = true, in = ParameterIn.PATH))
    public void removeCachedAttributesFor(@PathVariable("username") final String username) {
        val cachingRepository = getCachingPersonAttributeDao();
        cachingRepository.removeUserAttributes(username);
    }

    private CachingPersonAttributeDaoImpl getCachingPersonAttributeDao() {
        val cachingRepository = (CachingPersonAttributeDaoImpl) cachingAttributeRepository.getObject();
        Objects.requireNonNull(cachingRepository, "Unable to locate caching attribute repository from application context");
        return cachingRepository;
    }

}

