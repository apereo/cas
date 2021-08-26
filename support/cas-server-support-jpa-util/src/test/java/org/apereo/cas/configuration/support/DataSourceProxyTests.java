package org.apereo.cas.configuration.support;

import org.apereo.cas.configuration.model.support.jpa.serviceregistry.JpaServiceRegistryProperties;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.mock.jndi.SimpleNamingContextBuilder;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DataSourceProxyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("JDBC")
public class DataSourceProxyTests {

    @Test
    public void verifyProxySource() throws Exception {
        val builder = new SimpleNamingContextBuilder();
        val ds = JpaBeans.newDataSource("org.hsqldb.jdbcDriver", "sa", StringUtils.EMPTY, "jdbc:hsqldb:mem:cas");
        builder.bind("java:comp/env/jdbc/MyDS", ds);
        builder.activate();

        val props = new JpaServiceRegistryProperties();
        props.setDataSourceName("java:comp/env/jdbc/MyDS");
        assertNotNull(JpaBeans.newDataSource(props));

        props.setDataSourceName("bad-name");
        assertNotNull(JpaBeans.newDataSource(props));
    }

}
