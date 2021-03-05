package org.apereo.cas.redis;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * This is {@link RedisUserAccount}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Data
@RedisHash("RedisUserAccount")
@AllArgsConstructor
public class RedisUserAccount implements Serializable {
    private static final int MAP_SIZE = 8;

    private static final long serialVersionUID = 7655148747303981918L;

    private String username;

    private String password;

    private Map<String, List<Object>> attributes = new LinkedHashMap<>(MAP_SIZE);

    private AccountStatus status = AccountStatus.OK;

    /**
     * Indicates user account status.
     */
    public enum AccountStatus {

        /**
         * Ok account status.
         */
        OK,
        /**
         * Locked account status.
         */
        LOCKED,
        /**
         * Disabled account status.
         */
        DISABLED,
        /**
         * Expired account status.
         */
        EXPIRED,
        /**
         * Must change password account status.
         */
        MUST_CHANGE_PASSWORD
    }
}
