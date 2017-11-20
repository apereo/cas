package org.apereo.cas.configuration.model.support.jdbc;

import org.apereo.cas.configuration.support.RequiresModule;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * This is {@link JdbcAuthenticationProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-support-jdbc-authentication")
public class JdbcAuthenticationProperties implements Serializable {
    private static final long serialVersionUID = 7199786191466526110L;
    /**
     * Settings related to search-mode jdbc authentication.
     * Searches for a user record by querying against a username and password; the user is authenticated if at least one result is found.
     */
    private List<SearchJdbcAuthenticationProperties> search = new ArrayList();
    /**
     * Settings related to query-encode-mode jdbc authentication.
     * A JDBC querying handler that will pull back the password and the private salt value for a user and validate
     * the encoded password using the public salt value. Assumes everything is inside the same database table.
     * Supports settings for number of iterations as well as private salt.
     * This password encoding method combines the private Salt and the public salt which it prepends to the password
     * before hashing. If multiple iterations
     * are used, the byte code hash of the first iteration is rehashed without the salt values. The final hash
     * is converted to hex before comparing it to the database value.
     */
    private List<QueryEncodeJdbcAuthenticationProperties> encode = new ArrayList();
    /**
     * Settings related to query-mode jdbc authentication.
     * Authenticates a user by comparing the user password
     * (which can be encoded with a password encoder) against the password on record determined by a configurable database query.
     */
    private List<QueryJdbcAuthenticationProperties> query = new ArrayList();
    /**
     * Settings related to bind-mode jdbc authentication.
     * Authenticates a user by attempting to create a database connection using the username and (hashed) password.
     */
    private List<BindJdbcAuthenticationProperties> bind = new ArrayList();

    public List<SearchJdbcAuthenticationProperties> getSearch() {
        return search;
    }

    public void setSearch(final List<SearchJdbcAuthenticationProperties> search) {
        this.search = search;
    }

    public List<QueryEncodeJdbcAuthenticationProperties> getEncode() {
        return encode;
    }

    public void setEncode(final List<QueryEncodeJdbcAuthenticationProperties> encode) {
        this.encode = encode;
    }

    public List<QueryJdbcAuthenticationProperties> getQuery() {
        return query;
    }

    public void setQuery(final List<QueryJdbcAuthenticationProperties> query) {
        this.query = query;
    }

    public List<BindJdbcAuthenticationProperties> getBind() {
        return bind;
    }

    public void setBind(final List<BindJdbcAuthenticationProperties> bind) {
        this.bind = bind;
    }
}
