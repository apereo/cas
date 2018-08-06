package org.apereo.cas.otp.repository.credentials;

import org.apereo.cas.CipherExecutor;
import org.apereo.cas.authentication.OneTimeTokenAccount;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
class BaseOneTimeTokenCredentialRepositoryTest {
    @InjectMocks
    private BaseOneTimeTokenCredentialRepository credentialRepository;
    @Mock
    private CipherExecutor<String, String> tokenCredentialCipher;

    @Test
    void shouldReturnAccountWithDecodedSecret() {
        when(tokenCredentialCipher.decode("encoded_secret")).thenReturn("plain_secret");

        OneTimeTokenAccount decodedAccount =
            credentialRepository.decode(
                new OneTimeTokenAccount(
                    "username", "encoded_secret", 12345, Collections.singletonList(123)));

        assertThat(decodedAccount.getSecretKey()).isEqualTo("plain_secret");
    }
}
