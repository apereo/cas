package org.apereo.cas.pm.impl;

import module java.base;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.pm.PasswordChangeRequest;
import org.apereo.cas.pm.PasswordManagementQuery;
import org.apereo.cas.pm.PasswordManagementService;
import org.apereo.cas.util.function.FunctionUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.jooq.lambda.Unchecked;

/**
 * This is {@link ChainingPasswordManagementService}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@RequiredArgsConstructor
public class ChainingPasswordManagementService implements PasswordManagementService {
    private final List<PasswordManagementService> registeredServices;

    @Override
    public boolean change(final PasswordChangeRequest bean) throws Throwable {
        return registeredServices
            .stream()
            .allMatch(Unchecked.predicate(service -> service.change(bean)));
    }

    @Override
    public boolean unlockAccount(final Credential credential) throws Throwable {
        return registeredServices
            .stream()
            .allMatch(Unchecked.predicate(service -> service.unlockAccount(credential)));
    }

    @Override
    public String findEmail(final PasswordManagementQuery query) throws Throwable {
        return registeredServices
            .stream()
            .map(service -> FunctionUtils.doAndHandle(() -> service.findEmail(query)))
            .filter(StringUtils::isNotBlank)
            .findFirst()
            .orElse(null);
    }

    @Override
    public String findPhone(final PasswordManagementQuery query) throws Throwable {
        return registeredServices
            .stream()
            .map(service -> FunctionUtils.doAndHandle(() -> service.findPhone(query)))
            .filter(StringUtils::isNotBlank)
            .findFirst()
            .orElse(null);
    }

    @Override
    public String findUsername(final PasswordManagementQuery query) throws Throwable {
        return registeredServices
            .stream()
            .map(service -> FunctionUtils.doAndHandle(() -> service.findUsername(query)))
            .filter(StringUtils::isNotBlank)
            .findFirst()
            .orElse(null);
    }

    @Override
    public String createToken(final PasswordManagementQuery query) {
        return registeredServices
            .stream()
            .map(service -> FunctionUtils.doAndHandle(() -> service.createToken(query)))
            .filter(StringUtils::isNotBlank)
            .findFirst()
            .orElse(null);
    }

    @Override
    public String parseToken(final String token) {
        return registeredServices
            .stream()
            .map(service -> FunctionUtils.doAndHandle(() -> service.parseToken(token)))
            .filter(StringUtils::isNotBlank)
            .findFirst()
            .orElse(null);
    }

    @Override
    public Map<String, String> getSecurityQuestions(final PasswordManagementQuery query) throws Throwable {
        return registeredServices
            .stream()
            .map(service -> FunctionUtils.doAndHandle(() -> service.getSecurityQuestions(query)))
            .filter(Objects::nonNull)
            .flatMap(questionsMap -> questionsMap.entrySet().stream())
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                (v1, v2) -> v2
            ));
    }

    @Override
    public void updateSecurityQuestions(final PasswordManagementQuery query) throws Throwable {
        registeredServices.forEach(service -> FunctionUtils.doUnchecked(_ -> service.updateSecurityQuestions(query)));
    }

    @Override
    public boolean isAnswerValidForSecurityQuestion(final PasswordManagementQuery query, final String question,
                                                    final String knownAnswer, final String givenAnswer) {
        return registeredServices
            .stream()
            .anyMatch(service -> service.isAnswerValidForSecurityQuestion(query, question, knownAnswer, givenAnswer));
    }
}
