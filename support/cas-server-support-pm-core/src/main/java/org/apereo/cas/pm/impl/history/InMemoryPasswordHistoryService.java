package org.apereo.cas.pm.impl.history;

import org.apereo.cas.pm.PasswordChangeRequest;

import lombok.val;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This is {@link InMemoryPasswordHistoryService}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
public class InMemoryPasswordHistoryService extends BasePasswordHistoryService {
    private final List<PasswordHistoryEntity> history = new ArrayList<>(0);

    @Override
    public boolean exists(final PasswordChangeRequest changeRequest) {
        val username = changeRequest.getUsername();
        val password = changeRequest.getPassword();
        val encodedPassword = encodePassword(password);
        return history
            .stream()
            .anyMatch(p -> p.getPassword().equalsIgnoreCase(encodedPassword) && p.getUsername().equalsIgnoreCase(username));
    }

    @Override
    public Collection<? extends PasswordHistoryEntity> fetchAll() {
        return new ArrayList<>(history);
    }

    @Override
    public Collection<? extends PasswordHistoryEntity> fetch(final String username) {
        return history
            .stream()
            .filter(p -> p.getUsername().equalsIgnoreCase(username))
            .collect(Collectors.toList());
    }

    @Override
    public boolean store(final PasswordChangeRequest changeRequest) {
        val username = changeRequest.getUsername();
        val password = changeRequest.getPassword();
        val encodedPassword = encodePassword(password);
        val entity = new PasswordHistoryEntity();
        entity.setUsername(username);
        entity.setPassword(encodedPassword);
        history.add(entity);
        return true;
    }

    @Override
    public void remove(final String username) {
        this.history.removeIf(p -> p.getUsername().equalsIgnoreCase(username));
    }

    @Override
    public void removeAll() {
        this.history.clear();
    }
}
