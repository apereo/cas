package org.apereo.cas.support.saml;

import module java.base;
import org.apereo.cas.audit.AuditableContext;
import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.BaseAuthenticationServiceSelectionStrategy;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.web.support.WebUtils;
import com.google.common.base.Splitter;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.hc.core5.net.URIBuilder;

/**
 * This is {@link ShibbolethIdPEntityIdAuthenticationServiceSelectionStrategy}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
@Getter
public class ShibbolethIdPEntityIdAuthenticationServiceSelectionStrategy extends BaseAuthenticationServiceSelectionStrategy {
    @Serial
    private static final long serialVersionUID = -2059445756475980894L;

    private final String idpServerPrefix;

    private final AuditableExecution registeredServiceAccessStrategyEnforcer;

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
    public Service resolveServiceFrom(final Service service) throws Throwable {
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
        val matches = service != null && service.getId().matches(casPattern);
        LOGGER.trace("Does service id [{}] match against [{}]: [{}]",
            service, idpServerPrefix, BooleanUtils.toStringYesNo(matches));
        val supported = matches && getEntityIdAsParameter(service).isPresent();
        LOGGER.trace("Is request from [{}] supported by [{}]: [{}]",
            service, getClass().getSimpleName(), BooleanUtils.toStringYesNo(supported));
        return supported;
    }
    
    protected static Optional<String> getEntityIdAsParameter(final Service service) {
        try {
            LOGGER.trace("Checking for query parameters in [{}] to locate entity id", service.getId());
            val builder = new URIBuilder(service.getId());
            val param = builder.getQueryParams()
                .stream()
                .filter(p -> p.getName().equals(SamlProtocolConstants.PARAMETER_ENTITY_ID))
                .findFirst();

            if (param.isPresent()) {
                LOGGER.debug("Found entity id in service id [{}]", param.get().getValue());
                return Optional.of(param.get().getValue());
            }

            if (service.getAttributes().containsKey(SamlProtocolConstants.PARAMETER_ENTITY_ID)) {
                val entityId = CollectionUtils.firstElement(service.getAttributes().get(SamlProtocolConstants.PARAMETER_ENTITY_ID)).map(Object::toString);
                LOGGER.debug("Found entity id in service attributes [{}]", entityId);
                return entityId;
            }
            
            val request = WebUtils.getHttpServletRequestFromExternalWebflowContext();
            if (request != null && StringUtils.isNotBlank(request.getQueryString())) {
                LOGGER.debug("Evaluating http request query string [{}]", request.getQueryString());
                val query = request.getQueryString().split("&");
                val paramRequest = Arrays.stream(query)
                    .map(p -> {
                        var params = Splitter.on("=").splitToList(p);
                        return Pair.of(params.getFirst(), params.get(1));
                    })
                    .filter(p -> p.getKey().equals(SamlProtocolConstants.PARAMETER_ENTITY_ID))
                    .map(Pair::getValue)
                    .map(EncodingUtils::urlDecode)
                    .findFirst();
                LOGGER.debug("Found entity id as part of request url [{}]", paramRequest);
                return paramRequest;
            }
        } catch (final Throwable e) {
            LoggingUtils.error(LOGGER, e);
        }
        LOGGER.trace("Unable to locate entity id for [{}]", service);
        return Optional.empty();
    }

    private boolean isEntityIdServiceRegistered(final String entityId, final Service original) throws Throwable {
        val service = createService(entityId, original);
        val registeredService = getServicesManager().findServiceBy(service);
        val audit = AuditableContext.builder()
            .registeredService(registeredService)
            .build();
        return !registeredServiceAccessStrategyEnforcer.execute(audit).isExecutionFailure();
    }
}
