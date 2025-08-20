package org.apereo.cas.support.events.dao;

import org.apereo.cas.support.events.CasEventRepository;
import org.apereo.cas.support.events.CasEventRepositoryFilter;
import org.apereo.cas.util.DateTimeUtils;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.val;
import org.springframework.boot.actuate.audit.AuditEvent;
import org.springframework.boot.actuate.audit.listener.AuditApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * This is {@link AbstractCasEventRepository}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public abstract class AbstractCasEventRepository implements CasEventRepository, ApplicationEventPublisherAware {

    /**
     * Field name to track event type.
     */
    protected static final String TYPE_PARAM = "type";

    /**
     * Field name to track creation type of event.
     */
    protected static final String CREATION_TIME_PARAM = "creationTime";

    /**
     * Field name to track principal linked to the event.
     */
    protected static final String PRINCIPAL_ID_PARAM = "principalId";

    private final CasEventRepositoryFilter eventRepositoryFilter;

    @Setter
    private ApplicationEventPublisher applicationEventPublisher;

    private static ZonedDateTime convertEventCreationTime(final CasEvent event) {
        return DateTimeUtils.zonedDateTimeOf(event.getCreationTime());
    }

    @Override
    public CasEvent save(final CasEvent event) throws Throwable {
        if (getEventRepositoryFilter().shouldSaveEvent(event)) {
            val result = saveInternal(event);
            Optional.ofNullable(applicationEventPublisher).ifPresent(publisher -> {
                val properties = new HashMap(event.getProperties());
                properties.put(CasEventRepository.PARAM_SOURCE, "CAS");
                val auditEvent = new AuditEvent(event.getPrincipalId(), event.getType(), properties);
                publisher.publishEvent(new AuditApplicationEvent(auditEvent));
            });
            return result;
        }
        return event;
    }

    @Override
    public Stream<? extends CasEvent> load(final ZonedDateTime dateTime) {
        return load()
            .filter(e -> {
                val dt = convertEventCreationTime(e);
                return dt.isEqual(dateTime) || dt.isAfter(dateTime);
            });
    }

    @Override
    public Stream<? extends CasEvent> getEventsOfTypeForPrincipal(final String type, final String principal) {
        return getEventsForPrincipal(principal)
            .filter(event -> event.getType().equals(type));
    }

    @Override
    public Stream<? extends CasEvent> getEventsOfTypeForPrincipal(final String type, final String principal,
                                                                  final ZonedDateTime dateTime) {
        return getEventsOfTypeForPrincipal(type, principal)
            .filter(e -> {
                val dt = convertEventCreationTime(e);
                return dt.isEqual(dateTime) || dt.isAfter(dateTime);
            });
    }

    @Override
    public Stream<? extends CasEvent> getEventsOfType(final String type) {
        return load().filter(event -> event.getType().equals(type));
    }

    @Override
    public Stream<? extends CasEvent> getEventsOfType(final String type, final ZonedDateTime dateTime) {
        return getEventsOfType(type)
            .filter(e -> {
                val dt = convertEventCreationTime(e);
                return dt.isEqual(dt) || dt.isAfter(dt);
            });
    }

    @Override
    public Stream<? extends CasEvent> getEventsForPrincipal(final String id) {
        return load().filter(e -> e.getPrincipalId().equalsIgnoreCase(id));
    }

    @Override
    public Stream<? extends CasEvent> getEventsForPrincipal(final String id, final ZonedDateTime dateTime) {
        return getEventsForPrincipal(id)
            .filter(e -> {
                val dt = convertEventCreationTime(e);
                return dt.isEqual(dateTime) || dt.isAfter(dateTime);
            });
    }
    
    /**
     * Save internal.
     *
     * @param event the event
     * @return saved cas event
     * @throws Exception the exception
     */
    public abstract CasEvent saveInternal(CasEvent event) throws Exception;
}
