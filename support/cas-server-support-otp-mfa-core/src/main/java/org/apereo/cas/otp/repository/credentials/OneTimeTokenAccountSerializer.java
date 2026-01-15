package org.apereo.cas.otp.repository.credentials;

import module java.base;
import org.apereo.cas.authentication.OneTimeTokenAccount;
import org.apereo.cas.util.serialization.BaseJacksonSerializer;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * This is {@link OneTimeTokenAccountSerializer}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
public class OneTimeTokenAccountSerializer extends BaseJacksonSerializer<Map<String, List<OneTimeTokenAccount>>> {
    @Serial
    private static final long serialVersionUID = 1466569521275630254L;

    public OneTimeTokenAccountSerializer(final ConfigurableApplicationContext applicationContext) {
        super(applicationContext, Map.class);
    }
}
