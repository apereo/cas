package org.apereo.cas.web.report;

import org.apereo.cas.authentication.credential.BasicIdentifiableCredential;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.BaseCasActuatorEndpoint;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import lombok.val;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.apereo.services.persondir.IPersonAttributes;
import org.apereo.services.persondir.support.CachingPersonAttributeDaoImpl;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.endpoint.annotation.DeleteOperation;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.Selector;
import org.springframework.util.Assert;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * This is {@link CasAttributeRepositoryReportEndpoint}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Endpoint(id = "attributeRepository", enableByDefault = false)
public class CasAttributeRepositoryReportEndpoint extends BaseCasActuatorEndpoint {
    private final ObjectProvider<IPersonAttributeDao> cachingAttributeRepository;

    public CasAttributeRepositoryReportEndpoint(
        final CasConfigurationProperties casProperties,
        final ObjectProvider<IPersonAttributeDao> cachingAttributeRepository) {
        super(casProperties);
        this.cachingAttributeRepository = cachingAttributeRepository;
    }
    
    @ReadOperation
    @Operation(summary = "Display cached attributes in the attribute repository for user",
        parameters = @Parameter(name = "username", required = true, in = ParameterIn.PATH))
    public IPersonAttributes showCachedAttributesFor(@Selector final String username) throws Throwable {
        val cachingRepository = getCachingPersonAttributeDao();
        return cachingRepository.getPerson(username);
    }

    @DeleteOperation
    @Operation(summary = "Remove cached attributes in the attribute repository for user",
        parameters = @Parameter(name = "username", required = true, in = ParameterIn.PATH))
    public void removeCachedAttributesFor(@Selector final String username) throws Throwable {
        final var cachingRepository = getCachingPersonAttributeDao();
        cachingRepository.removeUserAttributes(username);
    }

    private CachingPersonAttributeDaoImpl getCachingPersonAttributeDao() {
        val cachingRepository = (CachingPersonAttributeDaoImpl) cachingAttributeRepository.getObject();
        Objects.requireNonNull(cachingRepository, "Unable to locate caching attribute repository from application context");
        return cachingRepository;
    }

}

