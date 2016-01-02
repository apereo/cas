package org.jasig.cas.authentication;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import org.jasig.cas.authentication.handler.PasswordEncoder;
import org.jasig.cas.authentication.handler.PrincipalNameTransformer;
import org.jasig.cas.integration.pac4j.authentication.handler.support.UsernamePasswordWrapperAuthenticationHandler;
import org.pac4j.http.credentials.authenticator.Authenticator;
import org.pac4j.http.credentials.password.NopPasswordEncoder;
import org.pac4j.mongo.credentials.authenticator.MongoAuthenticator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

/**
 * An authentication handler to verify credentials against a MongoDb instance.
 * @author Misagh Moayyed
 * @since 4.2.0
 */
@Repository("mongoAuthenticationHandler")
public final class MongoAuthenticationHandler extends UsernamePasswordWrapperAuthenticationHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(MongoAuthenticationHandler.class);

    @Value("${cas.authn.mongo.collection.name:users}")
    private String collectionName;

    @Value("${cas.authn.mongo.db.name:cas}")
    private String databaseName;

    @Value("${cas.authn.mongo.db.host:}")
    private String mongoHostUri;

    @Value("${cas.authn.mongo.attributes:}")
    private String attributes;

    @Value("${cas.authn.mongo.username.attribute:username}")
    private String usernameAttribute;

    @Value("${cas.authn.mongo.password.attribute:password}")
    private String passwordAttribute;

    @Autowired(required=false)
    @Qualifier("mongoPac4jPasswordEncoder")
    private org.pac4j.http.credentials.password.PasswordEncoder mongoPasswordEncoder = new NopPasswordEncoder();

    @Override
    protected Authenticator getAuthenticator(final Credential credential) {
        final MongoClientURI uri = new MongoClientURI(this.mongoHostUri);
        final MongoClient client = new MongoClient(uri);
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

    @Autowired(required = false)
    @Override
    public void setPasswordEncoder(@Qualifier("mongoPasswordEncoder")
                                       final PasswordEncoder passwordEncoder) {
        if (passwordEncoder != null) {
            super.setPasswordEncoder(passwordEncoder);
        }
    }

    @Autowired(required=false)
    @Override
    public void setPrincipalNameTransformer(@Qualifier("mongoPrincipalNameTransformer")
                                                final PrincipalNameTransformer principalNameTransformer) {
        if (principalNameTransformer != null) {
            super.setPrincipalNameTransformer(principalNameTransformer);
        }
    }

    public void setMongoHostUri(final String mongoHostUri) {
        this.mongoHostUri = mongoHostUri;
    }

    public void setCollectionName(final String collectionName) {
        this.collectionName = collectionName;
    }

    public void setDatabaseName(final String databaseName) {
        this.databaseName = databaseName;
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

    public void setMongoPasswordEncoder(final org.pac4j.http.credentials.password.PasswordEncoder mongoPasswordEncoder) {
        this.mongoPasswordEncoder = mongoPasswordEncoder;
    }
}
