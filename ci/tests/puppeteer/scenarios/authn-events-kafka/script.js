const cas = require("../../cas.js");
const assert = require("assert");
const {spawnSync} = require("child_process");

/**
 * Reads every record of the events topic from the start, with its key, through the Kafka CLI in the container.
 * The consumer stops when no record arrives for ten seconds, and may report that as a non-zero exit.
 *
 * @returns the records, each as {key, value}
 */
async function readEvents() {
    const result = spawnSync("docker", ["exec", "kafka-server", "/opt/kafka/bin/kafka-console-consumer.sh",
        "--bootstrap-server", "localhost:9092", "--topic", "cas-events", "--from-beginning",
        "--timeout-ms", "10000", "--property", "print.key=true", "--property", "key.separator=|"]);
    const output = result.stdout.toString();
    await cas.log(`Kafka consumer exited with ${result.status}: ${result.stderr.toString().trim()}`);
    return output.split("\n")
        .filter((line) => line.includes("|{") || line.includes("|["))
        .map((line) => {
            const index = line.indexOf("|");
            return {key: line.substring(0, index), value: line.substring(index + 1)};
        });
}

(async () => {
    const unknownUser = `kafka${Date.now()}`;
    const browser = await cas.newBrowser(cas.browserOptions());
    try {
        const page = await cas.newPage(browser);
        await cas.gotoLogin(page);
        await cas.loginWith(page, unknownUser, "WrongPassword");
        await cas.assertCookie(page, false);
        await cas.gotoLogin(page);
        await cas.loginWith(page);
        await cas.assertCookie(page);
    } finally {
        await cas.closeBrowser(browser);
    }

    await cas.logg("Authentication events are published to the Kafka topic, each keyed by its id");
    let events = [];
    for (let attempt = 1; attempt <= 10; attempt++) {
        events = await readEvents();
        if (events.some((event) => event.value.includes(unknownUser)) && events.some((event) => event.value.includes("\"casuser\""))) {
            break;
        }
        await cas.sleep(2000);
    }
    await cas.log(`Found ${events.length} event(s) in the topic`);
    assert(events.some((event) => event.value.includes(unknownUser)), "The failed login must be published");
    assert(events.some((event) => event.value.includes("\"casuser\"")), "The successful login must be published");
    assert(events.every((event) => event.key.length > 0 && event.key !== "null"), "Every event must be keyed by its id");
})();
