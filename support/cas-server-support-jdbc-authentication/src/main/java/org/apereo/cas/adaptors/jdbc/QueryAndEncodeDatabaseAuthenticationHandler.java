package org.apereo.cas.adaptors.jdbc;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.crypto.hash.ConfigurableHashService;
import org.apache.shiro.crypto.hash.DefaultHashService;
import org.apache.shiro.crypto.hash.HashRequest;
import org.apache.shiro.util.ByteSource;
import org.apereo.cas.authentication.HandlerResult;
import org.apereo.cas.authentication.PreventedException;
import org.apereo.cas.authentication.UsernamePasswordCredential;
import org.apereo.cas.authentication.exceptions.AccountDisabledException;
import org.apereo.cas.authentication.exceptions.AccountPasswordMustChangeException;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.services.ServicesManager;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;

import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.FailedLoginException;
import javax.sql.DataSource;
import java.security.GeneralSecurityException;
import java.util.Map;

/**
 * A JDBC querying handler that will pull back the password and
 * the private salt value for a user and validate the encoded
 * password using the public salt value. Assumes everything
 * is inside the same database table. Supports settings for
 * number of iterations as well as private salt.
 * <p>
 * This handler uses the hashing method defined by Apache Shiro's
 * {@link org.apache.shiro.crypto.hash.DefaultHashService}. Refer to the Javadocs
 * to learn more about the behavior. If the hashing behavior and/or configuration
 * of private and public salts does nto meet your needs, a extension can be developed
 * to specify alternative methods of encoding and digestion of the encoded password.
 * </p>
 *
 * @author Misagh Moayyed
 * @author Charles Hasegawa
 * @since 4.1.0
 */
public class QueryAndEncodeDatabaseAuthenticationHandler extends AbstractJdbcUsernamePasswordAuthenticationHandler {

    /**
     * The Algorithm name.
     */
    protected String algorithmName;

    /**
     * The Sql statement to execute.
     */
    protected String sql;

    /**
     * The Password field name.
     */
    protected String passwordFieldName = "password";

    /**
     * The Salt field name.
     */
    protected String saltFieldName = "salt";

    /**
     * The Expired field name.
     */
    protected String expiredFieldName;

    /**
     * The Expired field name.
     */
    protected String disabledFieldName;

    /**
     * The Number of iterations field name.
     */
    protected String numberOfIterationsFieldName;

    /**
     * The number of iterations. Defaults to 0.
     */
    protected long numberOfIterations;

    /**
     * Static/private salt to be combined with the dynamic salt retrieved
     * from the database. Optional.
     * <p>If using this implementation as part of a password hashing strategy,
     * it might be desirable to configure a private salt.
     * A hash and the salt used to compute it are often stored together.
     * If an attacker is ever able to access the hash (e.g. during password cracking)
     * and it has the full salt value, the attacker has all of the input necessary
     * to try to brute-force crack the hash (source + complete salt).</p>
     * <p>However, if part of the salt is not available to the attacker (because it is not stored with the hash),
     * it is much harder to crack the hash value since the attacker does not have the complete inputs necessary.
     * The privateSalt property exists to satisfy this private-and-not-shared part of the salt.</p>
     * <p>If you configure this attribute, you can obtain this additional very important safety feature.</p>
     */
    protected String staticSalt;

    public QueryAndEncodeDatabaseAuthenticationHandler(final String name, final ServicesManager servicesManager, final PrincipalFactory principalFactory,
                                                       final Integer order, final DataSource dataSource,
                                                       final String algorithmName, final String sql, final String passwordFieldName,
                                                       final String saltFieldName, final String expiredFieldName, final String disabledFieldName,
                                                       final String numberOfIterationsFieldName, final long numberOfIterations,
                                                       final String staticSalt) {
        super(name, servicesManager, principalFactory, order, dataSource);
        this.algorithmName = algorithmName;
        this.sql = sql;
        this.passwordFieldName = passwordFieldName;
        this.saltFieldName = saltFieldName;
        this.expiredFieldName = expiredFieldName;
        this.disabledFieldName = disabledFieldName;
        this.numberOfIterationsFieldName = numberOfIterationsFieldName;
        this.numberOfIterations = numberOfIterations;
        this.staticSalt = staticSalt;
    }

    @Override
    protected HandlerResult authenticateUsernamePasswordInternal(final UsernamePasswordCredential transformedCredential,
                                                                 final String originalPassword)
            throws GeneralSecurityException, PreventedException {

        if (StringUtils.isBlank(this.sql) || StringUtils.isBlank(this.algorithmName) || getJdbcTemplate() == null) {
            throw new GeneralSecurityException("Authentication handler is not configured correctly");
        }

        final String username = transformedCredential.getUsername();
        try {
            final Map<String, Object> values = getJdbcTemplate().queryForMap(this.sql, username);
            final String digestedPassword = digestEncodedPassword(transformedCredential.getPassword(), values);

            if (!values.get(this.passwordFieldName).equals(digestedPassword)) {
                throw new FailedLoginException("Password does not match value on record.");
            }
            if (StringUtils.isNotBlank(this.expiredFieldName)){
                final Object dbExpired = values.get(this.expiredFieldName);
                if (dbExpired != null && (Boolean.TRUE.equals(BooleanUtils.toBoolean(dbExpired.toString())) || dbExpired.equals(Integer.valueOf(1)))){
                    throw new AccountPasswordMustChangeException("Password has expired");
                }
            }
            if (StringUtils.isNotBlank(this.disabledFieldName)){
                final Object dbDisabled = values.get(this.disabledFieldName);
                if (dbDisabled != null && (Boolean.TRUE.equals(BooleanUtils.toBoolean(dbDisabled.toString())) || dbDisabled.equals(1))){
                    throw new AccountDisabledException("Account has been disabled");
                }
            }
            return createHandlerResult(transformedCredential, this.principalFactory.createPrincipal(username), null);

        } catch (final IncorrectResultSizeDataAccessException e) {
            if (e.getActualSize() == 0) {
                throw new AccountNotFoundException(username + " not found with SQL query");
            } 
            throw new FailedLoginException("Multiple records found for " + username);
        } catch (final DataAccessException e) {
            throw new PreventedException("SQL exception while executing query for " + username, e);
        }
    }

    /**
     * Digest encoded password.
     *
     * @param encodedPassword the encoded password
     * @param values          the values retrieved from database
     * @return the digested password
     */
    protected String digestEncodedPassword(final String encodedPassword, final Map<String, Object> values) {
        final ConfigurableHashService hashService = new DefaultHashService();

        if (StringUtils.isNotBlank(this.staticSalt)) {
            hashService.setPrivateSalt(ByteSource.Util.bytes(this.staticSalt));
        }
        hashService.setHashAlgorithmName(this.algorithmName);

        Long numOfIterations = this.numberOfIterations;
        if (values.containsKey(this.numberOfIterationsFieldName)) {
            final String longAsStr = values.get(this.numberOfIterationsFieldName).toString();
            numOfIterations = Long.valueOf(longAsStr);
        }

        hashService.setHashIterations(numOfIterations.intValue());
        if (!values.containsKey(this.saltFieldName)) {
            throw new IllegalArgumentException("Specified field name for salt does not exist in the results");
        }

        final String dynaSalt = values.get(this.saltFieldName).toString();
        final HashRequest request = new HashRequest.Builder()
                .setSalt(dynaSalt)
                .setSource(encodedPassword)
                .build();
        return hashService.computeHash(request).toHex();
    }
}
