
const assert = require("assert");
const cas = require("../../cas.js");
const express = require("express");

const SERVICE = "https://localhost:9859/anything/cas";

/**
 * The Groovy trigger returns the chained provider, so the user picks one.
 *
 * @param browser the browser
 */
async function verifyGroovyTrigger(browser) {
    const page = await cas.newPage(browser);
    await cas.gotoLogin(page, SERVICE);
    await cas.loginWith(page);
    await cas.sleep(2000);
    await cas.assertVisibility(page, "#mfa-gauth");
    await cas.assertVisibility(page, "#mfa-webauthn");
}

/**
 * The REST trigger posts the principal and the service to an endpoint, which answers with the provider to use or
 * with nothing when no multifactor authentication is needed.
 *
 * @param browser the browser
 */
async function verifyRestTrigger(browser) {
    const requests = [];
    const app = express();
    app.use(express.json());
    app.post("/mfa", (req, res) => {
        cas.log(`Multifactor trigger asked about ${JSON.stringify(req.body)}`);
        requests.push(req.body);
        res.status(200).send(req.body.principalId === "casuser" ? "mfa-gauth" : "");
    });
    const server = await new Promise((resolve) => {
        const instance = app.listen(5619, () => resolve(instance));
    });
    try {
        let context = await browser.createBrowserContext();
        let page = await cas.newPage(context);
        await cas.gotoLogin(page, SERVICE);
        await cas.loginWith(page);
        await cas.sleep(2000);
        await cas.assertInnerTextStartsWith(page, "#login h2", "Your account is not registered");
        await context.close();
        assert(requests.some((request) => request.principalId === "casuser" && request.serviceId === SERVICE));

        context = await browser.createBrowserContext();
        page = await cas.newPage(context);
        await cas.gotoLogin(page, SERVICE);
        await cas.loginWith(page, "casnomfa", "Mellon");
        await cas.sleep(2000);
        await cas.assertTicketParameter(page);
        await context.close();
    } finally {
        server.close();
    }
}

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    if (process.env.SCENARIO_VARIATION === "rest") {
        await verifyRestTrigger(browser);
    } else {
        await verifyGroovyTrigger(browser);
    }
    await cas.closeBrowser(browser);
})();
