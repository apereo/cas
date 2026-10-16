
const assert = require("assert");
const cas = require("../../cas.js");

const LOGIN_URL = "https://localhost:8443/cas/login";
const USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/155.0.0.0 Safari/537.36";

async function loginExecution() {
    const body = await cas.doRequest(LOGIN_URL, "GET", {"User-Agent": USER_AGENT}, 200);
    const match = /name="execution" value="([^"]+)"/.exec(body);
    assert(match !== null, "Login form does not carry a flow execution");
    return match[1];
}

async function submitLogin(execution) {
    const form = new URLSearchParams({
        username: "casuser",
        password: "Mellon",
        execution: execution,
        _eventId: "submit",
        geolocation: "",
        deviceFingerprint: ""
    });
    let status = 0;
    let cookies = [];
    await cas.doRequest(LOGIN_URL, "POST", {"Content-Type": "application/x-www-form-urlencoded", "User-Agent": USER_AGENT}, 0, form.toString(),
        (res) => {
            status = res.statusCode;
            cookies = res.headers["set-cookie"] ?? [];
        });
    const ticketGrantingCookie = cookies.some((cookie) => cookie.startsWith("TGC="));
    await cas.log(`Login submission answered ${status}; ticket-granting cookie issued: ${ticketGrantingCookie}`);
    return {status, ticketGrantingCookie};
}

function tamper(execution) {
    const index = execution.indexOf("_") + Math.floor((execution.length - execution.indexOf("_")) / 2);
    const replacement = execution.charAt(index) === "A" ? "B" : "A";
    return `${execution.substring(0, index)}${replacement}${execution.substring(index + 1)}`;
}

async function verifyRobustness() {
    await cas.separator();
    await cas.log("Submitting the same login form twice at once must not fail");
    const execution = await loginExecution();
    const results = await Promise.all([submitLogin(execution), submitLogin(execution)]);
    results.forEach((result) => assert(result.status > 0 && result.status < 500, `Double submission answered ${result.status}`));
    assert(results.some((result) => result.ticketGrantingCookie), "Double submission must still log the user in");

    await cas.separator();
    await cas.log("A tampered flow execution must restart the flow without logging in");
    const tampered = await submitLogin(tamper(await loginExecution()));
    assert(tampered.status === 302, `Tampered execution answered ${tampered.status}`);
    assert(!tampered.ticketGrantingCookie, "Tampered execution must not log the user in");

    await cas.separator();
    await cas.log("A badly formatted flow execution must be refused without a server error");
    const malformed = await submitLogin("not-a-flow-execution");
    assert(malformed.status > 0 && malformed.status < 500, `Badly formatted execution answered ${malformed.status}`);
    assert(!malformed.ticketGrantingCookie, "Badly formatted execution must not log the user in");

    await cas.separator();
    await cas.log("Going back to the login page after logging into a service must issue a new service ticket");
    const service = "https://localhost:9859/anything/cas";
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLogin(page, service);
    await cas.loginWith(page);
    await cas.sleep(1000);
    const firstTicket = await cas.assertTicketParameter(page);
    await page.goBack();
    await cas.sleep(2000);
    await cas.logPage(page);
    const secondTicket = await cas.assertTicketParameter(page);
    assert(firstTicket !== secondTicket, "Going back must not replay the previous service ticket");
    await cas.gotoLogin(page);
    await cas.assertCookie(page);
    await cas.closeBrowser(browser);
}

(async () => {
    let browser = await cas.newBrowser(cas.browserOptions());
    let page = await cas.newPage(browser);
    await cas.gotoLogin(page);
    const eventId = await cas.elementValue(page, "input[name=_eventId]");
    const execution = await cas.elementValue(page, "input[name=execution]");
    const geolocation = await cas.elementValue(page, "input[name=geolocation]");
    await cas.log(`Event ID: ${eventId}, Execution: ${execution}, GeoLocation: ${geolocation}`);
    await cas.closeBrowser(browser);

    browser = await cas.newBrowser(cas.browserOptions());
    page = await cas.newPage(browser);
    await cas.goto(page, "https://localhost:8444/cas/login");
    await cas.elementValue(page, "input[name=_eventId]", eventId);
    await cas.elementValue(page, "input[name=execution]", execution);
    await cas.elementValue(page, "input[name=geolocation]", geolocation);
    await cas.loginWith(page);
    await cas.assertCookie(page);
    await cas.assertPageTitle(page, "CAS - Central Authentication Service Log In Successful");
    await cas.assertInnerText(page, "#content div h2", "Log In Successful");
    await cas.closeBrowser(browser);

    await verifyRobustness();
})();
