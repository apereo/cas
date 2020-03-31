package org.apereo.cas.configuration.model.support.surrogate;

import org.apereo.cas.configuration.model.core.authentication.PersonDirectoryPrincipalResolverProperties;
import org.apereo.cas.configuration.model.support.couchdb.BaseCouchDbProperties;
import org.apereo.cas.configuration.model.support.email.EmailProperties;
import org.apereo.cas.configuration.model.support.jpa.AbstractJpaProperties;
import org.apereo.cas.configuration.model.support.ldap.AbstractLdapSearchProperties;
import org.apereo.cas.configuration.model.support.sms.SmsProperties;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;
import org.apereo.cas.configuration.support.RestEndpointProperties;
import org.apereo.cas.configuration.support.SpringResourceProperties;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This is {@link SurrogateAuthenticationProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@RequiresModule(name = "cas-server-support-surrogate-webflow")
@Getter
@Setter
@Accessors(chain = true)
public class SurrogateAuthenticationProperties implements Serializable {

    private static final long serialVersionUID = -2088813217398883623L;

    /**
     * The separator character used to distinguish between the surrogate account and the admin account.
     */
    private String separator = "+";

    /**
     * Locate surrogate accounts via CouchDB.
     */
    private CouchDb couchDb = new CouchDb();

    /**
     * Locate surrogate accounts via CAS configuration, hardcoded as properties.
     */
    private Simple simple = new Simple();

    /**
     * Locate surrogate accounts via a JSON resource.
     */
    private Json json = new Json();

    /**
     * Locate surrogate accounts via an LDAP server.
     */
    private Ldap ldap = new Ldap();

    /**
     * Locate surrogate accounts via a JDBC resource.
     */
    private Jdbc jdbc = new Jdbc();

    /**
     * Locate surrogate accounts via a REST resource.
     */
    private Rest rest = new Rest();

    /**
     * Settings related to tickets issued for surrogate session, their expiration policy, etc.
     */
    private Tgt tgt = new Tgt();

    /**
     * Principal construction settings.
     */
    @NestedConfigurationProperty
    private PersonDirectoryPrincipalResolverProperties principal = new PersonDirectoryPrincipalResolverProperties();

    /**
     * Email settings for notifications.
     */
    @NestedConfigurationProperty
    private EmailProperties mail = new EmailProperties();

    /**
     * SMS settings for notifications.
     */
    @NestedConfigurationProperty
    private SmsProperties sms = new SmsProperties();

    @RequiresModule(name = "cas-server-support-surrogate-webflow")
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class Simple implements Serializable {

        private static final long serialVersionUID = 16938920863432222L;

        /**
         * Define the list of accounts that are allowed to impersonate.
         * This is done in a key-value structure where the key is the admin user
         * and the value is a comma-separated list of identifiers that can be
         * impersonated by the admin-user.
         */
        private Map<String, String> surrogates = new LinkedHashMap<>(2);
    }

    @RequiresModule(name = "cas-server-support-surrogate-authentication-couchdb")
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class CouchDb extends BaseCouchDbProperties {

        private static final long serialVersionUID = 8378399979559955402L;

        /**
         * Use user profiles instead of surrogate/principal pairs. If +true+, a list of of principals the user is an authorized surrogate of is stored in the
         * user profile in CouchDb. Most useful with CouchDb authentication or AUP.
         */
        private boolean profileBased;

        /**
         * Attribute with list of principals the user may surrogate when user surrogates are stored in user profiles.
         */
        private String surrogatePrincipalsAttribute = "surrogateFor";

        public CouchDb() {
            this.setDbName("surrogates");
        }
    }

    @RequiresModule(name = "cas-server-support-surrogate-webflow")
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class Json extends SpringResourceProperties {

        private static final long serialVersionUID = 3599367681439517829L;
    }

    @RequiresModule(name = "cas-server-support-surrogate-authentication-rest")
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class Rest extends RestEndpointProperties {

        private static final long serialVersionUID = 8152273816132989085L;
    }

    @RequiresModule(name = "cas-server-support-surrogate-authentication-ldap")
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class Ldap extends AbstractLdapSearchProperties {

        private static final long serialVersionUID = -3848837302921751926L;

        /**
         * LDAP search filter used to locate the surrogate account.
         */
        private String surrogateSearchFilter;

        /**
         * Attribute that must be found on the LDAP entry linked to the admin user
         * that tags the account as authorized for impersonation.
         */
        @RequiredProperty
        private String memberAttributeName;

        /**
         * A pattern that is matched against the attribute value of the admin user,
         * that allows for further authorization of the admin user and accounts qualified for impersonation.
         * The regular expression pattern is expected to contain at least a single group whose value on a
         * successful match indicates the qualified impersonated user by admin.
         */
        private String memberAttributeValueRegex;
    }

    @RequiresModule(name = "cas-server-support-surrogate-authentication")
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class Tgt implements Serializable {

        private static final long serialVersionUID = 2077366413438267330L;

        /**
         * Timeout in seconds to kill the surrogate session and consider tickets expired.
         */
        private long timeToKillInSeconds = 1_800;
    }

    @RequiresModule(name = "cas-server-support-surrogate-authentication-jdbc")
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class Jdbc extends AbstractJpaProperties {

        private static final long serialVersionUID = 8970195444880123796L;

        /**
         * Surrogate query to use to determine whether an admin user can impersonate another user.
         * The query must return an integer count of greater than zero.
         */
        @RequiredProperty
        private String surrogateSearchQuery = "SELECT COUNT(*) FROM surrogate WHERE username=?";

        /**
         * SQL query to use in order to retrieve the list of qualified accounts for impersonation for a given admin user.
         */
        @RequiredProperty
        private String surrogateAccountQuery = "SELECT surrogate_user AS surrogateAccount FROM surrogate WHERE username=?";
    }
}
