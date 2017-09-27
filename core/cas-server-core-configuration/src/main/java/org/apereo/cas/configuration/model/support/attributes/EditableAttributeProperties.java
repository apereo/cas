package org.apereo.cas.configuration.model.support.attributes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apereo.cas.configuration.model.support.jpa.AbstractJpaProperties;
import org.apereo.cas.configuration.model.support.ldap.AbstractLdapProperties;
import org.apereo.cas.configuration.support.RestEndpointProperties;

/**
 * This is {@link EditableAttributeProperties}.
 *
 * @author Marcus Watkins
 * @since 5.2.0
 */

public class EditableAttributeProperties implements Serializable {

    private static final long serialVersionUID = -4654740038123600286L;

    private List<EditableAttribute> attributes = new ArrayList<>();

    public List<EditableAttribute> getAttributes() {
        return attributes;
    }

    public void setAttributes(final List<EditableAttribute> attributes) {
        this.attributes = attributes;
    }

    /**
     * Control EditableAttributes via JDBC.
     */
    private Jdbc jdbc = new Jdbc();

    /**
     * Control EditableAttributes via LDAP.
     */
    private Ldap ldap = new Ldap();

    /**
     * Control EditableAttributes via Rest.
     */
    private Rest rest = new Rest();

    public Rest getRest() {
        return rest;
    }

    public void setRest(final Rest rest) {
        this.rest = rest;
    }

    public Jdbc getJdbc() {
        return jdbc;
    }

    public void setJdbc(final Jdbc redis) {
        this.jdbc = redis;
    }

    public Ldap getLdap() {
        return ldap;
    }

    public void setLdap(final Ldap ldap) {
        this.ldap = ldap;
    }

    public static class Jdbc extends AbstractJpaProperties {
        private static final long serialVersionUID = -1325011278378393385L;

        /**
         * The table name in the database that holds editable attributes to update for the user.
         */
        private String tableName;

        public String getTableName() {
            return tableName;
        }

        public void setTableName(final String tableName) {
            this.tableName = tableName;
        }
    }

    public static class Rest extends RestEndpointProperties {
        private static final long serialVersionUID = -8102345678378393382L;
    }

    public static class Ldap extends AbstractLdapProperties {
        private static final long serialVersionUID = -7991011278378393382L;
        /**
         * Base DN to start the search for user accounts.
         */
        private String baseDn;
        /**
         * Search filter to use.
         * Syntax is {@code cn={user}} or {@code cn={0}}
         */
        private String userFilter;

        public String getBaseDn() {
            return baseDn;
        }

        public void setBaseDn(final String baseDn) {
            this.baseDn = baseDn;
        }

        public String getUserFilter() {
            return userFilter;
        }

        public void setUserFilter(final String userFilter) {
            this.userFilter = userFilter;
        }
    }
    
    public static class EditableAttribute {

        public enum EditableAttributeType {
            /**
             * Attribute should be represented with a text box.
             */
            TEXT,
            /**
             * Attribute should use a select box.
             */
            SELECT,
            /**
             * Attribute should use a password box.
             */
            PASSWORD,
        }
        
        private EditableAttributeType type;
        private String id;
        private String prompt;
        private ArrayList<String> options;
        private String validationRegex = "^";

        public EditableAttributeType getType() {
            return type;
        }

        public void setType(final EditableAttributeType type) {
            this.type = type;
        }

        public String getId() {
            return id;
        }

        public void setId(final String id) {
            this.id = id;
        }

        public String getPrompt() {
            return prompt;
        }

        public void setPrompt(final String prompt) {
            this.prompt = prompt;
        }

        public ArrayList<String> getOptions() {
            return options;
        }

        public void setOptions(final ArrayList<String> options) {
            this.options = options;
        }

        public String getValidationRegex() {
            return validationRegex;
        }

        public void setValidationRegex(final String validationRegex) {
            this.validationRegex = validationRegex;
        }
    }

}
