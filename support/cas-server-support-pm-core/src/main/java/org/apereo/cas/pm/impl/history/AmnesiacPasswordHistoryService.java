package org.apereo.cas.pm.impl.history;

import module java.base;
import org.apereo.cas.pm.PasswordChangeRequest;

/**
 * This is {@link AmnesiacPasswordHistoryService}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
public class AmnesiacPasswordHistoryService extends BasePasswordHistoryService {
    @Override
    public boolean exists(final PasswordChangeRequest changeRequest) {
        return false;
    }

    @Override
    public Collection<? extends PasswordHistoryEntity> fetchAll() {
        return new ArrayList<>();
    }

    @Override
    public Collection<? extends PasswordHistoryEntity> fetch(final String username) {
        return new ArrayList<>();
    }

    @Override
    public boolean store(final PasswordChangeRequest changeRequest) {
        return true;
    }

    @Override
    public void remove(final String username) {
    }

    @Override
    public void removeAll() {
    }
}
