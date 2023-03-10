package org.apereo.cas.ticket.registry;

import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.serialization.TicketSerializationManager;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.function.FunctionUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jooq.lambda.Unchecked;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * This is {@link DynamoDbTicketRegistry}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
public class DynamoDbTicketRegistry extends AbstractTicketRegistry {

    private final DynamoDbTicketRegistryFacilitator dbTableService;

    public DynamoDbTicketRegistry(final CipherExecutor cipherExecutor,
                                  final TicketSerializationManager ticketSerializationManager,
                                  final TicketCatalog ticketCatalog,
                                  final DynamoDbTicketRegistryFacilitator dbTableService) {
        super(cipherExecutor, ticketSerializationManager, ticketCatalog);
        this.dbTableService = dbTableService;
    }

    @Override
    public Stream<? extends Ticket> getSessionsFor(final String principalId) {
        return dbTableService.getSessionsFor(digest(principalId));
    }

    @Override
    public Stream<? extends Ticket> getSessionsWithAttributes(final Map<String, List<Object>> queryAttributes) {
        val filterExpressions = new ArrayList<String>();
        val expressionValues = new HashMap<String, AttributeValue>();
        val expressionAttrNames = new HashMap<String, String>();

        filterExpressions.add("prefix=:prefix");
        queryAttributes.forEach((key, queryValues) -> {
            val expressionParameter = isCipherExecutorEnabled()
                ? digest(key)
                : key.replace('.', '_').replace('-', '_');
            val expressionAttrName = '#' + expressionParameter;

            val criteriaValues = new ArrayList<String>();
            for (var i = 0; i < queryValues.size(); i++) {
                criteriaValues.add("contains(attributes." + expressionAttrName + ", :" + expressionParameter + i + ')');

                val attributeValue = digest(queryValues.get(i).toString());
                expressionValues.put(':' + expressionParameter + i, AttributeValue.builder().s(attributeValue).build());
            }
            filterExpressions.add('(' + String.join(" OR ", criteriaValues) + ')');
            expressionAttrNames.put(expressionAttrName, digest(key));
        });
        val expression = String.join(" AND ", filterExpressions);
        val prefix = dbTableService.getTicketCatalog().findTicketDefinition(TicketGrantingTicket.class)
            .orElseThrow()
            .getPrefix();
        expressionValues.put(":prefix", AttributeValue.builder().s(prefix).build());
        return dbTableService.getSessionsWithAttributes(expression, expressionAttrNames, expressionValues)
            .map(this::decodeTicket)
            .filter(Objects::nonNull);
    }

    @Override
    public void addTicket(final Stream<? extends Ticket> toSave) {
        FunctionUtils.doAndHandle(__ -> {
            val toPut = toSave.map(Unchecked.function(this::toTicketPayload));
            dbTableService.put(toPut);
        });
    }

    @Override
    public void addTicketInternal(final Ticket ticket) {
        FunctionUtils.doAndHandle(__ -> {
            LOGGER.debug("Adding ticket [{}] with ttl [{}s]", ticket.getId(),
                ticket.getExpirationPolicy().getTimeToLive());
            dbTableService.put(toTicketPayload(ticket));
        });
    }

    private DynamoDbTicketRegistryFacilitator.TicketPayload toTicketPayload(final Ticket ticket) throws Exception {
        val encTicket = encodeTicket(ticket);
        val principal = digest(getPrincipalIdFrom(ticket));
        return DynamoDbTicketRegistryFacilitator.TicketPayload
            .builder()
            .originalTicket(ticket)
            .encodedTicket(encTicket)
            .principal(principal)
            .attributes(collectAndDigestTicketAttributes(ticket))
            .build();
    }

    @Override
    public Ticket getTicket(final String ticketId, final Predicate<Ticket> predicate) {
        val encTicketId = digest(ticketId);
        if (StringUtils.isBlank(encTicketId)) {
            return null;
        }
        LOGGER.debug("Retrieving ticket [{}]", ticketId);
        val ticket = dbTableService.get(ticketId, encTicketId);
        val decodedTicket = decodeTicket(ticket);
        if (decodedTicket != null && predicate.test(decodedTicket)) {
            return decodedTicket;
        }
        return null;
    }

    @Override
    public long deleteAll() {
        return dbTableService.deleteAll();
    }

    @Override
    public Collection<? extends Ticket> getTickets() {
        return decodeTickets(dbTableService.getAll());
    }

    @Override
    public Stream<? extends Ticket> stream() {
        return dbTableService.stream().map(this::decodeTicket);
    }

    @Override
    public Ticket updateTicket(final Ticket ticket) throws Exception {
        addTicket(ticket);
        return ticket;
    }

    @Override
    public long deleteSingleTicket(final String ticketIdToDelete) {
        val ticketId = digest(ticketIdToDelete);
        return dbTableService.delete(ticketIdToDelete, ticketId) ? 1 : 0;
    }

    @Override
    public long sessionCount() {
        return dbTableService.countTickets(TicketGrantingTicket.class, TicketGrantingTicket.PREFIX);
    }

    @Override
    public long serviceTicketCount() {
        return dbTableService.countTickets(ServiceTicket.class, ServiceTicket.PREFIX);
    }
}
