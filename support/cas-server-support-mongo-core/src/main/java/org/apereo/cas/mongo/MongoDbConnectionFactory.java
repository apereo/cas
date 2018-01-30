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
     * Build mongo template.
     *
     * @param mongo the mongo properties settings
     * @return the mongo template
     */
    public MongoTemplate buildMongoTemplate(final BaseMongoDbProperties mongo) {
        final MongoDbFactory mongoDbFactory = mongoDbFactory(buildMongoDbClient(mongo), mongo);
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
        final String authDbName;

        if (StringUtils.isNotBlank(props.getClientUri())) {
            final MongoClientURI uri = buildMongoClientURI(props.getClientUri(), buildMongoDbClientOptions(props));
            authDbName = uri.getCredentials().getSource();
            dbName = uri.getDatabase();
            LOGGER.debug("Using database [{}] from the connection client URI", dbName);
        } else {
            authDbName = props.getAuthenticationDatabaseName();
            dbName = props.getDatabaseName();
            LOGGER.debug("Using database [{}] from individual settings", dbName);
        }

        if (StringUtils.isBlank(dbName)) {
            LOGGER.error("Database name cannot be undefined. It must be specified as part of the client URI connection string if used, or "
                    + "as an individual setting for the MongoDb connection");
        }

        return new SimpleMongoDbFactory(mongo, dbName, null, authDbName);
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
            return new HashSet<>(0);
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
     * @param mongo the property setttings (including, perhaps, a client uri)
     * @return a bean containing the MongoClientOptions object
     */
    private MongoClientOptionsFactoryBean buildMongoDbClientOptionsFactoryBean(final BaseMongoDbProperties mongo) {
        try {
            final MongoClientOptionsFactoryBean bean1 = new MongoClientOptionsFactoryBean();

            bean1.setWriteConcern(WriteConcern.valueOf(mongo.getWriteConcern()));
            bean1.setHeartbeatConnectTimeout((int) mongo.getTimeout());
            bean1.setHeartbeatSocketTimeout((int) mongo.getTimeout());
            bean1.setMaxConnectionLifeTime(mongo.getConns().getLifetime());
            bean1.setSocketKeepAlive(mongo.isSocketKeepAlive());
            bean1.setMaxConnectionIdleTime((int) mongo.getIdleTimeout());
            bean1.setConnectionsPerHost(mongo.getConns().getPerHost());
            bean1.setSocketTimeout((int) mongo.getTimeout());
            bean1.setConnectTimeout((int) mongo.getTimeout());
            if (StringUtils.isNotBlank(mongo.getReplicaSet())) {
                bean1.setRequiredReplicaSetName(mongo.getReplicaSet());
            }
            bean1.setSsl(mongo.isSslEnabled());
            if (mongo.isSslEnabled()) {
                bean1.setSslSocketFactory((SSLSocketFactory) SSLSocketFactory.getDefault());
            }

            bean1.afterPropertiesSet();

            if (StringUtils.isNotBlank(mongo.getClientUri())) {
                final MongoClientOptionsFactoryBean bean2 = new MongoClientOptionsFactoryBean();

                final MongoClientURI uri = buildMongoClientURI(mongo.getClientUri(), bean1.getObject());
                final MongoClientOptions opts = uri.getOptions();

                bean2.setWriteConcern(opts.getWriteConcern());
                bean2.setHeartbeatConnectTimeout(opts.getHeartbeatConnectTimeout());
                bean2.setHeartbeatSocketTimeout(opts.getHeartbeatSocketTimeout());
                bean2.setMaxConnectionLifeTime(opts.getMaxConnectionLifeTime());
                bean2.setSocketKeepAlive(opts.isSocketKeepAlive());
                bean2.setMaxConnectionIdleTime(opts.getMaxConnectionIdleTime());
                bean2.setConnectionsPerHost(opts.getConnectionsPerHost());
                bean2.setSocketTimeout(opts.getSocketTimeout());
                bean2.setConnectTimeout(opts.getConnectTimeout());
                bean2.setRequiredReplicaSetName(opts.getRequiredReplicaSetName());
                bean2.setSsl(opts.isSslEnabled());

                if (opts.isSslEnabled()) {
                    bean2.setSslSocketFactory((SSLSocketFactory) SSLSocketFactory.getDefault());
                }

                bean2.afterPropertiesSet();
                bean1.destroy();

                return bean2;
            }

            return bean1;
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
        final MongoClientURI uri = buildMongoClientURI(clientUri, clientOptions);
        return new MongoClient(uri);
    }

    private MongoCredential buildMongoCredential(final MongoClientURI uri) {
        return MongoCredential.createCredential(uri.getUsername(), uri.getDatabase(), uri.getPassword());
    }

    private MongoCredential buildMongoCredential(final BaseMongoDbProperties mongo) {
        final String dbName = StringUtils.defaultIfBlank(mongo.getAuthenticationDatabaseName(), mongo.getDatabaseName());
        return MongoCredential.createCredential(mongo.getUserId(), dbName, mongo.getPassword().toCharArray());
    }

    private MongoClientURI buildMongoClientURI(final String clientUri, final MongoClientOptions clientOptions) {
        final MongoClientOptions.Builder builder = new MongoClientOptions.Builder(clientOptions);
        return new MongoClientURI(clientUri, builder);
    }

    private static class ClientUriMongoDbProperties extends BaseMongoDbProperties {
        private static final long serialVersionUID = -9182480568666563805L;

        ClientUriMongoDbProperties(final String clientUri) {
            setClientUri(clientUri);
        }
    }
}
