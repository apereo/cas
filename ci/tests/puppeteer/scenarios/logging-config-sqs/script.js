const cas = require("../../cas.js");
const assert = require("assert");

async function awslocal(args) {
    return cas.dockerExec("localstack", ["awslocal", "--output", "json", ...args]);
}

/*
 * Drains the queue the SQS appender writes to until the audit record of the login shows up. The record is written
 * while the login request is handled, so it carries the request's logging context as message attributes, which must
 * be typed and stay within the ten attributes SQS allows on a message.
 */
async function findAuditRecord(queueUrl) {
    for (let attempt = 1; attempt <= 15; attempt++) {
        const output = await awslocal(["sqs", "receive-message", "--queue-url", queueUrl,
            "--max-number-of-messages", "10", "--message-attribute-names", "All", "--wait-time-seconds", "2"]);
        const messages = output.length === 0 ? [] : (JSON.parse(output).Messages ?? []);
        const record = messages.find((message) => message.Body.includes("AUTHENTICATION_SUCCESS") && message.Body.includes("casuser"));
        if (record !== undefined) {
            return record;
        }
        await cas.sleep(1000);
    }
    throw new Error("The audit record of the login never reached the queue");
}

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLogin(page);
    await cas.loginWith(page);
    await cas.assertCookie(page);
    await cas.closeBrowser(browser);

    const queueUrl = JSON.parse(await awslocal(["sqs", "get-queue-url", "--queue-name", "cas-logs"])).QueueUrl;
    const record = await findAuditRecord(queueUrl);
    await cas.log(`Audit record from the queue: ${record.Body}`);
    assert(record.Body.startsWith("INFO | org.apereo.inspektr"));

    const attributes = Object.entries(record.MessageAttributes ?? {});
    await cas.log(`Message attributes: ${JSON.stringify(record.MessageAttributes)}`);
    assert(attributes.length > 0, "The logging context of the request must travel with the message");
    assert(attributes.length <= 10);
    attributes.forEach(([, value]) => assert(value.DataType === "String" && value.StringValue.length > 0));

    const tags = JSON.parse(await awslocal(["sqs", "list-queue-tags", "--queue-url", queueUrl])).Tags;
    assert(tags.application === "cas");
})();
