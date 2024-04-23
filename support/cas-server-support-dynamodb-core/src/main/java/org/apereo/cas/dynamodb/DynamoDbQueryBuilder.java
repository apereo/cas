package org.apereo.cas.dynamodb;

import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
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
@SuperBuilder
@ToString
public class DynamoDbQueryBuilder {
    private final String key;

    private final List<AttributeValue> attributeValue;

    private final ComparisonOperator operator;
}

