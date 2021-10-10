package org.apereo.cas.services;

import org.apereo.cas.cosmosdb.CosmosDbDocument;
import org.apereo.cas.services.util.RegisteredServiceJsonSerializer;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.serialization.StringSerializer;

import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.PartitionKey;
import com.fasterxml.jackson.core.util.MinimalPrettyPrinter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * This is {@link CosmosDbServiceRegistry}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
public class CosmosDbServiceRegistry extends AbstractServiceRegistry {
    /**
     * Partition key.
     */
    public static final String PARTITION_KEY = "id";

    private final StringSerializer<RegisteredService> serializer;

    private final CosmosContainer container;

    public CosmosDbServiceRegistry(final CosmosContainer container,
                                   final ConfigurableApplicationContext applicationContext,
                                   final Collection<ServiceRegistryListener> serviceRegistryListeners) {
        super(applicationContext, serviceRegistryListeners);
        this.serializer = new RegisteredServiceJsonSerializer(new MinimalPrettyPrinter());
        this.container = container;
    }

    @Override
    public RegisteredService save(final RegisteredService registeredService) {
        invokeServiceRegistryListenerPreSave(registeredService);
        if (registeredService.getId() == RegisteredService.INITIAL_IDENTIFIER_VALUE) {
            registeredService.setId(System.currentTimeMillis());
            insert(registeredService);
        } else {
            update(registeredService);
        }
        return registeredService;
    }

    @Override
    public boolean delete(final RegisteredService registeredService) {
        val doc = createCosmosDbDocument(registeredService);
        LOGGER.debug("Deleting registered service [{}] from container [{}]", doc.getId(), container.getId());
        val response = container.deleteItem(doc, new CosmosItemRequestOptions());
        return !HttpStatus.valueOf(response.getStatusCode()).isError();
    }

    @Override
    public void deleteAll() {
        LOGGER.debug("Deleting registered services from container [{}]", container.getId());
        val queryOptions = new CosmosQueryRequestOptions();
        val items = container.queryItems("SELECT * FROM " + container.getId(), queryOptions, CosmosDbDocument.class);
        items.iterableByPage()
            .forEach(response -> response.getResults()
                .forEach(doc -> container.deleteItem(doc, new CosmosItemRequestOptions())));
    }

    @Override
    public Collection<RegisteredService> load() {
        val services = new ArrayList<RegisteredService>();
        val queryOptions = new CosmosQueryRequestOptions();
        LOGGER.trace("Loading registered services from container [{}]", container.getId());
        val items = container.queryItems("SELECT * FROM " + container.getId(), queryOptions, CosmosDbDocument.class);
        items.iterableByPage()
            .forEach(response -> services.addAll(response.getResults()
                .stream()
                .map(this::getRegisteredServiceFromDocumentBody)
                .peek(this::invokeServiceRegistryListenerPostLoad)
                .collect(Collectors.toList())));
        return services;
    }

    @Override
    public RegisteredService findServiceBy(final String id) {
        val services = new ArrayList<RegisteredService>();
        val queryOptions = new CosmosQueryRequestOptions();
        LOGGER.trace("Loading registered service by [{}] from container [{}]", id, container.getId());
        val query = String.format("SELECT * FROM %s r WHERE r.serviceId LIKE '%s'", container.getId(), '%' + id + '%');
        val items = container.queryItems(query, queryOptions, CosmosDbDocument.class);
        items.iterableByPage()
            .forEach(response -> services.addAll(response.getResults()
                .stream()
                .map(this::getRegisteredServiceFromDocumentBody)
                .sorted()
                .filter(r -> r.matches(id))
                .peek(this::invokeServiceRegistryListenerPostLoad)
                .collect(Collectors.toList())));
        return services.isEmpty() ? null : services.get(0);
    }

    @Override
    public RegisteredService findServiceById(final long id) {
        try {
            val key = String.valueOf(id);
            LOGGER.debug("Reading registered services with id [{}] from [{}]", key, container.getId());
            val doc = container.readItem(key, new PartitionKey(key), CosmosDbDocument.class).getItem();
            return getRegisteredServiceFromDocumentBody(doc);
        } catch (final CosmosException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND.value()) {
                LOGGER.debug("Unable to locate registered service with id [{}]", id);
                LOGGER.trace(e.getMessage(), e);
            } else {
                LoggingUtils.warn(LOGGER, e);
            }
        }
        return null;
    }

    private RegisteredService getRegisteredServiceFromDocumentBody(final CosmosDbDocument doc) {
        return this.serializer.from(doc.getBody());
    }

    private void insert(final RegisteredService registeredService) {
        val doc = createCosmosDbDocument(registeredService);
        LOGGER.trace("Creating registered service with id [{}] in container [{}]", doc.getId(), container.getId());
        container.createItem(doc);
    }

    private void update(final RegisteredService registeredService) {
        val doc = createCosmosDbDocument(registeredService);
        LOGGER.trace("Upserting registered service with id [{}] in container [{}]", doc.getId(), container.getId());
        container.upsertItem(doc);
    }

    private CosmosDbDocument createCosmosDbDocument(final RegisteredService registeredService) {
        val body = serializer.toString(registeredService);
        val document = new CosmosDbDocument();
        document.setBody(body);
        document.setId(String.valueOf(registeredService.getId()));
        document.setServiceId(registeredService.getServiceId());
        return document;
    }
}
