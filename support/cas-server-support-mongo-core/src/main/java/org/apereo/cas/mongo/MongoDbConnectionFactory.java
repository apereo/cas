package org.apereo.cas.mongo;

import org.apereo.cas.configuration.model.support.mongo.BaseMongoDbProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.util.CollectionUtils;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.ReadConcern;
import com.mongodb.ReadConcernLevel;
import com.mongodb.ServerAddress;
import com.mongodb.WriteConcern;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.connection.ConnectionPoolSettings;
import com.mongodb.connection.ServerSettings;
import com.mongodb.connection.SocketSettings;
import com.mongodb.connection.SslSettings;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.ssl.SSLContexts;
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
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
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
    private static final int DEFAULT_PORT = 27017;

    private final MongoCustomConversions customConversions;

    private final SSLContext sslContext;

    public MongoDbConnectionFactory() {
        this(new ArrayList<>(0), SSLContexts.createSystemDefault());
    }

    public MongoDbConnectionFactory(final SSLContext sslContext) {
        this(new ArrayList<>(0), sslContext);
    }


    public MongoDbConnectionFactory(final Converter... converters) {
        this(Stream.of(converters).collect(Collectors.toList()), SSLContexts.createSystemDefault());
    }

    public MongoDbConnectionFactory(final List<Converter> converters, final SSLContext sslContext) {
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
        converters.add(new BaseConverters.BsonTimestampToStringConverter());
        converters.add(new BaseConverters.ZonedDateTimeToDateConverter());
        converters.add(new BaseConverters.DateToZonedDateTimeConverter());
        converters.add(new BaseConverters.BsonTimestampToDateConverter());
        converters.add(new BaseConverters.ZonedDateTimeToStringConverter());
        converters.add(new BaseConverters.StringToZonedDateTimeConverter());
        converters.addAll(JodaTimeConverters.getConvertersToRegister());
        converters.addAll(Jsr310Converters.getConvertersToRegister());

        this.customConversions = new MongoCustomConversions(converters);
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
            val servers = new ArrayList<ServerAddress>(0);
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
                        .maxConnectionLifeTime(poolConfig.getLifeTime(), TimeUnit.MILLISECONDS)
                        .maxConnectionIdleTime(poolConfig.getIdleTime(), TimeUnit.MILLISECONDS)
                        .maxSize(poolConfig.getMaxSize())
                        .minSize(poolConfig.getMinSize())
                        .maxWaitTime(poolConfig.getMaxWaitTime(), TimeUnit.MILLISECONDS)
                        .build();
                    builder.applySettings(pool);
                })
                .applyToSocketSettings(builder -> {
                    val socket = SocketSettings.builder()
                        .connectTimeout((int) Beans.newDuration(mongo.getTimeout()).toMillis(), TimeUnit.MILLISECONDS)
                        .readTimeout((int) Beans.newDuration(mongo.getTimeout()).toMillis(), TimeUnit.MILLISECONDS)
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
                        .heartbeatFrequency((int) Beans.newDuration(mongo.getTimeout()).toMillis(), TimeUnit.MILLISECONDS)
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
    public MongoTemplate buildMongoTemplate(final BaseMongoDbProperties mongo) {
        val mongoDbFactory = mongoDbFactory(buildMongoDbClient(mongo), mongo);
        return new MongoTemplate(mongoDbFactory, mappingMongoConverter(mongoDbFactory));
    }

    private MongoMappingContext mongoMappingContext() {
        val mappingContext = new MongoMappingContext();
        mappingContext.setInitialEntitySet(getInitialEntitySet());
        mappingContext.setSimpleTypeHolder(this.customConversions.getSimpleTypeHolder());
        mappingContext.setFieldNamingStrategy(MongoDbConnectionFactory.fieldNamingStrategy());
        return mappingContext;
    }

    private MappingMongoConverter mappingMongoConverter(final MongoDatabaseFactory mongoDbFactory) {
        val dbRefResolver = new DefaultDbRefResolver(mongoDbFactory);
        val converter = new MappingMongoConverter(dbRefResolver, this.mongoMappingContext());
        converter.setCustomConversions(customConversions);
        converter.setMapKeyDotReplacement("_#_");
        converter.afterPropertiesSet();
        return converter;
    }

    private static MongoDatabaseFactory mongoDbFactory(final MongoClient mongo, final BaseMongoDbProperties props) {
        if (StringUtils.isNotBlank(props.getDatabaseName())) {
            return new SimpleMongoClientDatabaseFactory(mongo, props.getDatabaseName());
        }
        val connectionString = new ConnectionString(props.getClientUri());
        return new SimpleMongoClientDatabaseFactory(mongo, Objects.requireNonNull(connectionString.getDatabase()));
    }

    private Set<Class<?>> getInitialEntitySet() {
        val initialEntitySet = new HashSet<Class<?>>();
        for (val basePackage : getMappingBasePackages()) {
            initialEntitySet.addAll(scanForEntities(basePackage));
        }
        return initialEntitySet;
    }

    private Set<Class<?>> scanForEntities(final String basePackage) {
        if (!StringUtils.isBlank(basePackage)) {
            return new HashSet<>(0);
        }

        val initialEntitySet = new HashSet<Class<?>>();
        if (StringUtils.isNotBlank(basePackage)) {
            val componentProvider = new ClassPathScanningCandidateComponentProvider(false);
            componentProvider.addIncludeFilter(new AnnotationTypeFilter(Document.class));
            componentProvider.addIncludeFilter(new AnnotationTypeFilter(Persistent.class));

            for (val candidate : componentProvider.findCandidateComponents(basePackage)) {
                try {
                    val beanClassName = Objects.requireNonNull(candidate.getBeanClassName());
                    initialEntitySet.add(ClassUtils.forName(beanClassName, getClass().getClassLoader()));
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

    private static MongoCredential buildMongoCredential(final BaseMongoDbProperties mongo) {
        val dbName = StringUtils.defaultIfBlank(mongo.getAuthenticationDatabaseName(), mongo.getDatabaseName());
        return MongoCredential.createCredential(mongo.getUserId(), dbName, mongo.getPassword().toCharArray());
    }

    protected Collection<String> getMappingBasePackages() {
        return CollectionUtils.wrap(getClass().getPackage().getName());
    }
}
