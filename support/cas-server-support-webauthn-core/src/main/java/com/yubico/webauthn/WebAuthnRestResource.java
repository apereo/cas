// Copyright (c) 2018, Yubico AB
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are met:
//
// 1. Redistributions of source code must retain the above copyright notice, this
//    list of conditions and the following disclaimer.
//
// 2. Redistributions in binary form must reproduce the above copyright notice,
//    this list of conditions and the following disclaimer in the documentation
//    and/or other materials provided with the distribution.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
// AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
// IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
// DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
// FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
// DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
// SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
// CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
// OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
// OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

package com.yubico.webauthn;

import org.apereo.cas.configuration.CasConfigurationProperties;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.yubico.internal.util.JacksonCodecs;
import com.yubico.util.Either;
import com.yubico.webauthn.data.AssertionRequestWrapper;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.RegistrationRequest;
import com.yubico.webauthn.meta.VersionInfo;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@RestController("webAuthnRestResource")
@Slf4j
@RequiredArgsConstructor
public class WebAuthnRestResource {
    private static final String API_VERSION = "/api/v1";

    private static final ObjectMapper MAPPER = JacksonCodecs.json().findAndRegisterModules();

    private final WebAuthnServer server;

    private final CasConfigurationProperties casProperties;

    private final JsonNodeFactory jsonFactory = JsonNodeFactory.instance;

    private static ResponseEntity<Object> jsonFail() {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body("{\"messages\":[\"Failed to encode response as JSON\"]}");
    }

    private static ResponseEntity<Object> startResponse(String operationName, Object request) {
        try {
            LOGGER.trace("Operation: {}", operationName);
            return ResponseEntity.ok(writeJson(request));
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
            return jsonFail();
        }
    }

    @SneakyThrows
    private static String writeJson(final Object o) {
        return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(o);
    }

    @GetMapping(API_VERSION)
    public ResponseEntity<Object> index() {
        return new ResponseEntity<>(writeJson(new IndexResponse()), HttpStatus.OK);
    }

    @GetMapping(API_VERSION + "/version")
    public ResponseEntity<Object> version() {
        return new ResponseEntity<>(writeJson(new VersionResponse()), HttpStatus.OK);
    }

    @PostMapping(API_VERSION + "/register")
    public ResponseEntity<Object> startRegistration(
        @NonNull @RequestParam("username") String username,
        @NonNull @RequestParam("displayName") String displayName,
        @RequestParam(value = "credentialNickname", required = false, defaultValue = "true") String credentialNickname,
        @RequestParam(value = "requireResidentKey", required = false) boolean requireResidentKey,
        @RequestParam(value = "sessionToken", required = false, defaultValue = "") String sessionTokenBase64) throws Exception {
        val result = server.startRegistration(
            username,
            Optional.of(displayName),
            Optional.ofNullable(credentialNickname),
            requireResidentKey,
            Optional.ofNullable(sessionTokenBase64).map(base64 -> {
                try {
                    return ByteArray.fromBase64Url(base64);
                } catch (final Exception e) {
                    throw new RuntimeException(e);
                }
            })
        );

        if (result.isRight()) {
            return startResponse("startRegistration", new StartRegistrationResponse(result.right().get()));
        }
        return messagesJson(
            ResponseEntity.badRequest(),
            result.left().get()
        );

    }

    @PostMapping(API_VERSION + "/register/finish")
    public ResponseEntity finishRegistration(@RequestBody String responseJson) throws Exception {
        val result = server.finishRegistration(responseJson);
        return finishResponse(
            result,
            "Attestation verification failed; further error message(s) were unfortunately lost to an internal server error.",
            "finishRegistration",
            responseJson
        );
    }

    @PostMapping(API_VERSION + "/authenticate")
    public ResponseEntity startAuthentication(@RequestParam("username") String username) throws MalformedURLException {
        Either<List<String>, AssertionRequestWrapper> request = server.startAuthentication(Optional.ofNullable(username));
        if (request.isRight()) {
            return startResponse("startAuthentication", new StartAuthenticationResponse(request.right().get()));
        }
        return messagesJson(ResponseEntity.badRequest(), request.left().get());

    }

    @PostMapping(API_VERSION + "/authenticate/finish")
    public ResponseEntity finishAuthentication(@NonNull String responseJson) {
        Either<List<String>, WebAuthnServer.SuccessfulAuthenticationResult> result = server.finishAuthentication(responseJson);

        return finishResponse(
            result,
            "Authentication verification failed; further error message(s) were unfortunately lost to an internal server error.",
            "finishAuthentication",
            responseJson
        );
    }

