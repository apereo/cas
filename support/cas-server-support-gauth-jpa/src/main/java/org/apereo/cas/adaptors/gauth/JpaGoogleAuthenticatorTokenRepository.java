package org.apereo.cas.adaptors.gauth;

import org.apereo.cas.adaptors.gauth.repository.token.BaseGoogleAuthenticatorTokenRepository;
import org.apereo.cas.adaptors.gauth.repository.token.GoogleAuthenticatorToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * This is {@link JpaGoogleAuthenticatorTokenRepository}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@EnableTransactionManagement(proxyTargetClass = true)
@Transactional(readOnly = false, transactionManager = "transactionManagerGoogleAuthenticator")
public class JpaGoogleAuthenticatorTokenRepository extends BaseGoogleAuthenticatorTokenRepository {
    private static final Logger LOGGER = LoggerFactory.getLogger(JpaGoogleAuthenticatorCredentialRepository.class);

    @PersistenceContext(unitName = "googleAuthenticatorEntityManagerFactory")
    private EntityManager entityManager;
    
    @Override
    public void clean() {
        
    }

    @Override
    public void store(final GoogleAuthenticatorToken token) {

    }

    @Override
    public boolean exists(final String uid, final Integer otp) {
        return false;
    }
}
