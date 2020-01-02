package org.apereo.cas.mongo;

import org.apereo.cas.configuration.model.support.mongo.BaseMongoDbProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.util.CollectionUtils;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.WriteConcern;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.ssl.SSLContexts;
import org.bson.BSON;
import org.bson.codecs.configuration.CodecRegistries;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.data.annotation.Persistent;
import org.springframework.data.convert.JodaTimeConverters;
import org.springframework.data.convert.Jsr310Converters;
import org.springframework.data.mapping.model.FieldNamingStrategy;
import org.springframework.data.mapping.model.PropertyNameFieldNamingStrategy;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.util.ClassUtils;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This is {@link MongoDbConnectionFactory}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
public class MongoDbConnectionFactory {
    private static final int DEFAULT_PORT = 27017;

    private final MongoCustomConversions customConversions;

    public MongoDbConnectionFactory() {
        this(new ArrayList<>(0));
    }

    public MongoDbConnectionFactory(final Converter... converters) {
        this(Stream.of(converters).collect(Collectors.toList()));
    }

    public MongoDbConnectionFactory(final List<Converter> converters) {
        converters.add(new BaseConverters.LoggerConverter());
        converters.add(new BaseConverters.ClassConverter());
        converters.add(new BaseConverters.CommonsLogConverter());
        converters.add(new BaseConverters.PersonAttributesConverter());
        converters.add(new BaseConverters.CacheLoaderConverter());
        converters.add(new BaseConverters.RunnableConverter());
        converters.add(new BaseConverters.ReferenceQueueConverter());
        converters.add(new BaseConverters.ThreadLocalConverter());
        converters.add(new BaseConverters.CertPathConverter());
        converters.add(new BaseConverters.CaffeinCacheConverter());
        converters.add(new BaseConverters.CaffeinCacheLoaderConverter());
        converters.add(new BaseConverters.CacheConverter());
        converters.add(new BaseConverters.PatternToStringConverter());
        converters.add(new BaseConverters.StringToPatternConverter());
        converters.add(new BaseConverters.CacheBuilderConverter());
        converters.add(new BaseConverters.ObjectIdToLongConverter());
        converters.addAll(JodaTimeConverters.getConvertersToRegister());
        converters.addAll(Jsr310Converters.getConvertersToRegister());
        converters.add(new BaseConverters.BsonTimestampToStringConverter());
        converters.add(new BaseConverters.ZonedDateTimeToDateConverter());
        converters.add(new BaseConverters.DateToZonedDateTimeConverter());
        converters.add(new BaseConverters.BsonTimestampToDateConverter());
        converters.add(new BaseConverters.ZonedDateTimeToStringConverter());
        converters.add(new BaseConverters.StringToZonedDateTimeConverter());

        this.customConversions = new MongoCustomConversions(converters);
    }

    /**
     * Build mongo template.
     *
     * @param mongo the mongo properties settings
     * @return the mongo template
     */
    public MongoTemplate buildMongoTemplate(final BaseMongoDbProperties mongo) {
        val mongoDbFactory = mongoDbFactory(buildMongoDbClient(mongo), mongo);
        return new MongoTemplate(mongoDbFactory, mappingMongoConverter(mongoDbFactory));
    }

    /**
     * Build mongo template.
     *
     * @param clientUri the client uri
     * @return the mongo template
     */
    public MongoTemplate buildMongoTemplate(final String clientUri) {
        return buildMongoTemplate(new ClientUriMongoDbProperties(clientUri));
    }

    /**
     * Create collection.
     *
     * @param mongoTemplate  the mongo template
     * @param collectionName the collection name
     * @param dropCollection the drop collection
     */
    public void createCollection(final MongoOperations mongoTemplate, final String collectionName, final boolean dropCollection) {
        if (dropCollection) {
            LOGGER.trace("Dropping database collection: [{}]", collectionName);
            mongoTemplate.dropCollection(collectionName);
        }

        if (!mongoTemplate.collectionExists(collectionName)) {
            LOGGER.trace("Creating database collection: [{}]", collectionName);
            mongoTemplate.createCollection(collectionName);
        }
    }

