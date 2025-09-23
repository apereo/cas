package org.apereo.cas.aws.authz;

import org.apereo.cas.aws.ChainingAWSCredentialsProvider;
import org.apereo.cas.configuration.support.ExpressionLanguageCapable;
import org.apereo.cas.services.BaseRegisteredServiceAccessStrategy;
import org.apereo.cas.services.RegisteredServiceAccessStrategyRequest;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import software.amazon.awssdk.awscore.defaultsmode.DefaultsMode;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.core.retry.RetryMode;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.verifiedpermissions.VerifiedPermissionsClient;
import software.amazon.awssdk.services.verifiedpermissions.model.ActionIdentifier;
import software.amazon.awssdk.services.verifiedpermissions.model.AttributeValue;
import software.amazon.awssdk.services.verifiedpermissions.model.ContextDefinition;
import software.amazon.awssdk.services.verifiedpermissions.model.Decision;
import software.amazon.awssdk.services.verifiedpermissions.model.EntityIdentifier;
import software.amazon.awssdk.services.verifiedpermissions.model.IsAuthorizedRequest;
import java.io.Serial;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * This is {@link AmazonVerifiedPermissionsRegisteredServiceAccessStrategy}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@ToString(callSuper = true)
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@Slf4j
public class AmazonVerifiedPermissionsRegisteredServiceAccessStrategy extends BaseRegisteredServiceAccessStrategy {
    @Serial
    private static final long serialVersionUID = 8331462526633144898L;

    @ExpressionLanguageCapable
    private String credentialAccessKey;

    @ExpressionLanguageCapable
    private String credentialSecretKey;

    @ExpressionLanguageCapable
    private String region;

    @ExpressionLanguageCapable
    private String policyStoreId;

    @ExpressionLanguageCapable
    private String actionId;

    private Map<String, Object> context = new TreeMap<>();

    @Override
    public boolean authorizeRequest(final RegisteredServiceAccessStrategyRequest request) {
        val resolver = SpringExpressionLanguageValueResolver.getInstance();

        try (val client = buildAmazonVerifiedPermissionsClient()) {
            val authorizedRequest = IsAuthorizedRequest
                .builder()
                .principal(EntityIdentifier.builder()
                    .entityId(request.getPrincipalId())
                    .build())
                .resource(EntityIdentifier.builder()
                    .entityId(request.getService().getId())
                    .build())
                .action(ActionIdentifier.builder()
                    .actionId(resolver.resolve(actionId))
                    .build())
                .context(ContextDefinition.builder()
                    .contextMap(buildAuthorizationContextMap(request))
                    .build())
                .policyStoreId(resolver.resolve(policyStoreId))
                .build();
            return FunctionUtils.doAndHandle(() -> {
                LOGGER.debug("Sending authorization request [{}]", authorizedRequest);
                val authorizedResponse = client.isAuthorized(authorizedRequest);
                LOGGER.debug("Authorization response [{}], evaluated policies [{}]",
                    authorizedResponse.decisionAsString(), authorizedResponse.determiningPolicies());
                return authorizedResponse.decision() == Decision.ALLOW;
            }, e -> false).get();
        }
    }

    protected Map<String, AttributeValue> buildAuthorizationContextMap(
        final RegisteredServiceAccessStrategyRequest request) {
        val contextMap = new HashMap<>(buildAttributeValueMap(context));
        contextMap.putAll(buildAttributeValueMap(request.getAttributes()));
        contextMap.putAll(buildAttributeValueMap(request.getService().getAttributes()));
        return contextMap;
    }

    protected Map<String, AttributeValue> buildAttributeValueMap(final Map<String, ?> attributes) {
        return attributes
            .entrySet()
            .stream()
            .collect(Collectors.toMap(Map.Entry::getKey,
                value -> {
                    val values = CollectionUtils.toCollection(value.getValue());
                    val attributeValues = values
                        .stream()
                        .map(attrValue -> AttributeValue.builder()
                            .string(attrValue.toString())
                            .build())
                        .toList();
                    return AttributeValue.builder().set(attributeValues).build();
                }));
    }

    protected VerifiedPermissionsClient buildAmazonVerifiedPermissionsClient() {
        val resolver = SpringExpressionLanguageValueResolver.getInstance();
        val provider = ChainingAWSCredentialsProvider.getInstance(
            resolver.resolve(credentialAccessKey),
            resolver.resolve(credentialSecretKey));
        val clientConfig = ClientOverrideConfiguration.builder()
            .apiCallTimeout(Duration.ofSeconds(5))
            .apiCallAttemptTimeout(Duration.ofSeconds(5))
            .retryStrategy(RetryMode.STANDARD)
            .build();
        return VerifiedPermissionsClient.builder()
            .defaultsMode(DefaultsMode.STANDARD)
            .region(StringUtils.isBlank(region) ? Region.US_EAST_1 : Region.of(resolver.resolve(region)))
            .credentialsProvider(provider)
            .overrideConfiguration(clientConfig)
            .build();
    }
}
