package org.apereo.cas.authentication;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import org.apereo.cas.integration.pac4j.authentication.handler.support.UsernamePasswordWrapperAuthenticationHandler;
import org.pac4j.core.credentials.UsernamePasswordCredentials;
import org.pac4j.core.credentials.authenticator.Authenticator;
import org.pac4j.core.credentials.password.NopPasswordEncoder;
import org.pac4j.core.credentials.password.PasswordEncoder;
import org.pac4j.mongo.credentials.authenticator.MongoAuthenticator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * An authentication handler to verify credentials against a MongoDb instance.
 * @author Misagh Moayyed
 * @since 4.2.0
 */
public class MongoAuthenticationHandler extends UsernamePasswordWrapperAuthenticationHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(MongoAuthenticationHandler.class);
    
    private String collectionName;
    private String mongoHostUri;
    private String attributes;
    private String usernameAttribute;
    private String passwordAttribute;
    private MongoClientURI uri;
    private MongoClient client;

    private PasswordEncoder mongoPasswordEncoder = new NopPasswordEncoder();

    @PostConstruct
    private void createMongoClient() {
        uri = new MongoClientURI(this.mongoHostUri);
        client = new MongoClient(uri);
    }

    @PreDestroy
    private void cleanupResources() {
        client.close();
    }

    @Override
    protected Authenticator<UsernamePasswordCredentials> getAuthenticator(final Credential credential) {
        LOGGER.info("Connected to MongoDb instance @ {} using database [{}]",
                uri.getHosts(), uri.getDatabase());

        final MongoAuthenticator mongoAuthenticator = new MongoAuthenticator(client, this.attributes);
        mongoAuthenticator.setUsersCollection(this.collectionName);
        mongoAuthenticator.setUsersDatabase(uri.getDatabase());
        mongoAuthenticator.setUsernameAttribute(this.usernameAttribute);
        mongoAuthenticator.setPasswordAttribute(this.passwordAttribute);
        mongoAuthenticator.setPasswordEncoder(this.mongoPasswordEncoder);
        return mongoAuthenticator;
    }
    
    public void setMongoHostUri(final String mongoHostUri) {
        this.mongoHostUri = mongoHostUri;
    }

    public void setCollectionName(final String collectionName) {
        this.collectionName = collectionName;
    }
    
    public void setAttributes(final String attributes) {
        this.attributes = attributes;
    }

    public void setUsernameAttribute(final String usernameAttribute) {
        this.usernameAttribute = usernameAttribute;
    }

    public void setPasswordAttribute(final String passwordAttribute) {
        this.passwordAttribute = passwordAttribute;
    }

    public void setMongoPasswordEncoder(final PasswordEncoder mongoPasswordEncoder) {
        this.mongoPasswordEncoder = mongoPasswordEncoder;
    }
}
