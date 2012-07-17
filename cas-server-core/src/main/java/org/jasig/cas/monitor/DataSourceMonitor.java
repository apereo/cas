/*
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
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
package org.jasig.cas.monitor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.sql.DataSource;
import javax.validation.constraints.NotNull;

/**
 * Monitors a data source that describes a single connection or connection pool to a database.
 *
 * @author Middleware Services
 * @version $Revision: $
 */
public class DataSourceMonitor extends AbstractPoolMonitor {

    @NotNull
    private final DataSource dataSource;

    @NotNull
    private String validationQuery;


    /**
     * Creates a new instance that monitors the given data source.
     *
     * @param dataSource Data source to monitor.
     */
    public DataSourceMonitor(final DataSource dataSource) {
        this.dataSource = dataSource;
    }


    /**
     * Sets the validation query used to monitor the data source. The validation query should return
     * at least one result; otherwise results are ignored.
     *
     * @param query Validation query that should be as efficient as possible.
     */
    public void setValidationQuery(final String query) {
        this.validationQuery = query;
    }


    @Override
    protected StatusCode checkPool() throws Exception {
        final Connection connection = dataSource.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        StatusCode result = StatusCode.WARN;
        try {
            ps = dataSource.getConnection().prepareStatement(validationQuery);
            rs = ps.executeQuery();
            if (rs.next()) {
                result = StatusCode.OK;
            }
        } finally {
            if (rs != null) {
                rs.close();
            }
            if (ps != null) {
                ps.close();
            }
            connection.close();
        }
        return result;
    }

    @Override
    protected int getIdleCount() {
        return PoolStatus.UNKNOWN_COUNT;
    }

    @Override
    protected int getActiveCount() {
        return PoolStatus.UNKNOWN_COUNT;
    }
}
