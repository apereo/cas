package org.apereo.cas.ticket.registry;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.DeleteTableRequest;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;
import com.amazonaws.services.dynamodbv2.util.TableUtils;
import com.google.common.base.Throwables;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.CipherExecutor;
import org.apereo.cas.configuration.model.support.dynamodb.DynamoDbProperties;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.TicketDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.util.Collection;

/**
 * This is {@link DynamoDbTicketRegistry}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class DynamoDbTicketRegistry extends AbstractTicketRegistry {
    private static final Logger LOGGER = LoggerFactory.getLogger(DynamoDbTicketRegistry.class);

    private final TicketCatalog ticketCatalog;
    private final DynamoDbProperties dynamoDbProperties;
    private final AmazonDynamoDBClient amazonDynamoDBClient;

    public DynamoDbTicketRegistry(final CipherExecutor cipher,
                                  final TicketCatalog ticketCatalog,
                                  final DynamoDbProperties dynamoDbProperties) {
        setCipherExecutor(cipher);

        this.ticketCatalog = ticketCatalog;
        this.dynamoDbProperties = dynamoDbProperties;
        this.amazonDynamoDBClient = initializeAmazonDynamoDBClient();

        createTables();
    }

    @Override
    public void addTicket(final Ticket ticket) {

    }

    @Override
    public Ticket getTicket(final String ticketId) {
        return null;
    }

    @Override
    public long deleteAll() {
        return 0;
    }

    @Override
    public Collection<Ticket> getTickets() {
        return null;
    }

    @Override
    public Ticket updateTicket(final Ticket ticket) {
        return null;
    }

    @Override
    public boolean deleteSingleTicket(final String ticketId) {
        return false;
    }

    private AmazonDynamoDBClient initializeAmazonDynamoDBClient() {
        try {
            final ClientConfiguration cfg = new ClientConfiguration();
            cfg.setConnectionTimeout(dynamoDbProperties.getConnectionTimeout());
            cfg.setMaxConnections(dynamoDbProperties.getMaxConnections());
            cfg.setRequestTimeout(dynamoDbProperties.getRequestTimeout());
            cfg.setSocketTimeout(dynamoDbProperties.getSocketTimeout());
            cfg.setUseGzip(dynamoDbProperties.isUseGzip());
            cfg.setUseReaper(dynamoDbProperties.isUseReaper());
            cfg.setUseThrottleRetries(dynamoDbProperties.isUseThrottleRetries());
            cfg.setUseTcpKeepAlive(dynamoDbProperties.isUseTcpKeepAlive());
            cfg.setProtocol(Protocol.valueOf(dynamoDbProperties.getProtocol().toUpperCase()));
            cfg.setClientExecutionTimeout(dynamoDbProperties.getClientExecutionTimeout());
            cfg.setCacheResponseMetadata(dynamoDbProperties.isCacheResponseMetadata());

            if (StringUtils.isNotBlank(dynamoDbProperties.getLocalAddress())) {
                cfg.setLocalAddress(InetAddress.getByName(dynamoDbProperties.getLocalAddress()));
            }

            AWSCredentials credentials = null;
            if (dynamoDbProperties.getCredentialsPropertiesFile() != null) {
                credentials = new PropertiesCredentials(dynamoDbProperties.getCredentialsPropertiesFile().getInputStream());
            } else if (StringUtils.isNotBlank(dynamoDbProperties.getCredentialAccessKey()) && StringUtils.isNotBlank(dynamoDbProperties.getCredentialSecretKey())) {
                credentials = new BasicAWSCredentials(dynamoDbProperties.getCredentialAccessKey(), dynamoDbProperties.getCredentialSecretKey());
            }

            final AmazonDynamoDBClient client;
            if (credentials == null) {
                client = new AmazonDynamoDBClient(cfg);
            } else {
                client = new AmazonDynamoDBClient(credentials, cfg);
            }

            if (StringUtils.isNotBlank(dynamoDbProperties.getEndpoint())) {
                client.setEndpoint(dynamoDbProperties.getEndpoint());
            }

            if (StringUtils.isNotBlank(dynamoDbProperties.getRegion())) {
                client.setRegion(Region.getRegion(Regions.valueOf(dynamoDbProperties.getRegion())));
            }

            if (StringUtils.isNotBlank(dynamoDbProperties.getRegionOverride())) {
                client.setSignerRegionOverride(dynamoDbProperties.getRegionOverride());
            }

            return client;
        } catch (final Exception e) {
            throw Throwables.propagate(e);
        }
    }

    private void createTables() {
        final Collection<TicketDefinition> metadata = this.ticketCatalog.findAll();
        metadata.forEach(r -> {
            final CreateTableRequest request = new CreateTableRequest()
                    .withAttributeDefinitions(new AttributeDefinition("id", ScalarAttributeType.S))
                            .withKeySchema(new KeySchemaElement("id", KeyType.HASH))
                            .withProvisionedThroughput(new ProvisionedThroughput(dynamoDbProperties.getReadCapacity(), dynamoDbProperties.getWriteCapacity()))
                            .withTableName(r.getProperties().getStorageName());
            
            if (dynamoDbProperties.isDropTablesOnStartup()) {
                final DeleteTableRequest delete = new DeleteTableRequest(r.getProperties().getStorageName());
                TableUtils.deleteTableIfExists(amazonDynamoDBClient, delete);
            }
            TableUtils.createTableIfNotExists(amazonDynamoDBClient, request);
        });
    }
}
