const cas = require("../../cas.js");
const assert = require("assert");
const express = require("express");

const OPEN_SERVICE = "https://localhost:9859/anything/open";
const ACCEPT_ONLY_SERVICE = "https://localhost:9859/anything/acceptonly";

/**
 * Handlers are tried in order: the JSON handler gets a random order below Integer.MAX_VALUE and the
 * accept handler keeps Integer.MAX_VALUE, so JsonHandler always runs first. jsonuser exists only in the
 * JSON file, casuser only in the accept handler, bothuser in both.
 * An entry of null means the login must fail; otherwise it lists the handlers that must have succeeded.
 */
const EXPECTED = {
    all: {
        jsonuser: ["JsonHandler"],
        casuser: ["AcceptHandler"],
        bothuser: ["JsonHandler"]
    },
    tryall: {
        jsonuser: null,
        casuser: null,
        bothuser: ["AcceptHandler", "JsonHandler"]
    },
    allhandlers: {
        jsonuser: null,
        casuser: null,
        bothuser: ["AcceptHandler", "JsonHandler"]
    },
    required: {
        jsonuser: null,
        casuser: ["AcceptHandler"],
        bothuser: ["AcceptHandler", "JsonHandler"]
    },
    notprevented: {
        jsonuser: null,
        casuser: null,
        bothuser: ["AcceptHandler", "JsonHandler"]
    }
};

async function verifyLogin(page, username, expectedHandlers) {
    await cas.gotoLogout(page);
    await cas.gotoLogin(page, OPEN_SERVICE);
    const response = await cas.loginWith(page, username, "Mellon");
    await cas.sleep(1000);
    if (expectedHandlers === null) {
        await cas.log(`Login of ${username} must be refused by the authentication policy`);
        assert.equal(response.status(), 401);
        await cas.assertTicketParameter(page, false);
        await cas.assertCookie(page, false);
        await cas.assertVisibility(page, "#username");
        return;
    }
    await cas.log(`Login of ${username} must succeed through ${expectedHandlers}`);
    const ticket = await cas.assertTicketParameter(page);
    const json = await cas.validateTicket(OPEN_SERVICE, ticket);
    const success = json.serviceResponse.authenticationSuccess;
    assert.equal(success.user, username);
    const handlers = [].concat(success.attributes.successfulAuthenticationHandlers).sort();
    assert.deepStrictEqual(handlers, expectedHandlers);
}

async function verifyRequiredHandlerGlobalPolicy(page) {
    await cas.logg("A TGT from JsonHandler alone must not get a ticket for a service that requires AcceptHandler");
    await cas.gotoLogout(page);
    await cas.gotoLogin(page);
    await cas.loginWith(page, "jsonuser", "Mellon");
    await cas.sleep(1000);
    await cas.assertCookie(page);

    await cas.gotoLogin(page, OPEN_SERVICE);
    await cas.sleep(1000);
    await cas.assertTicketParameter(page);

    await cas.gotoLogin(page, ACCEPT_ONLY_SERVICE);
    await cas.sleep(1000);
    await cas.assertPageUrlStartsWith(page, "https://localhost:8443/cas/login");
    await cas.assertTicketParameter(page, false);
    await cas.assertVisibility(page, "#username");

    await cas.logg("A TGT from AcceptHandler gets a ticket for that service");
    await cas.gotoLogout(page);
    await cas.gotoLogin(page, ACCEPT_ONLY_SERVICE);
    await cas.loginWith(page, "casuser", "Mellon");
    await cas.sleep(1000);
    const ticket = await cas.assertTicketParameter(page);
    const json = await cas.validateTicket(ACCEPT_ONLY_SERVICE, ticket);
    assert.equal(json.serviceResponse.authenticationSuccess.user, "casuser");
}

/**
 * RestfulAuthenticationPolicy POSTs the principal as JSON and accepts only 200; 403 and 405 become
 * AccountDisabledException, 423 AccountLockedException, anything else (5xx included) FailedLoginException.
 */
