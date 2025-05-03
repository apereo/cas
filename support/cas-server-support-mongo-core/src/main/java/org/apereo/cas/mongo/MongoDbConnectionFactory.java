package org.apereo.cas.mongo;

import org.apereo.cas.configuration.model.support.mongo.BaseMongoDbProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.function.FunctionUtils;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.ReadConcern;
import com.mongodb.ReadConcernLevel;
import com.mongodb.ServerAddress;
import com.mongodb.WriteConcern;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.connection.ConnectionPoolSettings;
import com.mongodb.connection.ServerSettings;
import com.mongodb.connection.SocketSettings;
import com.mongodb.connection.SslSettings;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.core5.ssl.SSLContexts;
import org.bson.codecs.configuration.CodecRegistries;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.data.annotation.Persistent;
import org.springframework.data.convert.Jsr310Converters;
import org.springframework.data.mapping.model.FieldNamingStrategy;
import org.springframework.data.mapping.model.PropertyNameFieldNamingStrategy;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.core.index.IndexDefinition;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.util.ClassUtils;

import javax.net.ssl.SSLContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
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
    private static final Set<String> MONGO_INDEX_KEYS = Set.of("v", "key", "name", "ns");

    private static final int DEFAULT_PORT = 27017;

    @Getter
    private final MongoCustomConversions customConversions;

    private final SSLContext sslContext;

    public MongoDbConnectionFactory() {
        this(new ArrayList<>(), SSLContexts.createSystemDefault());
    }

    public MongoDbConnectionFactory(final SSLContext sslContext) {
        this(new ArrayList<>(), sslContext);
    }

    public MongoDbConnectionFactory(final Converter... converters) {
        this(Stream.of(converters).collect(Collectors.toList()), SSLContexts.createSystemDefault());
    }

    public MongoDbConnectionFactory(final List<Converter> converters, final SSLContext sslContext) {
        val mongoConverters = new ArrayList<>(converters);
        mongoConverters.add(new BaseConverters.LoggerConverter());
        mongoConverters.add(new BaseConverters.ClassConverter());
        mongoConverters.add(new BaseConverters.CommonsLogConverter());
        mongoConverters.add(new BaseConverters.PersonAttributesConverter());
        mongoConverters.add(new BaseConverters.CacheLoaderConverter());
        mongoConverters.add(new BaseConverters.RunnableConverter());
        mongoConverters.add(new BaseConverters.ReferenceQueueConverter());
        mongoConverters.add(new BaseConverters.ThreadLocalConverter());
        mongoConverters.add(new BaseConverters.CertPathConverter());
        mongoConverters.add(new BaseConverters.CaffeinCacheConverter());
        mongoConverters.add(new BaseConverters.CaffeinCacheLoaderConverter());
        mongoConverters.add(new BaseConverters.CacheConverter());
        mongoConverters.add(new BaseConverters.PatternToStringConverter());
        mongoConverters.add(new BaseConverters.StringToPatternConverter());
        mongoConverters.add(new BaseConverters.CacheBuilderConverter());
        mongoConverters.add(new BaseConverters.ObjectIdToLongConverter());
        mongoConverters.add(new BaseConverters.BsonTimestampToStringConverter());
        mongoConverters.add(new BaseConverters.ZonedDateTimeToDateConverter());
        mongoConverters.add(new BaseConverters.DateToZonedDateTimeConverter());
        mongoConverters.add(new BaseConverters.BsonTimestampToDateConverter());
        mongoConverters.add(new BaseConverters.ZonedDateTimeToStringConverter());
        mongoConverters.add(new BaseConverters.StringToZonedDateTimeConverter());
        mongoConverters.addAll(Jsr310Converters.getConvertersToRegister());

        this.customConversions = new MongoCustomConversions(mongoConverters);
        this.sslContext = sslContext;
    }

    /**
     * Create collection.
     *
     * @param mongoTemplate  the mongo template
     * @param collectionName the collection name
     * @param dropCollection the drop collection
     */
    public static void createCollection(final MongoOperations mongoTemplate, final String collectionName, final boolean dropCollection) {
        if (dropCollection) {
            LOGGER.trace("Dropping database collection: [{}]", collectionName);
            mongoTemplate.dropCollection(collectionName);
        }

        if (!mongoTemplate.collectionExists(collectionName)) {
            LOGGER.trace("Creating database collection: [{}]", collectionName);
            mongoTemplate.createCollection(collectionName);
        }
    }

    /**
     * Drop collection indexes.
     *
     * @param collection the collection
     */
    public static void dropCollectionIndexes(final MongoCollection<org.bson.Document> collection) {
        collection.dropIndexes();
    }

    /**
     * Remove any index with the same indexKey but differing indexOptions in anticipation of recreating it.
     *
     * @param mongoTemplate   the mongo template
     * @param collection      The collection to check the indexes of
     * @param indexesToCreate the indexes to create
     */
    public static void createOrUpdateIndexes(final MongoOperations mongoTemplate,
                                             final MongoCollection<org.bson.Document> collection,
                                             final List<? extends IndexDefinition> indexesToCreate) {
        val collectionName = collection.getNamespace().getCollectionName();
        val indexes = collection.listIndexes();
        LOGGER.debug("Existing indexes on collection [{}] are [{}]", collection.getNamespace(), indexes);
        indexesToCreate.forEach(index -> {
            var indexExistsWithDifferentOptions = false;
            val indexKeys = index.getIndexKeys();
            val indexOptions = index.getIndexOptions();
            for (val existingIndex : indexes) {
                val keyMatches = existingIndex.get("key").equals(indexKeys);
                val optionsMatch = indexOptions.entrySet().stream()
                    .allMatch(entry -> entry.getValue().equals(existingIndex.get(entry.getKey())));
                val noExtraOptions = existingIndex.keySet().stream()
                    .allMatch(key -> MONGO_INDEX_KEYS.contains(key) || indexOptions.containsKey(key));
                indexExistsWithDifferentOptions = indexExistsWithDifferentOptions || (keyMatches && !(optionsMatch && noExtraOptions));
            }

            try {
                if (indexExistsWithDifferentOptions) {
                    LOGGER.debug("Removing MongoDb index [{}] from [{}]", indexKeys, collection.getNamespace());
                    collection.dropIndex(indexKeys);
                }
                LOGGER.debug("Creating index [{}] on collection [{}]", index, collectionName);
                mongoTemplate.indexOps(collectionName).createIndex(index);
            } catch (final Exception e) {
                LoggingUtils.warn(LOGGER, e);
            }
        });
    }

    private static MongoDatabaseFactory mongoDbFactory(final MongoClient mongo, final BaseMongoDbProperties props) {
        if (StringUtils.isNotBlank(props.getDatabaseName())) {
            return new SimpleMongoClientDatabaseFactory(mongo, props.getDatabaseName());
        }
        val connectionString = new ConnectionString(props.getClientUri());
        return new SimpleMongoClientDatabaseFactory(mongo, Objects.requireNonNull(connectionString.getDatabase()));
    }

    private static FieldNamingStrategy fieldNamingStrategy() {
        return PropertyNameFieldNamingStrategy.INSTANCE;
    }

    private static MongoCredential buildMongoCredential(final BaseMongoDbProperties mongo) {
        val dbName = StringUtils.defaultIfBlank(mongo.getAuthenticationDatabaseName(), mongo.getDatabaseName());
        return MongoCredential.createCredential(mongo.getUserId(), dbName, mongo.getPassword().toCharArray());
    }

    /**
     * Build mongo db client.
     *
     * @param mongo the mongo
     * @return the mongo client
     */
    public MongoClient buildMongoDbClient(final BaseMongoDbProperties mongo) {
        val settingsBuilder = MongoClientSettings.builder();

        if (StringUtils.isNotBlank(mongo.getClientUri())) {
            LOGGER.debug("Using MongoDb client URI [{}] to connect to MongoDb instance", mongo.getClientUri());
            settingsBuilder.applyConnectionString(new ConnectionString(mongo.getClientUri()));
        } else {
            val serverAddresses = mongo.getHost().split(",");
            if (serverAddresses.length == 0) {
                throw new BeanCreationException("Unable to build a MongoDb client without any hosts/servers defined");
            }
            val servers = new ArrayList<ServerAddress>();
            if (serverAddresses.length > 1) {
                LOGGER.debug("Multiple MongoDb server addresses are defined. Ignoring port [{}], "
                             + "assuming ports are defined as part of the address", mongo.getPort());
                Arrays.stream(serverAddresses)
                    .filter(StringUtils::isNotBlank)
                    .map(ServerAddress::new)
                    .forEach(servers::add);
            } else {
                val port = mongo.getPort() > 0 ? mongo.getPort() : DEFAULT_PORT;
                LOGGER.debug("Found single MongoDb server address [{}] using port [{}]", mongo.getHost(), port);
                val addr = new ServerAddress(mongo.getHost(), port);
                servers.add(addr);
            }
            settingsBuilder.applyToClusterSettings(builder -> builder.hosts(servers));
            val credential = buildMongoCredential(mongo);
            settingsBuilder
                .credential(credential)
                .writeConcern(WriteConcern.valueOf(mongo.getWriteConcern()))
                .codecRegistry(CodecRegistries.fromRegistries(
                    CodecRegistries.fromProviders(new BaseConverters.ZonedDateTimeCodecProvider()),
                    MongoClientSettings.getDefaultCodecRegistry()))
                .readConcern(new ReadConcern(ReadConcernLevel.valueOf(mongo.getReadConcern())))
                .applyToConnectionPoolSettings(builder -> {
                    val poolConfig = mongo.getPool();
                    val pool = ConnectionPoolSettings.builder()
                        .maxConnectionLifeTime(Beans.newDuration(poolConfig.getLifeTime()).toMillis(), TimeUnit.MILLISECONDS)
                        .maxConnectionIdleTime(Beans.newDuration(poolConfig.getIdleTime()).toMillis(), TimeUnit.MILLISECONDS)
                        .maxSize(poolConfig.getMaxSize())
                        .minSize(poolConfig.getMinSize())
                        .maxWaitTime(Beans.newDuration(poolConfig.getMaxWaitTime()).toMillis(), TimeUnit.MILLISECONDS)
                        .build();
                    builder.applySettings(pool);
                })
                .applyToSocketSettings(builder -> {
                    val socket = SocketSettings.builder()
                        .connectTimeout(Beans.newDuration(mongo.getTimeout()).toMillis(), TimeUnit.MILLISECONDS)
                        .readTimeout(Beans.newDuration(mongo.getTimeout()).toMillis(), TimeUnit.MILLISECONDS)
                        .build();
                    builder.applySettings(socket);
                })
                .applyToSslSettings(builder -> {
                    val ssl = SslSettings.builder()
                        .enabled(mongo.isSslEnabled())
                        .context(this.sslContext)
                        .build();
                    builder.applySettings(ssl);
                })
                .applyToServerSettings(builder -> {
                    val server = ServerSettings.builder()
                        .heartbeatFrequency(Beans.newDuration(mongo.getTimeout()).toMillis(), TimeUnit.MILLISECONDS)
                        .build();
                    builder.applySettings(server);
                })
                .retryWrites(mongo.isRetryWrites());
        }
        return MongoClients.create(settingsBuilder.build());
    }

    /**
     * Build mongo template.
     *
     * @param mongo the mongo properties settings
     * @return the mongo template
     */
    public CasMongoOperations buildMongoTemplate(final BaseMongoDbProperties mongo) {
        val mongoDbFactory = mongoDbFactory(buildMongoDbClient(mongo), mongo);
        return new DefaultCasMongoTemplate(mongoDbFactory, mappingMongoConverter(mongoDbFactory));
    }

    /**
     * Build mongo template.
     *
     * @param mongoClient the mongo client
     * @param mongo       the mongo
     * @return the cas mongo operations
     */
    public CasMongoOperations buildMongoTemplate(final MongoClient mongoClient, final BaseMongoDbProperties mongo) {
        val mongoDbFactory = mongoDbFactory(mongoClient, mongo);
        return new DefaultCasMongoTemplate(mongoDbFactory, mappingMongoConverter(mongoDbFactory));
    }

    protected Collection<String> getMappingBasePackages() {
        return CollectionUtils.wrap(getClass().getPackage().getName());
    }

    private MongoMappingContext mongoMappingContext() {
        val mappingContext = new MongoMappingContext();
        mappingContext.setInitialEntitySet(getInitialEntitySet());
        mappingContext.setSimpleTypeHolder(customConversions.getSimpleTypeHolder());
        mappingContext.setFieldNamingStrategy(MongoDbConnectionFactory.fieldNamingStrategy());
        return mappingContext;
    }

    private MappingMongoConverter mappingMongoConverter(final MongoDatabaseFactory mongoDbFactory) {
        val dbRefResolver = new DefaultDbRefResolver(mongoDbFactory);
        val converter = new MappingMongoConverter(dbRefResolver, mongoMappingContext());
        converter.setCustomConversions(customConversions);
        converter.setMapKeyDotReplacement("_#_");
        converter.afterPropertiesSet();
        return converter;
    }

    private Set<Class<?>> getInitialEntitySet() {
        return getMappingBasePackages().stream()
            .flatMap(basePackage -> scanForEntities(basePackage).stream())
            .collect(Collectors.toSet());
    }

    private Set<Class<?>> scanForEntities(final String basePackage) {
        if (StringUtils.isBlank(basePackage)) {
            return new HashSet<>();
        }

        val initialEntitySet = new HashSet<Class<?>>();
        if (StringUtils.isNotBlank(basePackage)) {
            val componentProvider = new ClassPathScanningCandidateComponentProvider(false);
            componentProvider.addIncludeFilter(new AnnotationTypeFilter(Document.class));
            componentProvider.addIncludeFilter(new AnnotationTypeFilter(Persistent.class));
            initialEntitySet.addAll(findAndLoadComponents(basePackage, componentProvider));
        }

        return initialEntitySet;
    }

    private Set<Class<?>> findAndLoadComponents(final String basePackage,
                                                final ClassPathScanningCandidateComponentProvider componentProvider) {
        return FunctionUtils.doUnchecked(() -> {
            val initialEntitySet = new HashSet<Class<?>>();
            for (val candidate : componentProvider.findCandidateComponents(basePackage)) {
                val beanClassName = Objects.requireNonNull(candidate.getBeanClassName());
                initialEntitySet.add(ClassUtils.forName(beanClassName, getClass().getClassLoader()));
            }
            return initialEntitySet;
        });
    }
}
