package org.apereo.cas.pac4j.serialization;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.oauth2.sdk.pkce.CodeVerifier;
import com.nimbusds.oauth2.sdk.token.AccessTokenType;
import com.nimbusds.oauth2.sdk.token.BearerAccessToken;
import com.nimbusds.oauth2.sdk.token.RefreshToken;
import com.nimbusds.oauth2.sdk.token.Token;
import net.minidev.json.JSONObject;

import java.util.HashSet;
import java.util.Set;

/**
 * This is {@link NimbusOAuthJacksonModule}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
public class NimbusOAuthJacksonModule extends SimpleModule {
    private static final long serialVersionUID = 4380897174293794761L;

    public NimbusOAuthJacksonModule() {
        setMixInAnnotation(CodeVerifier.class, CodeVerifierMixin.class);
        setMixInAnnotation(BearerAccessToken.class, BearerAccessTokenMixin.class);
        setMixInAnnotation(RefreshToken.class, RefreshTokenMixin.class);
        setMixInAnnotation(Scope.class, ScopeMixin.class);
        setMixInAnnotation(Scope.Value.class, ScopeValueMixin.class);
        setMixInAnnotation(AccessTokenType.class, AccessTokenTypeMixin.class);
    }

    private static class AccessTokenTypeMixin {
        @JsonCreator
        AccessTokenTypeMixin(
            @JsonProperty("value")
            final String value) {
        }
    }

    private static class ScopeValueMixin extends Scope.Value {
        private static final long serialVersionUID = -5131994521473883314L;

        @JsonCreator
        ScopeValueMixin(
            @JsonProperty("value")
            final String value) {
            super(value);
        }
    }

    private static class ScopeMixin extends Scope {
        private static final long serialVersionUID = -5131994521473883314L;

        @JsonCreator
        ScopeMixin(
            @JsonProperty("values")
            final String... values) {
            super(values);
        }
    }

    private static class RefreshTokenMixin extends Token {
        private static final long serialVersionUID = 867184690952714608L;

        @JsonCreator
        RefreshTokenMixin(
            @JsonProperty("value")
            final String value) {
        }

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

    private static class BearerAccessTokenMixin extends BearerAccessToken {
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

    private static class CodeVerifierMixin extends CodeVerifier {
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
}
