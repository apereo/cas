package org.apereo.cas.otp.repository.credentials;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.util.serialization.AbstractJacksonBackedStringSerializer;
import org.apereo.cas.util.serialization.StringSerializer;
import org.springframework.core.io.Resource;

import java.util.Collection;
import java.util.List;
import java.util.TreeSet;

/**
 * This is {@link BaseJsonOneTimeTokenCredentialRepository}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public abstract class BaseJsonOneTimeTokenCredentialRepository extends BaseOneTimeTokenCredentialRepository {
    private final Resource location;
    private final StringSerializer<TreeSet<OneTimeTokenAccount>> serializer = new OneTimeAccountSerializer();

    public BaseJsonOneTimeTokenCredentialRepository(final Resource location) {
        this.location = location;
    }

    @Override
    public String getSecret(final String username) {
        try {
            if (!this.location.getFile().exists()) {
                logger.warn("JSON account repository file [{}] is not found.", this.location.getFile());
                return null;
            }

            if (this.location.getFile().length() <= 0) {
                logger.warn("JSON account repository file [{}] is empty.", this.location.getFile());
                return null;
            }

            final Collection<OneTimeTokenAccount> c = this.serializer.from(this.location.getFile());
            return c.stream()
                    .filter(a -> StringUtils.isNotBlank(a.getUsername()) && a.getUsername().equals(username))
                    .map(OneTimeTokenAccount::getSecretKey)
                    .findAny()
                    .orElse(null);
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    @Override
    public void save(final String userName, final String secretKey,
                                    final int validationCode,
                                    final List<Integer> scratchCodes) {
        try {
            logger.debug("Storing google authenticator account for [{}]", userName);
            final OneTimeTokenAccount account = new OneTimeTokenAccount(userName, secretKey, validationCode, scratchCodes);

            logger.debug("Ensuring JSON repository file exists at [{}]", this.location.getFile());
            this.location.getFile().createNewFile();

            final TreeSet<OneTimeTokenAccount> c;
            if (this.location.getFile().length() > 0) {
                logger.debug("Reading JSON repository file at [{}]", this.location.getFile());
                c = this.serializer.from(this.location.getFile());
            } else {
                c = new TreeSet<>();
            }
            
            logger.debug("Found {} account(s) and added google authenticator account for [{}]", c.size(), userName);
            c.add(account);

            logger.debug("Saving google authenticator accounts back to the JSON file at [{}]", this.location.getFile());
            this.serializer.to(this.location.getFile(), c);
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    private static class OneTimeAccountSerializer extends AbstractJacksonBackedStringSerializer<TreeSet<OneTimeTokenAccount>> {
        private static final long serialVersionUID = 1466569521275630254L;

        @Override
        protected Class getTypeToSerialize() {
            return TreeSet.class;
        }
    }
}
