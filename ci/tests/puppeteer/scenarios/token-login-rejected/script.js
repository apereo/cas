const cas = require("../../cas.js");
const assert = require("assert");

const SIGNING_SECRET = "Jq3wB7zH5nVxR2kP9cTfL4mYsD8gE6aU1oWiN0bQhZrXtKvCjMeSpFyGdAlOuI2w";
const WRONG_SECRET = "Zx9vB3nM6qW1eR4tY7uI0oP2aS5dF8gH1jK4lZ7xC0vB3nM6qW9eR2tY5uI8oP1a";
const SIGNED_SERVICE = "https://localhost:9859/anything/signed";
const SERVICE_WITHOUT_SECRET = "https://localhost:9859/anything/nosecret";
const UNREGISTERED_SERVICE = "https://localhost:9859/anything/unregistered";

const base64Url = (value) => Buffer.from(JSON.stringify(value)).toString("base64url");

/*
 * Builds an unsecured JWT (alg "none") by hand, since the JWT libraries refuse to produce one
 * when asked to sign; the signature part is left empty as RFC 7519 requires.
 */
function unsignedToken(claims) {
    return `${base64Url({alg: "none", typ: "JWT"})}.${base64Url(claims)}.`;
}

/*
 * Keeps the header and the signature of a valid token and swaps in a payload for another user,
 * which a verifier that checks the signature must refuse.
 */
function tamperedToken(token) {
    const [header, , signature] = token.split(".");
    const claims = {sub: "admin", iat: Math.floor(Date.now() / 1000)};
    return `${header}.${base64Url(claims)}.${signature}`;
}

async function attemptLogin(page, service, token) {
    await cas.gotoLogout(page);
    await cas.goto(page, `https://localhost:8443/cas/login?service=${encodeURIComponent(service)}&token=${token}`);
    await cas.sleep(1000);
}

async function assertAccepted(page, token) {
    await attemptLogin(page, SIGNED_SERVICE, token);
    const ticket = await cas.assertTicketParameter(page);
    const body = await cas.validateTicket(SIGNED_SERVICE, ticket);
    assert(body.serviceResponse.authenticationSuccess.user === "casuser");
    await cas.gotoLogin(page);
    await cas.assertCookie(page);
}

async function assertRejected(page, description, token, service = SIGNED_SERVICE) {
    await cas.log(`Presenting ${description}`);
    await attemptLogin(page, service, token);
    await cas.assertTicketParameter(page, false);
    await cas.assertCookie(page, false);
    if (service !== UNREGISTERED_SERVICE) {
        await cas.assertVisibility(page, "#username");
    } else {
        await cas.assertInnerText(page, "#content h2", "Application Not Authorized to Use CAS");
    }
}

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);

    const now = Math.floor(Date.now() / 1000);
    const valid = await cas.createJwt({sub: "casuser"}, SIGNING_SECRET, "HS256", {expiresIn: "5m"});
    await cas.log("A token signed with the secret of the service logs the user in");
    await assertAccepted(page, valid);

    await assertRejected(page, "a token whose payload was changed after signing", tamperedToken(valid));
    await assertRejected(page, "an expired token",
        await cas.createJwt({sub: "casuser", iat: now - 7200, exp: now - 3600}, SIGNING_SECRET, "HS256"));
    await assertRejected(page, "a token signed with another secret",
        await cas.createJwt({sub: "casuser"}, WRONG_SECRET, "HS256", {expiresIn: "5m"}));
    await assertRejected(page, "an unsigned token for a service that requires a signature",
        unsignedToken({sub: "casuser", iat: now, exp: now + 300}));
    await assertRejected(page, "a valid token for a service that is not registered", valid, UNREGISTERED_SERVICE);
    await assertRejected(page, "an unsigned token for a service that defines no signing secret",
        unsignedToken({sub: "casuser", iat: now, exp: now + 300}), SERVICE_WITHOUT_SECRET);
    await assertRejected(page, "a token for a service that defines no signing secret",
        await cas.createJwt({sub: "casuser"}, WRONG_SECRET, "HS256", {expiresIn: "5m"}), SERVICE_WITHOUT_SECRET);

    await cas.log("The service still accepts a valid token after the rejected attempts");
    await assertAccepted(page, valid);

    await cas.closeBrowser(browser);
})();
