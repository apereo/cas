package org.apereo.cas.pm.impl.history;

import org.apereo.cas.pm.PasswordChangeRequest;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collection;

/**
 * This is {@link AmnesiacPasswordHistoryService}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Slf4j
public class AmnesiacPasswordHistoryService extends BasePasswordHistoryService {
    @Override
    public boolean exists(final PasswordChangeRequest changeRequest) {
        return false;
    }

    @Override
    public Collection<PasswordHistoryEntity> fetchAll() {
        return new ArrayList<>();
    }

    @Override
    public Collection<PasswordHistoryEntity> fetch(final String username) {
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
