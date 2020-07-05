package org.apereo.cas.dynamodb;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * This is {@link DynamoDbQueryBuilder}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Getter
@Builder
public class DynamoDbQueryBuilder {
    private final String key;

    private final List<AttributeValue> attributeValue;

    private final ComparisonOperator operator;
}

