package org.apereo.cas.dynamodb;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ComparisonOperator;

import java.util.List;

/**
 * This is {@link DynamoDbQueryBuilder}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Getter
@Builder
@ToString
public class DynamoDbQueryBuilder {
    private final String key;

    private final List<AttributeValue> attributeValue;

    private final ComparisonOperator operator;
}