    private MongoMappingContext mongoMappingContext() {
        val mappingContext = new MongoMappingContext();
        mappingContext.setInitialEntitySet(getInitialEntitySet());
        mappingContext.setSimpleTypeHolder(this.customConversions.getSimpleTypeHolder());
        mappingContext.setFieldNamingStrategy(MongoDbConnectionFactory.fieldNamingStrategy());
        return mappingContext;
    }

    private MappingMongoConverter mappingMongoConverter(final MongoDbFactory mongoDbFactory) {
        val dbRefResolver = new DefaultDbRefResolver(mongoDbFactory);
        val converter = new MappingMongoConverter(dbRefResolver, this.mongoMappingContext());
        converter.setCustomConversions(customConversions);
        converter.setMapKeyDotReplacement("_#_");
        converter.afterPropertiesSet();
        return converter;
    }

    private static MongoDbFactory mongoDbFactory(final MongoClient mongo, final BaseMongoDbProperties props) {
        if (StringUtils.isNotBlank(props.getClientUri())) {
            val uri = buildMongoClientURI(props.getClientUri(), buildMongoDbClientOptions(props));
            LOGGER.trace("Using database [{}] from the connection client URI", uri.getDatabase());
            return new SimpleMongoDbFactory(uri);
        }
        return new SimpleMongoDbFactory(mongo, props.getDatabaseName());
    }

    private Set<Class<?>> getInitialEntitySet() {
        val initialEntitySet = new HashSet<Class<?>>();
        for (val basePackage : getMappingBasePackages()) {
            initialEntitySet.addAll(scanForEntities(basePackage));
        }
        return initialEntitySet;
    }

    protected Collection<String> getMappingBasePackages() {
        return CollectionUtils.wrap(getClass().getPackage().getName());
    }

