package org.apereo.cas.pac4j.serialization;

import module java.base;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.oauth2.sdk.id.State;
import com.nimbusds.oauth2.sdk.pkce.CodeVerifier;
import com.nimbusds.oauth2.sdk.token.AccessTokenType;
import com.nimbusds.oauth2.sdk.token.BearerAccessToken;
import com.nimbusds.oauth2.sdk.token.RefreshToken;
import com.nimbusds.oauth2.sdk.token.Token;
import net.minidev.json.JSONObject;
import tools.jackson.databind.module.SimpleModule;

/**
 * This is {@link NimbusOAuthJacksonModule}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@SuppressWarnings("UnusedVariable")
public class NimbusOAuthJacksonModule extends SimpleModule {
    @Serial
    private static final long serialVersionUID = 4380897174293794761L;

    public NimbusOAuthJacksonModule() {
        setMixInAnnotation(CodeVerifier.class, CodeVerifierMixin.class);
        setMixInAnnotation(BearerAccessToken.class, BearerAccessTokenMixin.class);
        setMixInAnnotation(RefreshToken.class, RefreshTokenMixin.class);
        setMixInAnnotation(Scope.class, ScopeMixin.class);
        setMixInAnnotation(Scope.Value.class, ScopeValueMixin.class);
        setMixInAnnotation(AccessTokenType.class, AccessTokenTypeMixin.class);
        setMixInAnnotation(State.class, AbstractStateMixin.class);
    }

    private static final class AccessTokenTypeMixin {
        @JsonCreator
        AccessTokenTypeMixin(
            @JsonProperty("value")
            final String value) {
        }
    }

    private static final class ScopeValueMixin extends Scope.Value {
        @Serial
        private static final long serialVersionUID = -5131994521473883314L;

        @JsonCreator
        ScopeValueMixin(
            @JsonProperty("value")
            final String value) {
            super(value);
        }
    }

    private static final class ScopeMixin extends Scope {
        @Serial
        private static final long serialVersionUID = -5131994521473883314L;

        @JsonCreator
        ScopeMixin(
            @JsonProperty("values")
            final String... values) {
            super(values);
        }
    }

    private static final class RefreshTokenMixin extends Token {
        @Serial
        private static final long serialVersionUID = 867184690952714608L;

        @JsonCreator
        RefreshTokenMixin(
            @JsonProperty("value")
            final String value) {
        }

        @Override
        @JsonIgnore
        public Set<String> getParameterNames() {
            return new HashSet<>();
        }

        @Override
        @JsonIgnore
        public JSONObject toJSONObject() {
            return null;
        }
    }

    private static final class BearerAccessTokenMixin extends BearerAccessToken {
        @Serial
        private static final long serialVersionUID = -7042673498464860693L;

        @JsonCreator
        BearerAccessTokenMixin(
            @JsonProperty("value")
            final String value,
            @JsonProperty("lifetime")
            final long lifetime,
            @JsonProperty("scope")
            final Scope scope) {
            super(value, lifetime, scope);
        }

        @Override
        @JsonIgnore
        public Set<String> getParameterNames() {
            return super.getParameterNames();
        }
    }

    private static final class CodeVerifierMixin extends CodeVerifier {
        @Serial
        private static final long serialVersionUID = -5587339765097722026L;

        @JsonCreator
        CodeVerifierMixin(
            @JsonProperty("value")
            final String value) {
            super(value);
        }

        @JsonIgnore
        @Override
        public byte[] getValueBytes() {
            return super.getValueBytes();
        }

        @Override
        @JsonIgnore
        public byte[] getSHA256() {
            return super.getSHA256();
        }
    }

    private abstract static class AbstractStateMixin {
        @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
        public static State parse(final String s) {
            return State.parse(s);
        }

        @JsonValue
        @Override
        public abstract String toString();
    }
}
