package org.apereo.cas.jpa;

import org.apereo.cas.util.scripting.ScriptingUtils;
import org.apereo.cas.util.spring.ApplicationContextProvider;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;
import org.springframework.boot.orm.jpa.hibernate.SpringPhysicalNamingStrategy;

/**
 * This is {@link CasHibernatePhysicalNamingStrategy}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Slf4j
public class CasHibernatePhysicalNamingStrategy extends SpringPhysicalNamingStrategy {
    @Override
    public Identifier toPhysicalTableName(final Identifier name, final JdbcEnvironment jdbcEnvironment) {
        val propsResult = ApplicationContextProvider.getCasConfigurationProperties();
        if (propsResult.isEmpty()) {
            LOGGER.error("Could not load configuration settings. CAS application context may not have initialized correctly.");
            return super.toPhysicalTableName(name, jdbcEnvironment);
        }
        val tableName = name.getCanonicalName();
        val casProperties = propsResult.get();
        val tableNames = casProperties.getJdbc().getPhysicalTableNames();
        if (tableNames.isEmpty()) {
            LOGGER.trace("No configured table names are defined to map [{}]", tableName);
            return super.toPhysicalTableName(name, jdbcEnvironment);
        }

        LOGGER.trace("Locating physical table name for [{}] based on configured table names [{}]", tableName, tableNames);
        if (tableNames.containsKey(tableName)) {
            val physicalName = tableNames.get(tableName);
            if (ScriptingUtils.isExternalGroovyScript(physicalName)) {
                LOGGER.trace("Executing script [{}] to determine physical table name for [{}]", physicalName, tableName);
                val scriptResource = ApplicationContextProvider.getResourceLoader().getResource(physicalName);
                val args = new Object[]{name, jdbcEnvironment, ApplicationContextProvider.getApplicationContext(), LOGGER};
                val identifier = ScriptingUtils.executeGroovyScript(scriptResource, args, Identifier.class, true);
                LOGGER.trace("Determine table physical name from script [{}] to be [{}]", scriptResource, identifier);
                return identifier;
            }
            LOGGER.trace("Located physical table name [{}] for [{}]", physicalName, tableName);
            return Identifier.toIdentifier(physicalName);
        }
        return super.toPhysicalTableName(name, jdbcEnvironment);
    }

    @Override
    protected boolean isCaseInsensitive(final JdbcEnvironment jdbcEnvironment) {
        val propsResult = ApplicationContextProvider.getCasConfigurationProperties();
        if (propsResult.isEmpty()) {
            LOGGER.debug("Could not load configuration settings to determine case insensitivity.");
            return super.isCaseInsensitive(jdbcEnvironment);
        }
        val casProperties = propsResult.get();
        return casProperties.getJdbc().isCaseInsensitive();
    }
}
