package org.apereo.cas.authentication;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import org.apache.commons.codec.binary.StringUtils;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.integration.pac4j.authentication.handler.support.UsernamePasswordWrapperAuthenticationHandler;
import org.apereo.cas.services.ServicesManager;
import org.pac4j.core.credentials.UsernamePasswordCredentials;
import org.pac4j.core.credentials.authenticator.Authenticator;
import org.pac4j.core.credentials.password.PasswordEncoder;
import org.pac4j.mongo.credentials.authenticator.MongoAuthenticator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    
    private PasswordEncoder mongoPasswordEncoder = new NoOpPasswordEncoder();

    public MongoAuthenticationHandler(final String name, final ServicesManager servicesManager, final PrincipalFactory principalFactory,
                                      final String collectionName, final String mongoHostUri, final String attributes, final String usernameAttribute,
                                      final String passwordAttribute, final PasswordEncoder mongoPasswordEncoder) {
        super(name, servicesManager, principalFactory, null);
        this.collectionName = collectionName;
        this.mongoHostUri = mongoHostUri;
        this.attributes = attributes;
        this.usernameAttribute = usernameAttribute;
        this.passwordAttribute = passwordAttribute;
        this.mongoPasswordEncoder = mongoPasswordEncoder;
    }

    @Override
    protected Authenticator<UsernamePasswordCredentials> getAuthenticator(final Credential credential) {
        final MongoClientURI uri = new MongoClientURI(this.mongoHostUri);
        final MongoClient client = new MongoClient(uri);
        LOGGER.info("Connected to MongoDb instance @ [{}] using database [{}]",
                uri.getHosts(), uri.getDatabase());

        final MongoAuthenticator mongoAuthenticator = new MongoAuthenticator(client, this.attributes);
        mongoAuthenticator.setUsersCollection(this.collectionName);
        mongoAuthenticator.setUsersDatabase(uri.getDatabase());
        mongoAuthenticator.setUsernameAttribute(this.usernameAttribute);
        mongoAuthenticator.setPasswordAttribute(this.passwordAttribute);
        mongoAuthenticator.setPasswordEncoder(this.mongoPasswordEncoder);
        return mongoAuthenticator;
    }
    
    private static class NoOpPasswordEncoder implements PasswordEncoder {
        @Override
        public String encode(final String s) {
            LOGGER.debug("No password encoding shall take place by CAS");
            return s;
        }

        @Override
        public boolean matches(final String s, final String s1) {
            return StringUtils.equals(s, s1);
        }
    }
}
