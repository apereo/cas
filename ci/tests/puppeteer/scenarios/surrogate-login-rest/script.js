const cas = require("../../cas.js");
const assert = require("assert");
const express = require("express");

const SERVICE = "https://localhost:9859/anything/cas";
const ACCOUNTS = {"casuser": ["banderson", "jdoe"]};

(async () => {
    const requests = [];
    const app = express();
    app.get("/surrogate", (req, res) => {
        const {principal, surrogate} = req.query;
        requests.push(req.query);
        const accounts = ACCOUNTS[principal] ?? [];
        if (surrogate === undefined) {
            cas.log(`Listing surrogate accounts of ${principal}: ${accounts}`);
            res.json(accounts);
        } else if (accounts.includes(surrogate)) {
            cas.log(`${principal} may impersonate ${surrogate}`);
            res.sendStatus(200);
        } else {
            cas.log(`${principal} may not impersonate ${surrogate}`);
            res.sendStatus(403);
        }
    });
    const server = await new Promise((resolve) => {
        const started = app.listen(5616, () => resolve(started));
    });

    const browser = await cas.newBrowser(cas.browserOptions());
    try {
        const page = await cas.newPage(browser);

        await cas.logg("The accounts to choose from come from the REST backend");
        await cas.gotoLogin(page, SERVICE);
        await cas.loginWith(page, "+casuser", "Mellon");
        await cas.sleep(1000);
        await cas.assertTextContent(page, "#titlePanel h2", "Choose Account");
        await cas.assertVisibility(page, "#surrogateTarget");
        const options = await page.$$eval("#surrogateTarget option", (elements) => elements.map((element) => element.value));
        await cas.log(`Accounts offered: ${options}`);
        assert(ACCOUNTS.casuser.every((account) => options.includes(account)));
        assert(requests.some((query) => query.principal === "casuser" && query.surrogate === undefined));
        await cas.gotoLogout(page);

        await cas.logg("An account the REST backend allows is impersonated");
        await cas.gotoLogin(page, SERVICE);
        await cas.loginWith(page, "banderson+casuser", "Mellon");
        await cas.sleep(1000);
        const ticket = await cas.assertTicketParameter(page);
        const json = await cas.validateTicket(SERVICE, ticket);
        const authenticationSuccess = json.serviceResponse.authenticationSuccess;
        assert(authenticationSuccess.user === "banderson");
        assert(authenticationSuccess.attributes.surrogateEnabled[0] === true);
        assert(authenticationSuccess.attributes.surrogatePrincipal[0] === "casuser");
        assert(authenticationSuccess.attributes.surrogateUser[0] === "banderson");
        assert(requests.some((query) => query.principal === "casuser" && query.surrogate === "banderson"));
        await cas.gotoLogout(page);

        await cas.logg("An account the REST backend refuses is not impersonated");
        await cas.gotoLogin(page, SERVICE);
        await cas.loginWith(page, "mallory+casuser", "Mellon");
        await cas.sleep(1000);
        await cas.assertTicketParameter(page, false);
        await cas.assertCookie(page, false);
        assert(requests.some((query) => query.principal === "casuser" && query.surrogate === "mallory"));
    } finally {
        await cas.closeBrowser(browser);
        server.close();
    }
})();
