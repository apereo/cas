package org.apereo.cas.configuration.model.support.mongo;

/**
 * This is {@link MongoAuthenticationProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */

public class MongoAuthenticationProperties {

    private String collectionName = "users";
    private String mongoHostUri = "mongodb://uri";
    private String attributes;
    private String usernameAttribute = "username";
    private String passwordAttribute = "password";

    public String getCollectionName() {
        return collectionName;
    }

    public void setCollectionName(final String collectionName) {
        this.collectionName = collectionName;
    }
    
    public String getMongoHostUri() {
        return mongoHostUri;
    }

    public void setMongoHostUri(final String mongoHostUri) {
        this.mongoHostUri = mongoHostUri;
    }

    public String getAttributes() {
        return attributes;
    }

    public void setAttributes(final String attributes) {
        this.attributes = attributes;
    }

    public String getUsernameAttribute() {
        return usernameAttribute;
    }

    public void setUsernameAttribute(final String usernameAttribute) {
        this.usernameAttribute = usernameAttribute;
    }

    public String getPasswordAttribute() {
        return passwordAttribute;
    }

    public void setPasswordAttribute(final String passwordAttribute) {
        this.passwordAttribute = passwordAttribute;
    }
}
