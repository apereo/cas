/*
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.cas.adaptors.jdbc;

import org.jasig.cas.authentication.HandlerResult;
import org.jasig.cas.authentication.PreventedException;
import org.jasig.cas.authentication.UsernamePasswordCredential;
import org.springframework.jdbc.datasource.DataSourceUtils;

import javax.security.auth.login.FailedLoginException;
import java.security.GeneralSecurityException;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * This class attempts to authenticate the user by opening a connection to the
 * database with the provided username and password. Servers are provided as a
 * Properties class with the key being the URL and the property being the type
 * of database driver needed.
 *
 * @author Scott Battaglia
 * @author Dmitriy Kopylenko
 * @author Marvin S. Addison
 *
 * @since 3.0.0
 */
public class BindModeSearchDatabaseAuthenticationHandler extends AbstractJdbcUsernamePasswordAuthenticationHandler {

    /**
     * {@inheritDoc}
     */
    @Override
    protected final HandlerResult authenticateUsernamePasswordInternal(final UsernamePasswordCredential credential)
            throws GeneralSecurityException, PreventedException {

        try {
            final String username = credential.getUsername();
            final String password = getPasswordEncoder().encode(credential.getPassword());
            final Connection c = this.getDataSource().getConnection(username, password);
            DataSourceUtils.releaseConnection(c, this.getDataSource());
            return createHandlerResult(credential, this.principalFactory.createPrincipal(username), null);
        } catch (final SQLException e) {
            throw new FailedLoginException(e.getMessage());
        } catch (final Exception e) {
            throw new PreventedException("Unexpected SQL connection error", e);
        }
    }
}
