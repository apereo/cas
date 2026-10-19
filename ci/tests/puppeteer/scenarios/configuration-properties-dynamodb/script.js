const cas = require("../../cas.js");
const assert = require("assert");

const ACTUATOR = "https://localhost:8443/cas/actuator";
const SOURCE = "bootstrapProperties-DynamoDbPropertySource";
const TABLE = "DynamoDbCasProperties";
const SERVICE = "https://localhost:9859/anything/cas";

async function assertProperty(name, value) {
    await cas.doGet(`${ACTUATOR}/env/${name}`,
        (res) => {
            const property = res.data["property"];
            assert(property["source"] === SOURCE, `${name} comes from ${property["source"]}`);
            assert(property["value"] === value, `${name} is ${property["value"]}`);
        },
        (error) => {
            throw error;
        });
}

async function assertLogin(page, user, password, success) {
    await cas.gotoLogin(page);
    await cas.loginWith(page, user, password);
    await cas.assertCookie(page, success);
    if (success) {
        await cas.gotoLogout(page);
    }
}

(async () => {
    await cas.logg("CAS creates the settings table on startup when it does not exist yet");
    assert((await cas.dynamoDbScan(TABLE)).length === 0);
    const table = await cas.dynamoDbRequest("DescribeTable", {TableName: TABLE});
    assert(table.Table.TableName === TABLE);

    const browser = await cas.newBrowser(cas.browserOptions());
    try {
        const page = await cas.newPage(browser);
        await assertLogin(page, "dynamouser", "p@SSw0rd", false);

        await cas.logg("A setting written straight into DynamoDb is picked up after a refresh");
        await cas.dynamoDbRequest("PutItem", {
            TableName: TABLE,
            Item: {
                name: {S: "cas.authn.accept.users"},
                value: {S: "dynamouser::p@SSw0rd"}
            }
        });
        await cas.refreshContext();
        await assertProperty("cas.authn.accept.users", "dynamouser::p@SSw0rd");
        await assertLogin(page, "dynamouser", "p@SSw0rd", true);

        await cas.logg("A setting changed through CAS is written to DynamoDb and applies after a refresh");
        await cas.gotoLogin(page, SERVICE);
        await cas.loginWith(page, "dynamouser", "p@SSw0rd");
        await cas.assertTicketParameter(page);
        await cas.gotoLogout(page, SERVICE);
        await cas.sleep(1000);
        await cas.assertPageUrlStartsWith(page, "https://localhost:8443/cas/logout");

        const payload = JSON.stringify([{name: "cas.logout.follow-service-redirects", value: "true"}]);
        await cas.doRequest(`${ACTUATOR}/casConfig/update`, "POST", {"Content-Type": "application/json"}, 200, payload);
        const items = await cas.dynamoDbScan(TABLE);
        assert(items.some((item) => item.name.S === "cas.logout.follow-service-redirects" && item.value.S === "true"));
        await cas.refreshContext();
        await assertProperty("cas.logout.follow-service-redirects", "true");

        await cas.gotoLogin(page, SERVICE);
        await cas.loginWith(page, "dynamouser", "p@SSw0rd");
        await cas.assertTicketParameter(page);
        await cas.gotoLogout(page, SERVICE);
        await cas.sleep(1000);
        await cas.assertPageUrlStartsWith(page, SERVICE);
    } finally {
        await cas.closeBrowser(browser);
    }
})();
