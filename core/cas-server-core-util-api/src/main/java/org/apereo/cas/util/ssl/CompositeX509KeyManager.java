package org.apereo.cas.util.ssl;

import org.apereo.cas.util.CollectionUtils;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.ArrayUtils;

import javax.net.ssl.X509KeyManager;
import java.net.Socket;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * This is {@link CompositeX509KeyManager}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@RequiredArgsConstructor
public class CompositeX509KeyManager implements X509KeyManager {

    private final List<X509KeyManager> keyManagers;

    @Override
    public String chooseClientAlias(final String[] keyType, final Principal[] issuers, final Socket socket) {
        return this.keyManagers.stream().map(keyManager -> keyManager.chooseClientAlias(keyType, issuers, socket))
            .filter(Objects::nonNull).findFirst().orElse(null);
    }


    @Override
    public String chooseServerAlias(final String keyType, final Principal[] issuers, final Socket socket) {
        return this.keyManagers.stream().map(keyManager -> keyManager.chooseServerAlias(keyType, issuers, socket))
            .filter(Objects::nonNull).findFirst().orElse(null);
    }


    @Override
    public PrivateKey getPrivateKey(final String alias) {
        return this.keyManagers.stream().map(keyManager -> keyManager.getPrivateKey(alias))
            .filter(Objects::nonNull).findFirst().orElse(null);
    }


    @Override
    public X509Certificate[] getCertificateChain(final String alias) {
        return this.keyManagers.stream().map(keyManager -> keyManager.getCertificateChain(alias))
            .filter(chain -> chain != null && chain.length > 0)
            .findFirst().orElse(null);
    }

    @Override
    public String[] getClientAliases(final String keyType, final Principal[] issuers) {
        val aliases = new ArrayList<String>(keyManagers.size());
        this.keyManagers.forEach(keyManager -> aliases.addAll(CollectionUtils.wrapList(keyManager.getClientAliases(keyType, issuers))));
        return aliases.toArray(ArrayUtils.EMPTY_STRING_ARRAY);
    }

    @Override
    public String[] getServerAliases(final String keyType, final Principal[] issuers) {
        val aliases = new ArrayList<String>(keyManagers.size());
        this.keyManagers.forEach(keyManager -> aliases.addAll(CollectionUtils.wrapList(keyManager.getServerAliases(keyType, issuers))));
        return aliases.toArray(ArrayUtils.EMPTY_STRING_ARRAY);
    }
}
