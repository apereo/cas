package org.apereo.cas.support.events.listener;

import org.apereo.cas.authentication.adaptive.geo.GeoLocationRequest;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.events.AbstractCasEvent;
import org.apereo.cas.support.events.CasEventRepository;
import org.apereo.cas.support.events.authentication.CasAuthenticationPolicyFailureEvent;
import org.apereo.cas.support.events.authentication.CasAuthenticationTransactionFailureEvent;
import org.apereo.cas.support.events.authentication.adaptive.CasRiskyAuthenticationDetectedEvent;
import org.apereo.cas.support.events.config.CasConfigurationModifiedEvent;
import org.apereo.cas.support.events.dao.CasEvent;
import org.apereo.cas.support.events.ticket.CasTicketGrantingTicketCreatedEvent;
import org.apereo.cas.util.DateTimeUtils;
import org.apereo.cas.util.serialization.TicketIdSanitizationUtils;
import org.apereo.cas.web.support.WebUtils;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.ConfigurationPropertiesBindingPostProcessor;
import org.springframework.cloud.bus.event.RefreshRemoteApplicationEvent;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.cloud.context.refresh.ContextRefresher;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;

import java.util.Collection;
import java.util.Map;

/**
 * This is {@link DefaultCasEventListener} that attempts to consume CAS events
 * upon various authentication events. Event data is persisted into a repository
 * via {@link CasEventRepository}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class DefaultCasEventListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultCasEventListener.class);


    @Autowired
    private ConfigurationPropertiesBindingPostProcessor binder;

    @Autowired(required = false)
    private ContextRefresher contextRefresher;

    @Autowired
    private ApplicationContext applicationContext;

    private final CasEventRepository casEventRepository;

    public DefaultCasEventListener(final CasEventRepository casEventRepository) {
        this.casEventRepository = casEventRepository;
    }

    /**
     * Handle application ready event.
     *
     * @param event the event
     */
    @EventListener
    public void handleApplicationReadyEvent(final ApplicationReadyEvent event) {
        LOGGER.info("CAS is ready to process requests @ [{}]", DateTimeUtils.zonedDateTimeOf(event.getTimestamp()));
    }

    /**
     * Handle refresh event when issued to this CAS server locally.
     *
     * @param event the event
     */
    @EventListener
    public void handleRefreshEvent(final EnvironmentChangeEvent event) {
        LOGGER.debug("Received event [{}]", event);
        rebindCasConfigurationProperties();
    }

    /**
     * Handle refresh event when issued by the cloud bus.
     *
     * @param event the event
     */
    @EventListener
    public void handleRefreshEvent(final RefreshRemoteApplicationEvent event) {
        LOGGER.debug("Received event [{}]", event);
        rebindCasConfigurationProperties();
    }

    /**
     * Handle configuration modified event.
     *
     * @param event the event
     */
    @EventListener
    public void handleConfigurationModifiedEvent(final CasConfigurationModifiedEvent event) {
        if (this.contextRefresher == null) {
            LOGGER.warn("Unable to refresh application context, since no refresher is available");
            return;
        }
        
        if (event.isEligibleForContextRefresh()) {
            LOGGER.info("Received event [{}]. Refreshing CAS configuration...", event);
            final Collection<String> keys = this.contextRefresher.refresh();
            LOGGER.debug("Refreshed the following settings: [{}].", keys);
            rebindCasConfigurationProperties();
            LOGGER.info("CAS finished rebinding configuration with new settings [{}]", keys);
        }
    }

    /**
     * Rebind cas configuration properties.
     */
    public void rebindCasConfigurationProperties() {
        final Map<String, CasConfigurationProperties> map = this.applicationContext.getBeansOfType(CasConfigurationProperties.class);
        final String name = map.keySet().iterator().next();
        LOGGER.debug("Reloading CAS configuration via [{}]", name);
        final Object e = this.applicationContext.getBean(name);
        this.binder.postProcessBeforeInitialization(e, name);
        final Object bean = this.applicationContext.getAutowireCapableBeanFactory().initializeBean(e, name);
        this.applicationContext.getAutowireCapableBeanFactory().autowireBean(bean);
        LOGGER.debug("Reloaded CAS configuration [{}]", name);
    }

    /**
     * Handle TGT creation event.
     *
     * @param event the event
     */
    @EventListener
    public void handleCasTicketGrantingTicketCreatedEvent(final CasTicketGrantingTicketCreatedEvent event) {
        if (this.casEventRepository != null) {
            final CasEvent dto = prepareCasEvent(event);
            dto.putCreationTime(event.getTicketGrantingTicket().getCreationTime());
            dto.putId(TicketIdSanitizationUtils.sanitize(event.getTicketGrantingTicket().getId()));
            dto.setPrincipalId(event.getTicketGrantingTicket().getAuthentication().getPrincipal().getId());
            this.casEventRepository.save(dto);
        }
    }

    /**
     * Handle cas authentication policy failure event.
     *
     * @param event the event
     */
    @EventListener
    public void handleCasAuthenticationTransactionFailureEvent(final CasAuthenticationTransactionFailureEvent event) {
        if (this.casEventRepository != null) {
            final CasEvent dto = prepareCasEvent(event);
            dto.setPrincipalId(event.getCredential().getId());
            dto.putId(CasAuthenticationPolicyFailureEvent.class.getSimpleName());
            this.casEventRepository.save(dto);
        }
    }

    /**
     * Handle cas authentication policy failure event.
     *
     * @param event the event
     */
    @EventListener
    public void handleCasAuthenticationPolicyFailureEvent(final CasAuthenticationPolicyFailureEvent event) {
        if (this.casEventRepository != null) {
            final CasEvent dto = prepareCasEvent(event);
            dto.setPrincipalId(event.getAuthentication().getPrincipal().getId());
            dto.putId(CasAuthenticationPolicyFailureEvent.class.getSimpleName());
            this.casEventRepository.save(dto);
        }
    }

    /**
     * Handle cas risky authentication detected event.
     *
     * @param event the event
     */
    @EventListener
    public void handleCasRiskyAuthenticationDetectedEvent(final CasRiskyAuthenticationDetectedEvent event) {
        if (this.casEventRepository != null) {
            final CasEvent dto = prepareCasEvent(event);
            dto.putId(event.getService().getName());
            dto.setPrincipalId(event.getAuthentication().getPrincipal().getId());
            this.casEventRepository.save(dto);
        }
    }

    private CasEvent prepareCasEvent(final AbstractCasEvent event) {
        final CasEvent dto = new CasEvent();
        dto.setType(event.getClass().getCanonicalName());
        dto.putTimestamp(event.getTimestamp());
        dto.putCreationTime(DateTimeUtils.zonedDateTimeOf(event.getTimestamp()));

        final ClientInfo clientInfo = ClientInfoHolder.getClientInfo();
        dto.putClientIpAddress(clientInfo.getClientIpAddress());
        dto.putServerIpAddress(clientInfo.getServerIpAddress());
        dto.putAgent(WebUtils.getHttpServletRequestUserAgent());

        final GeoLocationRequest location = WebUtils.getHttpServletRequestGeoLocation();
        dto.putGeoLocation(location);
        return dto;
    }

    public CasEventRepository getCasEventRepository() {
        return casEventRepository;
    }
}
