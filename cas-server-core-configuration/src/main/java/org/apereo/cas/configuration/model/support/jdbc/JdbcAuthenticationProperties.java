package org.apereo.cas.configuration.model.support.jdbc;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * This is {@link JdbcAuthenticationProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */

public class JdbcAuthenticationProperties {
    private Search search = new Search();
    private Encode encode = new Encode();
    private Query query = new Query();

    public Query getQuery() {
        return query;
    }

    public void setQuery(final Query query) {
        this.query = query;
    }

    public Encode getEncode() {
        return encode;
    }

    public void setEncode(final Encode encode) {
        this.encode = encode;
    }

    public Search getSearch() {
        return search;
    }

    public void setSearch(final Search search) {
        this.search = search;
    }

    public static class Query {
        private String sql;

        public String getSql() {
            return sql;
        }

        public void setSql(final String sql) {
            this.sql = sql;
        }
    }
    public static class Search {
        private String fieldUser;

        private String fieldPassword;

        private String tableUsers;

        public String getFieldUser() {
            return fieldUser;
        }

        public void setFieldUser(final String fieldUser) {
            this.fieldUser = fieldUser;
        }

        public String getFieldPassword() {
            return fieldPassword;
        }

        public void setFieldPassword(final String fieldPassword) {
            this.fieldPassword = fieldPassword;
        }

        public String getTableUsers() {
            return tableUsers;
        }

        public void setTableUsers(final String tableUsers) {
            this.tableUsers = tableUsers;
        }
    }
    
    public static class Encode {
        private String algorithmName;
        private String sql;
        private String passwordFieldName = "password";
        private String saltFieldName = "salt";
        private String numberOfIterationsFieldName = "numIterations";
        private long numberOfIterations;
        private String staticSalt;

        public String getAlgorithmName() {
            return algorithmName;
        }

        public void setAlgorithmName(final String algorithmName) {
            this.algorithmName = algorithmName;
        }

        public String getSql() {
            return sql;
        }

        public void setSql(final String sql) {
            this.sql = sql;
        }

        public String getPasswordFieldName() {
            return passwordFieldName;
        }

        public void setPasswordFieldName(final String passwordFieldName) {
            this.passwordFieldName = passwordFieldName;
        }

        public String getSaltFieldName() {
            return saltFieldName;
        }

        public void setSaltFieldName(final String saltFieldName) {
            this.saltFieldName = saltFieldName;
        }

        public String getNumberOfIterationsFieldName() {
            return numberOfIterationsFieldName;
        }

        public void setNumberOfIterationsFieldName(final String numberOfIterationsFieldName) {
            this.numberOfIterationsFieldName = numberOfIterationsFieldName;
        }

        public long getNumberOfIterations() {
            return numberOfIterations;
        }

        public void setNumberOfIterations(final long numberOfIterations) {
            this.numberOfIterations = numberOfIterations;
        }

        public String getStaticSalt() {
            return staticSalt;
        }

        public void setStaticSalt(final String staticSalt) {
            this.staticSalt = staticSalt;
        }
    }
}
