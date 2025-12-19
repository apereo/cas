package org.apereo.cas.gauth.credential;

import module java.base;
import com.warrenstrange.googleauth.ICredentialRepository;

/**
 * This is {@link DummyCredentialRepository}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
public class DummyCredentialRepository implements ICredentialRepository {
    private final Map<String, String> accounts = new LinkedHashMap<>();

    @Override
    public String getSecretKey(final String s) {
        return accounts.get(s);
    }

    @Override
    public void saveUserCredentials(final String s, final String s1, final int i, final List<Integer> list) {
        accounts.put(s, s1);
    }
}
