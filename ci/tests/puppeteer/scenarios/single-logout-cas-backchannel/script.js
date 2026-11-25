const cas = require("../../cas.js");
const express = require("express");
const assert = require("assert");

const SLO_DELAY = 2000;

function startApplications(logoutRequests) {
    const app = express();
    app.use(express.urlencoded({extended: false}));
    app.use((req, res, next) => {
        if (req.method === "POST") {
            logoutRequests.push({path: req.path, headers: req.headers, body: req.body, receivedAt: Date.now()});
            cas.log(`Received back-channel logout request at ${req.path}`);
        }
        next();
    });
    app.post("/app-a", (req, res) => {
        setTimeout(() => {
            logoutRequests.find((r) => r.path === "/app-a").respondedAt = Date.now();
            res.status(200).send("OK");
        }, SLO_DELAY);
    });
    app.post(/.*/, (req, res) => res.status(200).send("OK"));
    app.get("/loggedout", (req, res) => res.status(200).send("Logged out"));
    app.get(/.*/, (req, res) => res.status(200).send(`Application ${req.path}`));
    return app.listen(5612);
}

function assertLogoutRequest(request, ticket) {
    assert(request.headers["content-type"].startsWith("application/x-www-form-urlencoded"));
    assert.deepEqual(Object.keys(request.body), ["logoutRequest"]);
    const xml = request.body.logoutRequest;
    cas.log(xml);
    assert.match(xml, /^<samlp:LogoutRequest xmlns:samlp="urn:oasis:names:tc:SAML:2.0:protocol" ID="LR-[^"]+" Version="2.0" IssueInstant="[^"]+">/);
    const issueInstant = xml.match(/IssueInstant="([^"]+)"/)[1];
    assert.match(issueInstant, /^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}Z$/);
    assert(Math.abs(Date.now() - Date.parse(issueInstant)) < 5 * 60 * 1000, `IssueInstant ${issueInstant} is not current`);
    const nameId = "<saml:NameID xmlns:saml=\"urn:oasis:names:tc:SAML:2.0:assertion\">casuser</saml:NameID>";
    assert(xml.includes(nameId));
    assert(xml.includes(`<samlp:SessionIndex>${ticket}</samlp:SessionIndex>`));
}

(async () => {
    const logoutRequests = [];
    const server = startApplications(logoutRequests);

    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.goto(page, "https://localhost:8444");
    await cas.sleep(1000);
    await cas.goto(page, "https://localhost:8444/protected");
    await cas.loginWith(page);
    await cas.sleep(1000);
    await cas.logPage(page);
    await cas.assertPageUrlStartsWith(page, "https://localhost:8444/protected");
    await cas.assertInnerTextContains(page, "div.starter-template h2 span", "casuser");

    const tickets = {};
    for (const app of ["app-a", "app-b", "app-c"]) {
        await cas.gotoLogin(page, `http://localhost:5612/${app}`);
        await cas.sleep(1000);
        tickets[app] = await cas.assertTicketParameter(page);
    }
    assert.equal(logoutRequests.length, 0);

    await cas.log("Logging out: back-channel requests are sent synchronously before the logout response");
    const startedAt = Date.now();
    await cas.gotoLogout(page);
    const finishedAt = Date.now();
    await cas.sleep(1000);
    await cas.assertPageUrl(page, "http://localhost:5612/loggedout");
    await cas.assertInnerTextContains(page, "body", "Logged out");

    const paths = logoutRequests.map((r) => r.path);
    await cas.log(`Back-channel logout requests received at ${paths}`);
    assert.equal(paths.filter((p) => p === "/app-a").length, 1);
    assert.equal(paths.filter((p) => p === "/slo/b").length, 1);
    assert(!paths.includes("/app-b"), "The service URL must not be called when the service defines a logoutUrl");
    assert(!paths.some((p) => p.startsWith("/app-c")), "A service with logoutType NONE must not be called");

    const requestA = logoutRequests.find((r) => r.path === "/app-a");
    assertLogoutRequest(requestA, tickets["app-a"]);
    assertLogoutRequest(logoutRequests.find((r) => r.path === "/slo/b"), tickets["app-b"]);
    assert(requestA.respondedAt !== undefined && requestA.respondedAt <= finishedAt,
        "The logout response came back before the back-channel request was answered");
    assert(finishedAt - startedAt >= SLO_DELAY);

    await cas.sleep(1000);
    await cas.goto(page, "https://localhost:8444/protected");
    await cas.sleep(3000);
    await cas.logPage(page);
    await cas.assertPageUrlStartsWith(page, "https://localhost:8443/cas/login?service=");
    await cas.closeBrowser(browser);
    server.close();
})();
