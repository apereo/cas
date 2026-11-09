const cas = require("../../cas.js");
const assert = require("assert");

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

(async () => {
    const variation = process.env.SCENARIO_VARIATION;
    const expected = EXPECTED[variation];
    assert(expected !== undefined, `Unknown variation ${variation}`);
    await cas.logg(`Running variation ${variation}`);

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
