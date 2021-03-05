package org.apereo.cas.support.saml;

import org.apereo.cas.audit.AuditableContext;
import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.BaseAuthenticationServiceSelectionStrategy;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.web.support.WebUtils;

import com.google.common.base.Splitter;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.client.utils.URIBuilder;

import java.util.Arrays;
import java.util.Optional;

/**
 * This is {@link ShibbolethIdPEntityIdAuthenticationServiceSelectionStrategy}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
@Getter
public class ShibbolethIdPEntityIdAuthenticationServiceSelectionStrategy extends BaseAuthenticationServiceSelectionStrategy {
    private static final long serialVersionUID = -2059445756475980894L;

    private final String idpServerPrefix;

    private final transient AuditableExecution registeredServiceAccessStrategyEnforcer;

    public ShibbolethIdPEntityIdAuthenticationServiceSelectionStrategy(final ServicesManager servicesManager,
        final ServiceFactory<WebApplicationService> webApplicationServiceFactory, final String idpServerPrefix,
        final AuditableExecution registeredServiceAccessStrategyEnforcer) {
        super(servicesManager, webApplicationServiceFactory);
        this.idpServerPrefix = idpServerPrefix;
        this.registeredServiceAccessStrategyEnforcer = registeredServiceAccessStrategyEnforcer;
    }

    /**
     * Method attempts to resolve the service from the entityId parameter.  If present, an attempt is made
     * to find a service corresponding to the entityId in the Service Registry.  If a service is not resolved from
     * a passed entity id, the service= parameter of the request will be returned instead.  This is usually the
     * callback url to the external Shibboleth IdP.
     *
     * @param service the provided service by the caller
     * @return - the resolved service.
     */
    @Override
    public Service resolveServiceFrom(final Service service) {
        val result = getEntityIdAsParameter(service);
        if (result.isPresent()) {
            val entityId = result.get();
            LOGGER.debug("Located entity id [{}] from service authentication request at [{}]", entityId, service.getId());
            if (isEntityIdServiceRegistered(entityId, service)) {
                return createService(entityId, service);
            }
            LOGGER.debug("Entity id [{}] not registered as individual service", entityId);
        }
        LOGGER.debug("Could not located entity id from service authentication request at [{}]", service.getId());
        return service;
    }

    @Override
    public boolean supports(final Service service) {
        val casPattern = "^".concat(idpServerPrefix).concat(".*");
        return service != null && service.getId().matches(casPattern) && getEntityIdAsParameter(service).isPresent();
    }

    /**
     * Gets entity id as parameter.
     *
     * @param service the service
     * @return the entity id as parameter
     */
    protected static Optional<String> getEntityIdAsParameter(final Service service) {
        try {
            val builder = new URIBuilder(service.getId());
            val param = builder.getQueryParams()
                .stream()
                .filter(p -> p.getName().equals(SamlProtocolConstants.PARAMETER_ENTITY_ID))
                .findFirst();

            if (param.isPresent()) {
                LOGGER.debug("Found entity Id in service id [{}]", param.get().getValue());
                return Optional.of(param.get().getValue());
            }
            val request = WebUtils.getHttpServletRequestFromExternalWebflowContext();
            if (request != null && StringUtils.isNotBlank(request.getQueryString())) {
                val query = request.getQueryString().split("&");
                val paramRequest = Arrays.stream(query)
                    .map(p -> {
                        var params = Splitter.on("=").splitToList(p);
                        return Pair.of(params.get(0), params.get(1));
                    })
                    .filter(p -> p.getKey().equals(SamlProtocolConstants.PARAMETER_ENTITY_ID))
                    .map(Pair::getValue)
                    .map(EncodingUtils::urlDecode)
                    .findFirst();
                LOGGER.debug("Found entity id as part of request url [{}]", paramRequest);
                return paramRequest;
            }
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
        return Optional.empty();
    }

    private boolean isEntityIdServiceRegistered(final String entityId, final Service original) {
        val service = createService(entityId, original);
        val registeredService = getServicesManager().findServiceBy(service);
        val audit = AuditableContext.builder()
            .registeredService(registeredService)
            .build();
        return !registeredServiceAccessStrategyEnforcer.execute(audit).isExecutionFailure();
    }
}
