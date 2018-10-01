package org.apereo.cas.configuration.model.support.aup;

import org.apereo.cas.configuration.model.support.couchdb.BaseAsynchronousCouchDbProperties;
import org.apereo.cas.configuration.model.support.jpa.AbstractJpaProperties;
import org.apereo.cas.configuration.model.support.ldap.AbstractLdapSearchProperties;
import org.apereo.cas.configuration.model.support.mongo.SingleCollectionMongoDbProperties;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;
import org.apereo.cas.configuration.support.RestEndpointProperties;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * This is {@link AcceptableUsagePolicyProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-support-aup-webflow")
@Getter
@Setter
public class AcceptableUsagePolicyProperties implements Serializable {

    private static final long serialVersionUID = -7703477581675908899L;

    /**
     * Control AUP via LDAP.
     */
    private Ldap ldap = new Ldap();

    /**
     * Control AUP via Redis.
     */
    private Jdbc jdbc = new Jdbc();

    /**
     * Control AUP via Redis.
     */
    private Rest rest = new Rest();

    /**
     * Control AUP via CouchDb.
     */
    private CouchDb couchDb = new CouchDb();

    /**
     * Keep consent decisions stored via a MongoDb database resource.
     */
    private MongoDb mongo = new MongoDb();

    /**
     * AUP enabled allows AUP to be turned off on startup.
     */
    @RequiredProperty
    private boolean enabled = true;

    /**
     * AUP attribute to choose in order to determine whether policy
     * has been accepted or not.
     */
    @RequiredProperty
    private String aupAttributeName = "aupAccepted";

    @RequiresModule(name = "cas-server-support-aup-couchdb")
    public static class CouchDb extends BaseAsynchronousCouchDbProperties {

        private static final long serialVersionUID = 1323894615409106853L;

        public CouchDb() {
            setDbName("acceptable_usage_policy");
        }
    }

    @RequiresModule(name = "cas-server-support-aup-mongo")
    @Getter
    @Setter
    public static class MongoDb extends SingleCollectionMongoDbProperties {

        private static final long serialVersionUID = -1918436901491275547L;

        public MongoDb() {
            setCollection("MongoDbCasAUPRepository");
        }
    }

    @RequiresModule(name = "cas-server-support-aup-jdbc")
    @Getter
    @Setter
    public static class Jdbc extends AbstractJpaProperties {

        private static final long serialVersionUID = -1325011278378393385L;

        /**
         * The table name in the database that holds the AUP attribute to update for the user.
         */
        private String tableName;
    }

    @RequiresModule(name = "cas-server-support-aup-rest")
    @Getter
    @Setter
    public static class Rest extends RestEndpointProperties {
        private static final long serialVersionUID = -8102345678378393382L;
    }

    @RequiresModule(name = "cas-server-support-aup-ldap")
    @Getter
    @Setter
    public static class Ldap extends AbstractLdapSearchProperties {

        private static final long serialVersionUID = -7991011278378393382L;
    }
}
