package org.apereo.cas.shell.commands.util;

import module java.base;
import module java.naming;
import org.apereo.cas.shell.commands.CasShellCommand;
import org.apereo.cas.util.LoggingUtils;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.shell.core.command.annotation.Command;
import org.springframework.shell.core.command.annotation.Option;
import org.springframework.util.StringUtils;

/**
 * This is {@link ValidateLdapConnectionCommand}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Slf4j
public class ValidateLdapConnectionCommand implements CasShellCommand {
    private static final int TIMEOUT = 5000;

    /**
     * Validate endpoint.
     *
     * @param url            the url
     * @param bindDn         the bind dn
     * @param bindCredential the bind credential
     * @param baseDn         the base dn
     * @param searchFilter   the search filter
     * @param userPassword   the user password
     * @param userAttributes the user attributes
     * @return true/false
     */
    @Command(group = "Utilities", name = "validate-ldap", description = "Test connections to an LDAP server to verify connectivity, SSL, etc")
    public boolean validateLdap(
        @Option(
            longName = "url",
            description = "LDAP URL to test, comma-separated."
        )
        final String url,

        @Option(
            longName = "bindDn",
            description = "bindDn to use when testing the LDAP server"
        )
        final String bindDn,

        @Option(
            longName = "bindCredential",
            description = "bindCredential to use when testing the LDAP server"
        )
        final String bindCredential,

        @Option(
            longName = "baseDn",
            description = "baseDn to use when testing the LDAP server, searching for accounts (i.e. OU=some,DC=org,DC=edu)"
        )
        final String baseDn,

        @Option(
            longName = "searchFilter",
            description = "Filter to use when searching for accounts (i.e. (&(objectClass=*) (sAMAccountName=user)))",
            defaultValue = org.apache.commons.lang3.StringUtils.EMPTY
        )
        final String searchFilter,

        @Option(
            longName = "userPassword",
            description = "Password for the user found in the search result, to attempt authentication",
            defaultValue = org.apache.commons.lang3.StringUtils.EMPTY
        )
        final String userPassword,

        @Option(
            longName = "userAttributes",
            description = "User attributes, comma-separated, to fetch for the user found in the search result",
            defaultValue = org.apache.commons.lang3.StringUtils.EMPTY
        )
        final String userAttributes) {
        try {
            return connect(url, bindDn, bindCredential, baseDn, searchFilter, userAttributes, userPassword);
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
        return false;
    }

    @SuppressWarnings({"JdkObsolete", "BanJNDI"})
    private static boolean connect(final String ldapUrl,
                                   final String bindDn,
                                   final String bindCredential,
                                   final String baseDn,
                                   final String searchFilter,
                                   final String userAttributes,
                                   final String userPassword) throws Exception {
        val pair = getContext(ldapUrl, bindDn, bindCredential);
        if (pair == null) {
            LOGGER.error("Could not connect to any of the provided LDAP urls based on the given credentials.");
            return false;
        }

        val ctx = pair.getValue();
        try {
            var log = "Successfully connected to the LDAP url [" + pair.getKey() + "] ";
            if (ctx.getNameInNamespace() != null && !ctx.getNameInNamespace().isEmpty()) {
                log += "with namespace [" + ctx.getNameInNamespace() + "].";
            }
            LOGGER.info(log);

            if (!StringUtils.hasText(searchFilter)) {
                return true;
            }

            val attrIDs = userAttributes.split(",");
            LOGGER.info("******* Ldap Search *******");
            LOGGER.info("Ldap filter: [{}]", searchFilter);
            LOGGER.info("Ldap search base: [{}]", baseDn);
            LOGGER.info("Returning attributes: [{}]\n", Arrays.toString(attrIDs));

            val ctls = getSearchControls(attrIDs);
            val answer = ctx.search(baseDn, searchFilter, ctls);
            if (answer.hasMoreElements()) {
                LOGGER.info("******* Ldap Search Results *******");
                while (answer.hasMoreElements()) {
                    val result = answer.nextElement();
                    LOGGER.info("User name: [{}]", result.getName());
                    LOGGER.info("User full name: [{}]", result.getNameInNamespace());

                    if (org.apache.commons.lang3.StringUtils.isNotBlank(userPassword)) {
                        LOGGER.info("Attempting to authenticate [{}] with password [{}]", result.getName(), userPassword);

                        val env = getLdapDirectoryContextSettings(result.getNameInNamespace(), userPassword, pair.getKey());
                        new InitialDirContext(env);
                        LOGGER.info("Successfully authenticated [{}] with password [{}]", result.getName(), userPassword);
                    }
                    val attrs = result.getAttributes().getIDs();
                    while (attrs.hasMoreElements()) {
                        val id = attrs.nextElement();
                        LOGGER.info("[{}] => [{}]", id, result.getAttributes().get(id));
                    }
                }
                return true;
            } else {
                LOGGER.info("No search results could be found.");
            }
            LOGGER.info("Ldap search completed successfully.");
        } finally {
            if (ctx != null) {
                ctx.close();
            }
        }
        return false;
    }

    private static SearchControls getSearchControls(final String[] attrIDs) {
        val ctls = new SearchControls();
        ctls.setDerefLinkFlag(true);
        ctls.setTimeLimit(TIMEOUT);
        ctls.setReturningAttributes(attrIDs);
        ctls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        return ctls;
    }

    private static Pair<String, DirContext> getContext(final String ldapUrl, final String bindDn, final String bindCredential) {
        val urls = StringUtils.commaDelimitedListToSet(ldapUrl);
        for (val url : urls) {
            if (ldapUrl != null && !ldapUrl.isEmpty()) {
                LOGGER.info("Attempting connect to LDAP instance [{}]", url);

                val env = getLdapDirectoryContextSettings(bindDn, bindCredential, url);
                try {
                    return Pair.of(ldapUrl, new InitialDirContext(env));
                } catch (final Exception e) {
                    LOGGER.error("Failed to connect to ldap instance [{}]", ldapUrl);
                }
            }
        }

        return null;
    }

    @SuppressWarnings("JdkObsolete")
    private static Hashtable<String, String> getLdapDirectoryContextSettings(final String bindDn, final String bindCredential, final String url) {
        val env = new Hashtable<String, String>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, url.trim());
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.SECURITY_PRINCIPAL, bindDn);
        env.put(Context.SECURITY_CREDENTIALS, bindCredential);
        env.put("com.sun.jndi.ldap.connect.timeout", String.valueOf(TIMEOUT));
        return env;
    }
}
