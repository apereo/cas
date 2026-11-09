
const assert = require("assert");
const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await submitLoginFailure(page);
    await cas.assertInnerTextStartsWith(page, "#content div.banner p", "Authentication attempt has failed");
    await submitLoginFailure(page);
    await cas.assertInnerText(page, "#content h2", "Access Denied");
    await cas.assertInnerText(page, "#content p", "You've entered the wrong password for the user too many times. You've been throttled.");

    const url = "https://localhost:8443/cas/actuator/throttles";
    await cas.log(`Trying ${url}`);
    const response = await cas.goto(page, url);
    await cas.log(`${response.status()} ${response.statusText()}`);
    assert(response.ok());

    await verifyThrottledRecords(`${url}?username=CASUSER`, (records) => records.length > 0
        && records.every((record) => record.username === "casuser"));
    await verifyThrottledRecords(`${url}?username=unknown`, (records) => records.length === 0);

    await cas.closeBrowser(browser);
})();

async function submitLoginFailure(page) {
    await cas.gotoLogin(page);
    await cas.loginWith(page, "casuser", "BadPassword1");
}

async function verifyThrottledRecords(url, expectation) {
    await cas.doGet(url, async (res) => {
        assert(res.status === 200);
        assert(expectation(res.data), `Unexpected throttled records from ${url}`);
    }, async (err) => {
        throw err;
    }, {
        "Accept": "application/json",
        "Content-Type": "application/x-www-form-urlencoded"
    });
}
