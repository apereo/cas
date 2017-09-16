package org.apereo.cas.mongo;

import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.WriteConcern;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.configuration.model.support.mongo.BaseMongoDbProperties;
import org.apereo.cas.util.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.data.annotation.Persistent;
import org.springframework.data.convert.JodaTimeConverters;
import org.springframework.data.convert.Jsr310Converters;
import org.springframework.data.mapping.model.CamelCaseAbbreviatingFieldNamingStrategy;
import org.springframework.data.mapping.model.FieldNamingStrategy;
import org.springframework.data.mapping.model.PropertyNameFieldNamingStrategy;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoClientOptionsFactoryBean;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;
import org.springframework.data.mongodb.core.convert.CustomConversions;
import org.springframework.data.mongodb.core.convert.DbRefResolver;
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.util.ClassUtils;

import javax.net.ssl.SSLSocketFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This is {@link MongoDbConnectionFactory}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class MongoDbConnectionFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(MongoDbConnectionFactory.class);
    private static final int TIMEOUT = 5000;
    private static final int DEFAULT_PORT = 27017;

    private final CustomConversions customConversions;

    public MongoDbConnectionFactory() {
        final List<Converter> converters = new ArrayList();
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

        converters.addAll(JodaTimeConverters.getConvertersToRegister());
        converters.addAll(Jsr310Converters.getConvertersToRegister());

        this.customConversions = new CustomConversions(converters);
    }

    /**
     * Build mongo template mongo template.
     *
     * @param mongo the mongo
     * @return the mongo template
     */
    public MongoTemplate buildMongoTemplate(final BaseMongoDbProperties mongo) {
        final MongoDbFactory mongoDbFactory = mongoDbFactory(buildMongoDbClient(mongo), mongo);
        return new MongoTemplate(mongoDbFactory, mappingMongoConverter(mongoDbFactory));
    }

    /**
     * Build mongo template mongo template.
     *
     * @param clientUri the client uri
     * @return the mongo template
     */
    public MongoTemplate buildMongoTemplate(final String clientUri) {
        final MongoClientURI uri = buildMongoClientURI(clientUri);
        final Mongo mongo = buildMongoDbClient(clientUri, buildMongoDbClientOptions());
        final MongoDbFactory mongoDbFactory = mongoDbFactory(mongo, uri.getDatabase());
        return new MongoTemplate(mongoDbFactory, mappingMongoConverter(mongoDbFactory));
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
            LOGGER.debug("Dropping database collection: [{}]", collectionName);
            mongoTemplate.dropCollection(collectionName);
        }

        if (!mongoTemplate.collectionExists(collectionName)) {
            LOGGER.debug("Creating database collection: [{}]", collectionName);
            mongoTemplate.createCollection(collectionName);
        }
    }
    
    private MongoMappingContext mongoMappingContext() {
        final MongoMappingContext mappingContext = new MongoMappingContext();
        mappingContext.setInitialEntitySet(getInitialEntitySet());
        mappingContext.setSimpleTypeHolder(this.customConversions.getSimpleTypeHolder());
        mappingContext.setFieldNamingStrategy(this.fieldNamingStrategy());
        return mappingContext;
    }

    private MappingMongoConverter mappingMongoConverter(final MongoDbFactory mongoDbFactory) {
        final DbRefResolver dbRefResolver = new DefaultDbRefResolver(mongoDbFactory);
        final MappingMongoConverter converter = new MappingMongoConverter(dbRefResolver, this.mongoMappingContext());
        converter.setCustomConversions(customConversions);
        converter.afterPropertiesSet();
        return converter;
    }

    private MongoDbFactory mongoDbFactory(final Mongo mongo, final BaseMongoDbProperties props) {
        final String dbName;
        if (StringUtils.isNotBlank(props.getClientUri())) {
            final MongoClientURI uri = buildMongoClientURI(props.getClientUri());
            dbName = uri.getDatabase();
            LOGGER.debug("Using database [{}] from the connection client URI", dbName);
        } else {
            dbName = props.getDatabaseName();
            LOGGER.debug("Using database [{}] from individual settings", dbName);
        }

        if (StringUtils.isBlank(dbName)) {
            LOGGER.error("Database name cannot be undefined. It must be specified as part of the client URI connection string if used, or "
                    + "as an individual setting for the MongoDb connection");
        }
        return new SimpleMongoDbFactory(mongo, dbName, null, props.getAuthenticationDatabaseName());
    }

    private MongoDbFactory mongoDbFactory(final Mongo mongo, final String databaseName) {
        return new SimpleMongoDbFactory(mongo, databaseName, null, null);
    }

    private Set<Class<?>> getInitialEntitySet() {
        final Set<Class<?>> initialEntitySet = new HashSet<>();
        for (final String basePackage : getMappingBasePackages()) {
            initialEntitySet.addAll(scanForEntities(basePackage));
        }
        return initialEntitySet;
    }

    protected Collection<String> getMappingBasePackages() {
        return CollectionUtils.wrap(getClass().getPackage().getName());
    }

    private Set<Class<?>> scanForEntities(final String basePackage) {
        if (!StringUtils.isBlank(basePackage)) {
            return new HashSet<>();
        }

        final Set<Class<?>> initialEntitySet = new HashSet<>();

        if (StringUtils.isNotBlank(basePackage)) {
            final ClassPathScanningCandidateComponentProvider componentProvider = new ClassPathScanningCandidateComponentProvider(
                    false);
            componentProvider.addIncludeFilter(new AnnotationTypeFilter(Document.class));
            componentProvider.addIncludeFilter(new AnnotationTypeFilter(Persistent.class));

            for (final BeanDefinition candidate : componentProvider.findCandidateComponents(basePackage)) {
                try {
                    initialEntitySet.add(ClassUtils.forName(candidate.getBeanClassName(), getClass().getClassLoader()));
                } catch (final Exception e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
        }

        return initialEntitySet;
    }

    private boolean abbreviateFieldNames() {
        return false;
    }

    private FieldNamingStrategy fieldNamingStrategy() {
        return abbreviateFieldNames() ? new CamelCaseAbbreviatingFieldNamingStrategy()
                : PropertyNameFieldNamingStrategy.INSTANCE;
    }

    private MongoClientOptionsFactoryBean buildMongoDbClientOptionsFactoryBean(final BaseMongoDbProperties mongo) {
        try {
            final MongoClientOptionsFactoryBean bean = new MongoClientOptionsFactoryBean();
            bean.setWriteConcern(WriteConcern.valueOf(mongo.getWriteConcern()));
            bean.setHeartbeatConnectTimeout((int) mongo.getTimeout());
            bean.setHeartbeatSocketTimeout((int) mongo.getTimeout());
            bean.setMaxConnectionLifeTime(mongo.getConns().getLifetime());
            bean.setSocketKeepAlive(mongo.isSocketKeepAlive());
            bean.setMaxConnectionIdleTime((int) mongo.getIdleTimeout());
            bean.setConnectionsPerHost(mongo.getConns().getPerHost());
            bean.setSocketTimeout((int) mongo.getTimeout());
            bean.setConnectTimeout((int) mongo.getTimeout());
            if (StringUtils.isNotBlank(mongo.getReplicaSet())) {
                bean.setRequiredReplicaSetName(mongo.getReplicaSet());
            }
            bean.setSsl(mongo.isSslEnabled());
            if (mongo.isSslEnabled()) {
                bean.setSslSocketFactory((SSLSocketFactory) SSLSocketFactory.getDefault());
            }
            bean.afterPropertiesSet();
            return bean;
        } catch (final Exception e) {
            throw new BeanCreationException(e.getMessage(), e);
        }
    }

    private MongoClientOptions buildMongoDbClientOptions(final BaseMongoDbProperties mongo) {
        try {
            return buildMongoDbClientOptionsFactoryBean(mongo).getObject();
        } catch (final Exception e) {
            throw new BeanCreationException(e.getMessage(), e);
        }
    }

    private MongoClientOptions buildMongoDbClientOptions() {
        try {
            final MongoClientOptionsFactoryBean bean = new MongoClientOptionsFactoryBean();
            bean.setSocketTimeout(TIMEOUT);
            bean.setConnectTimeout(TIMEOUT);
            bean.setMaxWaitTime(TIMEOUT);
            bean.afterPropertiesSet();
            return bean.getObject();
        } catch (final Exception e) {
            throw new BeanCreationException(e.getMessage(), e);
        }
    }

    private Mongo buildMongoDbClient(final BaseMongoDbProperties mongo) {

        if (StringUtils.isNotBlank(mongo.getClientUri())) {
            LOGGER.debug("Using MongoDb client URI [{}] to connect to MongoDb instance", mongo.getClientUri());
            return buildMongoDbClient(mongo.getClientUri(), buildMongoDbClientOptions(mongo));
        }

        final String[] serverAddresses = mongo.getHost().split(",");
        if (serverAddresses == null || serverAddresses.length == 0) {
            throw new BeanCreationException("Unable to build a MongoDb client without any hosts/servers defined");
        }

        List<ServerAddress> servers = new ArrayList<>();
        if (serverAddresses.length > 1) {
            LOGGER.debug("Multiple MongoDb server addresses are defined. Ignoring port [{}], "
                    + "assuming ports are defined as part of the address", mongo.getPort());
            servers = Arrays.stream(serverAddresses)
                    .filter(StringUtils::isNotBlank)
                    .map(ServerAddress::new)
                    .collect(Collectors.toList());
        } else {
            final int port = mongo.getPort() > 0 ? mongo.getPort() : DEFAULT_PORT;
            LOGGER.debug("Found single MongoDb server address [{}] using port [{}]" + mongo.getHost(), port);
            final ServerAddress addr = new ServerAddress(mongo.getHost(), port);
            servers.add(addr);
        }

        final MongoCredential credential = buildMongoCredential(mongo);
        return new MongoClient(servers, CollectionUtils.wrap(credential), buildMongoDbClientOptions(mongo));
    }

    private Mongo buildMongoDbClient(final String clientUri, final MongoClientOptions clientOptions) {
        final MongoClientURI uri = buildMongoClientURI(clientUri);
        final MongoCredential credential = buildMongoCredential(uri);

        final String hostUri = uri.getHosts().get(0);
        final String[] host = hostUri.split(":");
        final ServerAddress addr = new ServerAddress(host[0], host.length > 1 ? Integer.parseInt(host[1]) : DEFAULT_PORT);
        return new MongoClient(addr, Collections.singletonList(credential), clientOptions);
    }

    private MongoCredential buildMongoCredential(final MongoClientURI uri) {
        return MongoCredential.createCredential(uri.getUsername(), uri.getDatabase(), uri.getPassword());
    }

    private MongoCredential buildMongoCredential(final BaseMongoDbProperties mongo) {
        final String dbName = StringUtils.defaultIfBlank(mongo.getAuthenticationDatabaseName(), mongo.getDatabaseName());
        return MongoCredential.createCredential(mongo.getUserId(), dbName, mongo.getPassword().toCharArray());
    }

    private MongoClientURI buildMongoClientURI(final String clientUri) {
        return new MongoClientURI(clientUri);
    }
}