    private Set<Class<?>> scanForEntities(final String basePackage) {
        if (!StringUtils.isBlank(basePackage)) {
            return new HashSet<>(0);
        }

        val initialEntitySet = new HashSet<Class<?>>();
        if (StringUtils.isNotBlank(basePackage)) {
            val componentProvider = new ClassPathScanningCandidateComponentProvider(
                false);
            componentProvider.addIncludeFilter(new AnnotationTypeFilter(Document.class));
            componentProvider.addIncludeFilter(new AnnotationTypeFilter(Persistent.class));

            for (val candidate : componentProvider.findCandidateComponents(basePackage)) {
                try {
                    initialEntitySet.add(ClassUtils.forName(candidate.getBeanClassName(), getClass().getClassLoader()));
                } catch (final Exception e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
        }

        return initialEntitySet;
    }

    private static FieldNamingStrategy fieldNamingStrategy() {
        return PropertyNameFieldNamingStrategy.INSTANCE;
    }

    /**
     * Create a MongoClientOptions object.
     * <p>
     * The object will be created from a collection of individual property
     * settings, or a MongoDb client connection string (uri), or some
     * combination of the two.
     * <p>
     * This is complicated by the fact that the default values provided by
     * the CAS code in BaseMongoDbProperties.java are not the same as the
     * default values for the corresponding options provided by the MongoDb
     * Java driver when it creates a MongoClientOptions object.
     * <p>
     * To ensure predictable results in all cases, we initialize the client
     * options from the individual property settings (even if just the CAS
     * default values), and then use those values as the starting point to
     * process the client uri (if one is provided). This way, any options
     * in the uri will override the earlier ones, but any options missing
     * from the uri will have the values (default or user-provided) from
     * the individual property settings.
     * <p>
     * This behavior matches the comment in BaseMongoDbProperties.java for
     * the clientUri property: "If not specified, will fallback onto other
     * individual settings. If specified, takes over all other settings
     * where applicable."
     *
     * @param mongo the property settings (including, perhaps, a client uri)
     * @return a bean containing the MongoClientOptions object
     */
    @SneakyThrows
    private static MongoClientOptions buildMongoDbClientOptions(final BaseMongoDbProperties mongo) {

        var clientOptions = (MongoClientOptions.Builder) null;

        if (StringUtils.isNotBlank(mongo.getClientUri())) {
            val opts = buildMongoClientURI(mongo.getClientUri()).getOptions();
            clientOptions = MongoClientOptions.builder(opts);
        } else {
            clientOptions = MongoClientOptions.builder()
                .writeConcern(WriteConcern.valueOf(mongo.getWriteConcern()))
                .heartbeatConnectTimeout((int) Beans.newDuration(mongo.getTimeout()).toMillis())
                .heartbeatSocketTimeout((int) Beans.newDuration(mongo.getTimeout()).toMillis())
                .maxConnectionLifeTime(mongo.getConns().getLifetime())
                .socketKeepAlive(mongo.isSocketKeepAlive())
                .maxConnectionIdleTime((int) Beans.newDuration(mongo.getIdleTimeout()).toMillis())
                .connectionsPerHost(mongo.getConns().getPerHost())
                .retryWrites(mongo.isRetryWrites())
                .socketTimeout((int) Beans.newDuration(mongo.getTimeout()).toMillis())
                .connectTimeout((int) Beans.newDuration(mongo.getTimeout()).toMillis())
                .sslEnabled(mongo.isSslEnabled());

            if (StringUtils.isNotBlank(mongo.getReplicaSet())) {
                clientOptions.requiredReplicaSetName(mongo.getReplicaSet());
            }
        }
        clientOptions.sslContext(SSLContexts.createSystemDefault());

        BSON.addEncodingHook(ZonedDateTime.class, new BaseConverters.ZonedDateTimeTransformer());

        val codecRegistry = CodecRegistries.fromRegistries(
            CodecRegistries.fromProviders(new BaseConverters.ZonedDateTimeCodecProvider()),
            MongoClient.getDefaultCodecRegistry()
        );
        clientOptions.codecRegistry(codecRegistry);
        return clientOptions.build();
    }

    /**
     * Build mongo db client.
     *
     * @param mongo the mongo
     * @return the mongo client
     */
    public static MongoClient buildMongoDbClient(final BaseMongoDbProperties mongo) {

        if (StringUtils.isNotBlank(mongo.getClientUri())) {
            LOGGER.debug("Using MongoDb client URI [{}] to connect to MongoDb instance", mongo.getClientUri());
            return buildMongoDbClient(mongo.getClientUri(), buildMongoDbClientOptions(mongo));
        }

        val serverAddresses = mongo.getHost().split(",");
        if (serverAddresses.length == 0) {
            throw new BeanCreationException("Unable to build a MongoDb client without any hosts/servers defined");
        }

        List<ServerAddress> servers = new ArrayList<>(0);
        if (serverAddresses.length > 1) {
            LOGGER.debug("Multiple MongoDb server addresses are defined. Ignoring port [{}], "
                + "assuming ports are defined as part of the address", mongo.getPort());
            servers = Arrays.stream(serverAddresses)
                .filter(StringUtils::isNotBlank)
                .map(ServerAddress::new)
                .collect(Collectors.toList());
        } else {
            val port = mongo.getPort() > 0 ? mongo.getPort() : DEFAULT_PORT;
            LOGGER.debug("Found single MongoDb server address [{}] using port [{}]", mongo.getHost(), port);
            val addr = new ServerAddress(mongo.getHost(), port);
            servers.add(addr);
        }

        val credential = buildMongoCredential(mongo);
        return new MongoClient(servers, CollectionUtils.wrap(credential), buildMongoDbClientOptions(mongo));
    }

    private static MongoClient buildMongoDbClient(final String clientUri, final MongoClientOptions clientOptions) {
        val uri = buildMongoClientURI(clientUri, clientOptions);
        return new MongoClient(uri);
    }

    private static MongoCredential buildMongoCredential(final BaseMongoDbProperties mongo) {
        val dbName = StringUtils.defaultIfBlank(mongo.getAuthenticationDatabaseName(), mongo.getDatabaseName());
        return MongoCredential.createCredential(mongo.getUserId(), dbName, mongo.getPassword().toCharArray());
    }

    private static MongoClientURI buildMongoClientURI(final String clientUri, final MongoClientOptions clientOptions) {
        val builder = Optional.ofNullable(clientOptions).map(MongoClientOptions::builder).orElseGet(MongoClientOptions::builder);
        return new MongoClientURI(clientUri, builder);
    }

    private static MongoClientURI buildMongoClientURI(final String clientUri) {
        return buildMongoClientURI(clientUri, null);
    }

    private static class ClientUriMongoDbProperties extends BaseMongoDbProperties {
        private static final long serialVersionUID = -9182480568666563805L;

        ClientUriMongoDbProperties(final String clientUri) {
            setClientUri(clientUri);
        }
    }
}
