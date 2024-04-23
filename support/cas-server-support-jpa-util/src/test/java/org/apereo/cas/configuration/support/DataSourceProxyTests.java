package org.apereo.cas.configuration.support;

import org.apereo.cas.configuration.model.support.jpa.serviceregistry.JpaServiceRegistryProperties;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import javax.naming.directory.InitialDirContext;
import java.util.Hashtable;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DataSourceProxyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("Hibernate")
class DataSourceProxyTests {

    @Test
    @SuppressWarnings("JdkObsolete")
    void verifyProxySource() throws Throwable {
        val ds = JpaBeans.newDataSource("org.hsqldb.jdbcDriver", "sa", StringUtils.EMPTY, "jdbc:hsqldb:mem:cas");
        val environment = new Hashtable<>();
        environment.put("java:comp/env/jdbc/MyDS", ds);
        val ctx = new InitialDirContext(environment);
        val props = new JpaServiceRegistryProperties();
        props.setDataSourceName("java:comp/env/jdbc/MyDS");
        assertNotNull(JpaBeans.newDataSource(props));

        props.setDataSourceName("bad-name");
        assertNotNull(JpaBeans.newDataSource(props));
        ctx.close();
    }

}
