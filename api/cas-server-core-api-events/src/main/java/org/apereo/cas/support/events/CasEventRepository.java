package org.apereo.cas.support.events;

import org.apereo.cas.support.events.dao.CasEvent;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Defines DAO operations over an events repository.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@FunctionalInterface
public interface CasEventRepository {

    /**
     * The event source.
     */
    String PARAM_SOURCE = "source";

    /**
     * Name of the events transaction manager.
     */
    String TRANSACTION_MANAGER_EVENTS = "transactionManagerEvents";

    /**
     * Bean name.
     */
    String BEAN_NAME = "casEventRepository";

    /**
     * Gets event repository filter.
     *
     * @return the event repository filter
     */
    default CasEventRepositoryFilter getEventRepositoryFilter() {
        return CasEventRepositoryFilter.noOp();
    }

    /**
     * Remove all.
     */
    default void removeAll() {
    }

    /**
     * With transaction.
     *
     * @param <T>    the type parameter
     * @param action the action
     */
    default <T> void withTransaction(final Consumer<T> action) {
        action.accept(null);
    }

    /**
     * Save.
     *
     * @param event the event
     * @return the cas event
     * @throws Throwable the throwable
     */
    CasEvent save(CasEvent event) throws Throwable;

    /**
     * Load collection of events.
     *
     * @return the collection
     */
    default Stream<? extends CasEvent> load() {
        return Stream.empty();
    }

    /**
     * Load collection of events created after the given date.
     *
     * @param dateTime the date time
     * @return the collection
     */
    default Stream<? extends CasEvent> load(final ZonedDateTime dateTime) {
        return Stream.empty();
    }

    /**
     * Gets events of type for principal.
     *
     * @param type      the type
     * @param principal the principal
     * @return the events of type
     */
    default Stream<? extends CasEvent> getEventsOfTypeForPrincipal(final String type, final String principal) {
        return Stream.empty();
    }

    /**
     * Gets events of type for principal after date.
     *
     * @param type      the type
     * @param principal the principal
     * @param dateTime  the date time
     * @return the events of type
     */
    default Stream<? extends CasEvent> getEventsOfTypeForPrincipal(final String type, final String principal, final ZonedDateTime dateTime) {
        return Stream.empty();
    }

    /**
     * Gets events of type.
     *
     * @param type the type
     * @return the events of type
     */
    default Stream<? extends CasEvent> getEventsOfType(final String type) {
        return Stream.empty();
    }

    /**
     * Gets events of type after date.
     *
     * @param type     the type
     * @param dateTime the date time
     * @return the events of type
     */
    default Stream<? extends CasEvent> getEventsOfType(final String type, final ZonedDateTime dateTime) {
        return Stream.empty();
    }

    /**
     * Gets events for principal.
     *
     * @param id the id
     * @return the events for principal
     */
    default Stream<? extends CasEvent> getEventsForPrincipal(final String id) {
        return Stream.empty();
    }

    /**
     * Gets events for principal after date.
     *
     * @param id       the id
     * @param dateTime the date time
     * @return the events for principal
     */
    default Stream<? extends CasEvent> getEventsForPrincipal(final String id, final ZonedDateTime dateTime) {
        return Stream.empty();
    }

    /**
     * Aggregate stream.
     *
     * @return the stream
     */
    default Stream<CasEventAggregate> aggregate() {
        return aggregate(Duration.ofDays(1));
    }

    /**
     * Aggregate stream.
     *
     * @param start the start
     * @return the stream
     */
    default Stream<CasEventAggregate> aggregate(final Duration start) {
        return aggregate(null, start);
    }

    /**
     * Aggregate stream.
     *
     * @param type  the type
     * @param start the start
     * @return the stream
     */
    default Stream<CasEventAggregate> aggregate(final Class type, final Duration start) {
        return Stream.empty();
    }

    /**
     * No op cas event repository that does not save events.
     *
     * @return the cas event repository
     */
    static CasEventRepository noOp() {
        return event -> event;
    }
}
