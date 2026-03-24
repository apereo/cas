package org.apereo.cas.util;

import module java.base;
import org.apereo.cas.util.function.FunctionUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.ldaptive.auth.DnResolver;
import org.ldaptive.auth.User;

/**
 * This is {@link ChainingLdapDnResolver}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@RequiredArgsConstructor
@Slf4j
@Getter
public class ChainingLdapDnResolver implements DnResolver {
    private final List<? extends DnResolver> resolvers;

    @Override
    public @Nullable String resolve(final User user) {
        return resolvers
            .stream()
            .map(resolver -> FunctionUtils.doAndHandle(
                    () -> resolver.resolve(user),
                    throwable -> {
                        LoggingUtils.warn(LOGGER, throwable);
                        return null;
                    })
                .get())
            .filter(Objects::nonNull)
            .findFirst()
            .orElseThrow(() -> new RuntimeException(new AccountNotFoundException("Unable to resolve user dn for " + user.getIdentifier())));
    }
}