    @PostMapping(API_VERSION + "/action/deregister")
    public ResponseEntity deregisterCredential(
        @NonNull @RequestParam("sessionToken") String sessionTokenBase64,
        @NonNull @RequestParam("credentialId") String credentialIdBase64) throws Exception {

        try {
            val credentialId = ByteArray.fromBase64Url(credentialIdBase64);

            val result = server.deregisterCredential(
                ByteArray.fromBase64Url(sessionTokenBase64),
                credentialId
            );

            if (result.isRight()) {
                return finishResponse(
                    result,
                    "Failed to deregister credential; further error message(s) were unfortunately lost to an internal server error.",
                    "deregisterCredential",
                    ""
                );
            }
            return messagesJson(
                ResponseEntity.badRequest(),
                result.left().get()
            );

        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
            return messagesJson(
                ResponseEntity.badRequest(),
                "Credential ID is not valid Base64Url data: " + credentialIdBase64
            );
        }


    }

    @DeleteMapping(API_VERSION + "/delete-account")
    public ResponseEntity deleteAccount(@NonNull @RequestParam("username") String username) {
        val result = server.deleteAccount(username, () ->
            ((ObjectNode) jsonFactory.objectNode()
                .set("success", jsonFactory.booleanNode(true)))
                .set("deletedAccount", jsonFactory.textNode(username))
        );

        if (result.isRight()) {
            return ResponseEntity.ok(writeJson(result.right().get().toString()));
        }
        return messagesJson(
            ResponseEntity.badRequest(),
            result.left().get()
        );
    }

    private ResponseEntity<Object> finishResponse(Either<List<String>, ?> result, String jsonFailMessage, String methodName, String responseJson) {
        if (result.isRight()) {
            try {
                LOGGER.trace("[{}]: {}", methodName, responseJson);
                return ResponseEntity.ok(writeJson(result.right().get()));
            } catch (final Exception e) {
                LOGGER.error(e.getMessage(), e);
                return messagesJson(
                    ResponseEntity.ok(),
                    jsonFailMessage
                );
            }
        }
        return messagesJson(
            ResponseEntity.badRequest(),
            result.left().get()
        );
    }

    private ResponseEntity<Object> messagesJson(ResponseEntity.BodyBuilder response, String message) {
        return messagesJson(response, Collections.singletonList(message));
    }

    private ResponseEntity<Object> messagesJson(ResponseEntity.BodyBuilder response, List<String> messages) {
        try {
            return response.body(
                writeJson(jsonFactory.objectNode()
                    .set("messages", jsonFactory.arrayNode()
                        .addAll(messages.stream().map(jsonFactory::textNode).collect(Collectors.toList()))
                    )
                ));
        } catch (final Exception e) {
            LOGGER.error("Failed to encode messages as JSON: {}", messages, e);
            return jsonFail();
        }
    }

    @Getter
    private static final class VersionResponse {
        private final VersionInfo version = VersionInfo.getInstance();
    }

    @Getter
    private final class Info {
        private final URL version;

        public Info() {
            version = casProperties.getServer().buildContextRelativeUrl(API_VERSION + "/version");
        }
    }

    @RequiredArgsConstructor
    @Getter
    private final class StartAuthenticationResponse {
        private final boolean success = true;

        private final AssertionRequestWrapper request;

        private final StartAuthenticationActions actions = new StartAuthenticationActions();
    }

    @RequiredArgsConstructor
    @Getter
    private final class StartRegistrationResponse {
        private final boolean success = true;

        private final RegistrationRequest request;

        private final StartRegistrationActions actions = new StartRegistrationActions();
    }

    @Getter
    private final class StartRegistrationActions {
        private final URL finish;

        public StartRegistrationActions() {
            finish = casProperties.getServer().buildContextRelativeUrl(API_VERSION + "/register/finish");
        }
    }

    @Getter
    private final class StartAuthenticationActions {
        private final URL finish;

        public StartAuthenticationActions() {
            finish = casProperties.getServer().buildContextRelativeUrl(API_VERSION + "/authenticate/finish");
        }
    }

    @NoArgsConstructor
    @Getter
    private final class IndexResponse {
        private final Index actions = new Index();

        private final Info info = new Info();
    }

    @Getter
    private final class Index {
        private final URL authenticate;

        private final URL deleteAccount;

        private final URL deregister;

        private final URL register;

        public Index() {
            authenticate = casProperties.getServer().buildContextRelativeUrl(API_VERSION + "/authenticate");
            deleteAccount = casProperties.getServer().buildContextRelativeUrl(API_VERSION + "/delete-account");
            deregister = casProperties.getServer().buildContextRelativeUrl(API_VERSION + "/action/deregister");
            register = casProperties.getServer().buildContextRelativeUrl(API_VERSION + "/register");
        }
    }

}
