package org.apereo.cas.authentication;

import org.apereo.cas.authentication.principal.Principal;
import org.ldaptive.Connection;
import org.ldaptive.LdapEntry;
import org.ldaptive.SearchOperation;
import org.ldaptive.SearchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.LinkedHashSet;

/**
 * This is {@link LdapSurrogateUsernamePasswordService}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
//public class LdapSurrogateUsernamePasswordService implements SurrogateAuthenticationService {
//    private static final Logger LOGGER = LoggerFactory.getLogger(LdapSurrogateUsernamePasswordService.class);
//
//    /*
//    @Override
//    public boolean canAuthenticateAs(final String username, final Principal surrogate) {
//        if (username.equalsIgnoreCase(surrogate.getId())) {
//            return true;
//        }
//
//        Connection connection = null;
//        try {
//            connection = connectionFactory.getConnection();
//            connection.open();
//            String spa = String.format(spaFormat, username);
//            String searchFilter = surrogateSearchFilter.replaceAll("%upsa", userPrincipalStringAttribute)
//                    .replaceAll("%uma", userMemberAttribute).replaceAll("%spa", spa).replaceAll("%u", surrogate.getId());
//            SearchOperation searchOperation = new SearchOperation(connection);
//            SearchResult searchResult = searchOperation.execute(new SearchRequest(baseDN, searchFilter)).getResult();
//            return searchResult.getEntry() != null;
//        } catch (final Exception e) {
//            LOGGER.error(e.getMessage(), e);
//        } finally {
//            if (connection != null) {
//                connection.close();
//            }
//        }
//        return false;
//    }
//
//    @Override
//    public Collection<String> getEligibleAccountsForSurrogateToProxy(final String username) {
//        final Collection<String> eligible = new LinkedHashSet<>();
//        Connection connection = null;
//        try {
//            connection = connectionFactory.getConnection();
//            connection.open();
//            SearchOperation searchOperation = new SearchOperation(connection);
//            String searchFilter = userSearchFilter.replaceAll("%upsa", userPrincipalStringAttribute).replaceAll("%u", surrogateUsername);
//            SearchResult searchResult = searchOperation.execute(new SearchRequest(baseDN, searchFilter, userMemberAttribute)).getResult();
//
//            LdapEntry ldapEntry = searchResult.getEntry();
//            for (String value : ldapEntry.getAttribute(userMemberAttribute).getStringValues()) {
//                Matcher matcher = spaPattern.matcher(value);
//                if (matcher.matches()) {
//                    eligible.add(matcher.group(1));
//                }
//            }
//        } catch (final Exception e) {
//            LOGGER.error(e.getMessage(), e);
//        } finally {
//            if (connection != null) {
//                connection.close();
//            }
//        }
//        return eligible;
//    }
//    
//    */
//}