const REST_STATUS = {
    casuser: 200,
    blocked: 403,
    locked: 423,
    broken: 500
};

async function startPolicyServer(requests) {
    const app = express();
    app.use(express.text({type: "*/*"}));
    app.post("/policy", (req, res) => {
        const username = JSON.parse(req.body).id;
        const status = REST_STATUS[username] ?? 404;
        cas.log(`Authentication policy request for ${username} (client ${req.get("x-policy-client")}); answering ${status}`);
        requests.push({username: username, client: req.get("x-policy-client")});
        res.sendStatus(status);
    });
    return new Promise((resolve) => {
        const started = app.listen(5612, () => resolve(started));
    });
}

async function loginToOpenService(page, username) {
    await cas.gotoLogout(page);
    await cas.gotoLogin(page, OPEN_SERVICE);
    const response = await cas.loginWith(page, username, "Mellon");
    await cas.sleep(1000);
    return response;
}

async function verifyAccepted(page, username) {
    await cas.logg(`${username} satisfies the authentication policy`);
    await loginToOpenService(page, username);
    const ticket = await cas.assertTicketParameter(page);
    const json = await cas.validateTicket(OPEN_SERVICE, ticket);
    assert.equal(json.serviceResponse.authenticationSuccess.user, username);
}

async function verifyRefused(page, username, view) {
    await cas.logg(`${username} has valid credentials but is refused by the authentication policy`);
    const response = await loginToOpenService(page, username);
    await cas.assertTicketParameter(page, false);
    await cas.assertCookie(page, false);
    await cas.assertPageUrlStartsWith(page, "https://localhost:8443/cas/login");
    if (view === undefined) {
        assert.equal(response.status(), 401);
        await cas.assertVisibility(page, "#username");
    } else {
        await cas.assertVisibility(page, view);
    }
}

async function verifyScriptedPolicy(variation) {
    const requests = [];
    const server = variation === "rest" ? await startPolicyServer(requests) : undefined;
    const browser = await cas.newBrowser(cas.browserOptions());
    try {
        const page = await cas.newPage(browser);
        await verifyAccepted(page, "casuser");
        await verifyRefused(page, "blocked", "#view-accountdisabled-heading");

        if (variation === "groovy") {
            await cas.logg("Users the script does not reject log in");
            await verifyAccepted(page, "locked");
        }

        if (variation === "rest") {
            await verifyRefused(page, "locked", "#view-accountlocked-heading");
            await cas.logg("A 5xx answer from the policy endpoint fails closed");
            await verifyRefused(page, "broken");
            await cas.logg("Unknown users fail in the handler, so the policy endpoint is never asked");
            await verifyRefused(page, "unknown");

            await cas.log(requests);
            for (const username of Object.keys(REST_STATUS)) {
                assert(requests.some((request) => request.username === username), `No policy request for ${username}`);
            }
            assert(requests.every((request) => request.client === "cas"), "Each request carries the configured header");
            assert(requests.every((request) => request.username !== "unknown"), "Policies only run after a handler succeeded");
        }
    } finally {
        await cas.closeBrowser(browser);
        if (server !== undefined) {
            server.close();
        }
    }
}

(async () => {
    const variation = process.env.SCENARIO_VARIATION;
    await cas.logg(`Running variation ${variation}`);
    if (variation === "groovy" || variation === "rest") {
        await verifyScriptedPolicy(variation);
        return;
    }
    const expected = EXPECTED[variation];
    assert(expected !== undefined, `Unknown variation ${variation}`);

    const browser = await cas.newBrowser(cas.browserOptions());
    try {
        const page = await cas.newPage(browser);
        for (const [username, handlers] of Object.entries(expected)) {
            await verifyLogin(page, username, handlers);
        }
        if (variation === "all") {
            await verifyRequiredHandlerGlobalPolicy(page);
        }
    } finally {
        await cas.closeBrowser(browser);
    }
})();
