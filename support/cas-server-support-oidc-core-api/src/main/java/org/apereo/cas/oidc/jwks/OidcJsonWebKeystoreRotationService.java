package org.apereo.cas.oidc.jwks;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jwk.JsonWebKeySet;

import java.util.Arrays;

/**
 * This is {@link OidcJsonWebKeystoreRotationService}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
public interface OidcJsonWebKeystoreRotationService {
    /**
     * State parameter to indicate the lifecycle and status of a given key.
     */
    String PARAMETER_STATE = "state";

    /**
     * Rotate keys in the keystore.
     * The result of this operation would force CAS to rotate keys in the keystore in the following way:
     * <ul>
     *     <li>Keys marked as {@link JsonWebKeyLifecycleStates#CURRENT} will switch to {@link JsonWebKeyLifecycleStates#PREVIOUS}</li>
     *     <li>Keys marked as {@link JsonWebKeyLifecycleStates#FUTURE} will switch to {@link JsonWebKeyLifecycleStates#CURRENT}</li>
     *     <li>A new future key is generated and put into the keystore with the state as {@link JsonWebKeyLifecycleStates#FUTURE}</li>
     * </ul>
     *
     * @return the json web key set
     * @throws Exception the exception
     */
    JsonWebKeySet rotate() throws Exception;

    /**
     * Removes keys in the keystore that marked as {@link JsonWebKeyLifecycleStates#PREVIOUS}.
     *
     * @return the json web key set
     * @throws Exception the exception
     */
    JsonWebKeySet revoke() throws Exception;

    @RequiredArgsConstructor
    @Getter
    enum JsonWebKeyLifecycleStates {
        /**
         * The key state is active and current
         * and is used by CAS for crypto operations as necessary.
         * Per the rotation schedule, the key with this status
         * would be replaced and rotated by the future key.
         */
        CURRENT(0),
        /**
         * The key state is one for the future and will take
         * the place of the active/current key per the rotation schedule.
         */
        FUTURE(1),
        /**
         * Previous key prior to the current key.
         * This key continues to remain valid and available,
         * and is a candidate to be removed from the keystore
         * per the revocation schedule.
         */
        PREVIOUS(2);

        private final long state;

        /**
         * Gets json web key state.
         *
         * @param key the key
         * @return the json web key state
         */
        public static JsonWebKeyLifecycleStates getJsonWebKeyState(final JsonWebKey key) {
            val state = key.getOtherParameterValue(OidcJsonWebKeystoreRotationService.PARAMETER_STATE, Long.class);
            return Arrays.stream(JsonWebKeyLifecycleStates.values())
                .filter(s -> state != null && s.getState() == state)
                .findFirst()
                .orElse(CURRENT);
        }

        /**
         * Sets json web key state.
         *
         * @param key   the key
         * @param state the state
         */
        public static void setJsonWebKeyState(final JsonWebKey key, final JsonWebKeyLifecycleStates state) {
            key.setOtherParameter(OidcJsonWebKeystoreRotationService.PARAMETER_STATE, state.getState());
        }

        public boolean isCurrent() {
            return getState() == CURRENT.getState();
        }

        public boolean isPrevious() {
            return getState() == PREVIOUS.getState();
        }

        public boolean isFuture() {
            return getState() == FUTURE.getState();
        }
    }
}
