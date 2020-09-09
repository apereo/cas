package org.apereo.cas;

import org.apereo.cas.dynamodb.AmazonDynamoDbClientFactoryTests;
import org.apereo.cas.dynamodb.DynamoDbTableUtilsTests;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

/**
 * This is {@link DynamoDbTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@SelectClasses({
    AmazonDynamoDbClientFactoryTests.class,
    DynamoDbTableUtilsTests.class
})
@RunWith(JUnitPlatform.class)
public class DynamoDbTestsSuite {
}
