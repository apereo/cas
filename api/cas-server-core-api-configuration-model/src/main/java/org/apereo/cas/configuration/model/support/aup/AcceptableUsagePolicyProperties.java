package org.apereo.cas.configuration.model.support.aup;

import org.apereo.cas.configuration.model.support.couchdb.BaseAsynchronousCouchDbProperties;
import org.apereo.cas.configuration.model.support.jpa.AbstractJpaProperties;
import org.apereo.cas.configuration.model.support.mongo.SingleCollectionMongoDbProperties;
import org.apereo.cas.configuration.model.support.redis.BaseRedisProperties;
import org.apereo.cas.configuration.support.RequiresModule;
import org.apereo.cas.configuration.support.RestEndpointProperties;
import org.apereo.cas.configuration.support.SpringResourceProperties;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * This is {@link AcceptableUsagePolicyProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-support-aup-webflow")
@Getter
@Setter
@Accessors(chain = true)
public class AcceptableUsagePolicyProperties implements Serializable {

    private static final long serialVersionUID = -7703477581675908899L;

    /**
     * Control AUP via LDAP.
     */
    private List<LdapAcceptableUsagePolicyProperties> ldap = new ArrayList<>();

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
     * Control AUP via a MongoDb database resource.
     */
    private MongoDb mongo = new MongoDb();

    /**
     * Control AUP a Groovy script.
     */
    private Groovy groovy = new Groovy();

    /**
     * Control AUP via Redis.
     */
    private Redis redis = new Redis();

    /**
     * Control AUP backed by runtime's memory.
     */
    private InMemory inMemory = new InMemory();

    /**
     * AUP enabled allows AUP to be turned off on startup.
     */
    private boolean enabled = true;

    /**
     * AUP attribute to choose in order to determine whether policy
     * has been accepted or not. The attribute is expected to contain
     * a boolean value where {@code true} indicates policy has been
     * accepted and {@code false} indicates otherwise.
     * The attribute is fetched for the principal from configured sources
     * and compared for the right match to determine policy status.
     * If the attribute is not found, the policy status is considered as denied.
     */
    private String aupAttributeName = "aupAccepted";

    /**
     * AUP attribute to choose whose single value dictates
     * how CAS should fetch the policy terms from
     * the relevant message bundles.
     */
    private String aupPolicyTermsAttributeName;
    
    @RequiresModule(name = "cas-server-support-aup-couchdb")
    @Accessors(chain = true)
    @Getter
    @Setter
    public static class CouchDb extends BaseAsynchronousCouchDbProperties {

        private static final long serialVersionUID = 1323894615409106853L;

        public CouchDb() {
            setDbName("acceptable_usage_policy");
        }
    }

    @RequiresModule(name = "cas-server-support-aup-mongo")
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class MongoDb extends SingleCollectionMongoDbProperties {

        private static final long serialVersionUID = -1918436901491275547L;

        public MongoDb() {
            setCollection("MongoDbCasAUPRepository");
        }
    }

    @RequiresModule(name = "cas-server-support-aup-jdbc")
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class Jdbc extends AbstractJpaProperties {

        private static final long serialVersionUID = -1325011278378393385L;

        /**
         * The table name in the database that holds the AUP attribute to update for the user.
         */
        private String tableName;

        /**
         * The column to store the AUP attribute. May differ from the profile attribute defined by {@link #aupAttributeName}.
         * SQL query can be further customized by setting {@link #sqlUpdate}.
         */
        private String aupColumn;

        /**
         * The column to identify the principal.
         * SQL query can be further customized by setting {@link #sqlUpdate}.
         */
        private String principalIdColumn = "username";

        /**
         * The profile attribute to extract the value for the {@link #principalIdColumn} used in the WHERE clause
         * of {@link #sqlUpdate}. If empty, the principal ID will be used.
         */
        private String principalIdAttribute;

        /**
         * The query template to update the AUP attribute.
         * %s placeholders represent {@link #tableName}, {@link #aupColumn}, {@link #principalIdColumn} settings.
         */
        private String sqlUpdate = "UPDATE %s SET %s=true WHERE %s=?";
    }

    @RequiresModule(name = "cas-server-support-aup-rest")
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class Rest extends RestEndpointProperties {
        private static final long serialVersionUID = -8102345678378393382L;
    }

    @RequiresModule(name = "cas-server-support-aup-core", automated = true)
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class Groovy extends SpringResourceProperties {
        private static final long serialVersionUID = 9164227843747126083L;
    }

    @RequiresModule(name = "cas-server-support-aup-core", automated = true)
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class InMemory implements Serializable {
        private static final long serialVersionUID = 8164227843747126083L;

        /**
         * Scope of map where the aup selection is stored.
         */
        private Scope scope = Scope.GLOBAL;

        /**
         * Scope options for the default aup repository can store flag indicating acceptance.
         * Scope refers to duration that acceptance is kept.
         * Current options are global on the particular server (not replicated across CAS servers)
         * and once per authentication via credentials (not authentication events via TGT).
         */
        public enum Scope {
            /**
             * Store in global in-memory map (for life of server).
             */
            GLOBAL,

            /**
             * Store aup acceptance such that user is prompted when
             * they authenticate via credentials.
             */
            AUTHENTICATION
        }
    }

    @RequiresModule(name = "cas-server-support-aup-redis")
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class Redis extends BaseRedisProperties {
        private static final long serialVersionUID = -2147683393318585262L;
    }
}
