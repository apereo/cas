package org.apereo.cas.heimdall;

import module java.base;
import module java.sql;
import org.apereo.cas.configuration.support.CloseableDataSource;
import org.apereo.cas.configuration.support.JpaBeans;
import org.apereo.cas.heimdall.authorizer.resource.policy.JdbcAuthorizationPolicy;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Cleanup;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.GenericApplicationContext;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link JdbcAuthorizationPolicyDataSourceTests}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@Tag("Authorization")
class JdbcAuthorizationPolicyDataSourceTests {

    static JdbcAuthorizationPolicy newPolicy(final String url, final GenericApplicationContext applicationContext) {
        return new JdbcAuthorizationPolicy() {
            @Serial
            private static final long serialVersionUID = -1184812932810291912L;

            @Override
            protected GenericApplicationContext resolveApplicationContext() {
                return applicationContext;
            }
        }.setUrl(url).setUsername("sa").setPassword(StringUtils.EMPTY);
    }

    @Test
    void verifyPoolIsRegisteredSharedAndClosed() throws Throwable {
        val applicationContext = new GenericApplicationContext();
        applicationContext.refresh();
        val url = "jdbc:hsqldb:mem:" + UUID.randomUUID();
        val first = newPolicy(url, applicationContext).buildJdbcTemplate().getJdbcTemplate().getDataSource();
        val second = newPolicy(url, applicationContext).buildJdbcTemplate().getJdbcTemplate().getDataSource();
        assertSame(first, second);

        val names = applicationContext.getBeanNamesForType(DataSource.class);
        assertEquals(1, names.length);
        assertTrue(names[0].startsWith("heimdallJdbcDataSource-"));
        assertFalse(names[0].contains(url));

        val pool = (HikariDataSource) ((CloseableDataSource) first).getTargetDataSource();
        assertEquals(0, pool.getMinimumIdle());
        applicationContext.close();
        assertTrue(pool.isClosed());
    }

    @Test
    void verifyNamedDataSourceIsUsed() throws Throwable {
        @Cleanup
        val applicationContext = new GenericApplicationContext();
        applicationContext.refresh();
        val url = "jdbc:hsqldb:mem:" + UUID.randomUUID();
        val dataSource = JpaBeans.newDataSource("org.hsqldb.jdbcDriver", "sa", StringUtils.EMPTY, url);
        applicationContext.registerBean("heimdallCustomDataSource", DataSource.class, () -> dataSource);
        val policy = newPolicy(url, applicationContext).setDataSourceName("heimdallCustomDataSource");
        assertSame(dataSource, policy.buildJdbcTemplate().getJdbcTemplate().getDataSource());
        assertEquals(1, applicationContext.getBeanNamesForType(DataSource.class).length);
    }

    @Test
    void verifyQueryTimeout() throws Throwable {
        @Cleanup
        val applicationContext = new GenericApplicationContext();
        applicationContext.refresh();
        val url = "jdbc:hsqldb:mem:" + UUID.randomUUID();
        assertEquals(5, newPolicy(url, applicationContext).buildJdbcTemplate().getJdbcTemplate().getQueryTimeout());
        assertEquals(30, newPolicy(url, applicationContext).setQueryTimeout("PT30S").buildJdbcTemplate().getJdbcTemplate().getQueryTimeout());
        assertEquals(1, newPolicy(url, applicationContext).setQueryTimeout("PT0.2S").buildJdbcTemplate().getJdbcTemplate().getQueryTimeout());
        assertEquals(0, newPolicy(url, applicationContext).setQueryTimeout("0").buildJdbcTemplate().getJdbcTemplate().getQueryTimeout());
        assertEquals(0, newPolicy(url, applicationContext).setQueryTimeout(null).buildJdbcTemplate().getJdbcTemplate().getQueryTimeout());
        assertEquals(0, newPolicy(url, applicationContext).setQueryTimeout("INFINITE").buildJdbcTemplate().getJdbcTemplate().getQueryTimeout());
    }

    @Test
    void verifyPoolWithoutApplicationContext() throws Throwable {
        val template = newPolicy("jdbc:hsqldb:mem:" + UUID.randomUUID(), null).buildJdbcTemplate();
        @Cleanup
        val dataSource = (CloseableDataSource) template.getJdbcTemplate().getDataSource();
        assertNotNull(dataSource);
        assertEquals(1, template.getJdbcTemplate().queryForObject("VALUES (1)", Integer.class));
    }
}
