const assert = require("assert");
const crypto = require("crypto");
const http = require("http");
const https = require("https");
const path = require("path");
const cas = require("../../cas.js");

const CAS_PREFIX = "https://localhost:8443/cas";
const CAS_SERVICE = "https://localhost:9859/anything/cas";
const PROTECTED_SERVICE = "https://localhost:9859/anything/protected";
const RESTRICTED_SERVICE = "https://localhost:9859/anything/restricted";
const PROXY_SERVICE = "https://localhost:9859/anything/proxy";
const PROXIED_SERVICE = "https://localhost:9859/anything/sample";
const MFA_SERVICE = "https://localhost:9859/anything/mfa";
const SAML_SP = "http://localhost:9443/simplesaml/module.php/core/authenticate.php?as=default-sp";
const OIDC_CLIENT_ID = "client";
const OIDC_CLIENT_SECRET = "secret";
const OIDC_REDIRECT_URI = "http://localhost:9889/anything/oidc";

const LOAD_USERS = 10;
const LOAD_ITERATIONS = parseInt(process.env.STATELESS_LOAD_ITERATIONS ?? "200", 10);
const LOAD_CONCURRENCY = parseInt(process.env.STATELESS_LOAD_CONCURRENCY ?? "20", 10);
const BROWSER_CONCURRENCY = parseInt(process.env.STATELESS_LOAD_BROWSERS ?? "4", 10);
const SERVICE_TICKET_TIME_TO_KILL_SECONDS = 20;
const MAX_COOKIE_SIZE = 4096;
const pendingExpirations = [];
const USER_AGENT = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) StatelessLoad/1.0";

const HTTPS_AGENT = new https.Agent({rejectUnauthorized: false, keepAlive: true, maxSockets: LOAD_CONCURRENCY * 4});
const HTTP_AGENT = new http.Agent({keepAlive: true, maxSockets: LOAD_CONCURRENCY * 4});

class CookieJar {
    constructor(cookies = new Map()) {
        this.cookies = new Map(cookies);
    }

    store(setCookies = []) {
        for (const setCookie of setCookies) {
            const [pair, ...attributes] = setCookie.split(";");
            assert(pair.length < MAX_COOKIE_SIZE, `Cookie ${pair.split("=")[0]} is ${pair.length} bytes`);
            const separator = pair.indexOf("=");
            const name = pair.substring(0, separator).trim();
            const value = pair.substring(separator + 1).trim();
            const expired = value === "" || attributes.some((attribute) => /^\s*max-age=0\s*$/i.test(attribute));
            if (expired) {
                this.cookies.delete(name);
            } else {
                this.cookies.set(name, value);
            }
        }
    }

    get(name) {
        return this.cookies.get(name);
    }

    with(name, value) {
        const jar = new CookieJar(this.cookies);
        jar.cookies.set(name, value);
        return jar;
    }

    header() {
        return [...this.cookies].map(([name, value]) => `${name}=${value}`).join("; ");
    }
}

function request(method, url, {jar = undefined, headers = {}, body = undefined} = {}) {
    return new Promise((resolve, reject) => {
        const target = new URL(url);
        const secure = target.protocol === "https:";
        const casRequest = target.host === "localhost:8443";
        const allHeaders = {"User-Agent": USER_AGENT, ...headers};
        if (jar !== undefined && casRequest && jar.header().length > 0) {
            allHeaders["Cookie"] = jar.header();
        }
        if (body !== undefined) {
            allHeaders["Content-Length"] = Buffer.byteLength(body);
        }
        const client = secure ? https : http;
        const options = {method, headers: allHeaders, agent: secure ? HTTPS_AGENT : HTTP_AGENT, timeout: 30000};
        const req = client.request(target, options, (res) => {
            const chunks = [];
            res.setEncoding("utf8");
            res.on("data", (chunk) => chunks.push(chunk));
            res.on("end", () => {
                if (jar !== undefined && casRequest) {
                    jar.store(res.headers["set-cookie"]);
                }
                resolve({status: res.statusCode, headers: res.headers, body: chunks.join("")});
            });
        });
        req.on("timeout", () => req.destroy(new Error(`Timed out: ${method} ${url}`)));
        req.on("error", reject);
        if (body !== undefined) {
            req.write(body);
        }
        req.end();
    });
}

function postForm(url, parameters, jar = undefined) {
    return request("POST", url, {
        jar,
        body: new URLSearchParams(parameters).toString(),
        headers: {"Content-Type": "application/x-www-form-urlencoded"}
    });
}

async function followRedirects(url, jar, destination, maxRedirects = 10) {
    let current = url;
    for (let count = 0; count <= maxRedirects; count++) {
        if (current.startsWith(destination)) {
            return current;
        }
        const response = await request("GET", current, {jar});
        if (response.status < 300 || response.status >= 400 || response.headers.location === undefined) {
            throw new Error(`Expected a redirect from ${current} toward ${destination} but received ${response.status}`);
        }
        current = new URL(response.headers.location, current).toString();
    }
    throw new Error(`Too many redirects toward ${destination}`);
}

function tamper(value) {
    let index = Math.floor(value.length * 2 / 3);
    while (value[index] === "." || value[index] === "-") {
        index++;
    }
    const replacement = value[index] === "A" ? "B" : "A";
    return `${value.substring(0, index)}${replacement}${value.substring(index + 1)}`;
}

function percentile(values, rank) {
    if (values.length === 0) {
        return 0;
    }
    const sorted = [...values].sort((first, second) => first - second);
    return sorted[Math.min(sorted.length - 1, Math.max(0, Math.ceil(rank / 100 * sorted.length) - 1))];
}

async function loginOverHttp(username, password = "Mellon") {
    const jar = new CookieJar();
    const loginPage = await request("GET", `${CAS_PREFIX}/login`, {jar});
    assert.equal(loginPage.status, 200);
    const execution = /name="execution" value="([^"]+)"/.exec(loginPage.body);
    assert(execution !== null, "Login form does not carry a flow execution");
    const response = await postForm(`${CAS_PREFIX}/login`, {
        username,
        password,
        execution: execution[1],
        _eventId: "submit",
        geolocation: "",
        deviceFingerprint: ""
    }, jar);
    return {status: response.status, jar, ticketGrantingCookie: jar.get("TGC")};
}

async function ssoServiceTicket(jar, service) {
    const response = await request("GET", `${CAS_PREFIX}/login?service=${encodeURIComponent(service)}`, {jar});
    if (response.status !== 302 || response.headers.location === undefined) {
        return {status: response.status, ticket: null};
    }
    return {status: response.status, ticket: new URL(response.headers.location).searchParams.get("ticket")};
}

async function validateTicket(service, ticket, endpoint = "serviceValidate") {
    const url = `${CAS_PREFIX}/p3/${endpoint}?service=${encodeURIComponent(service)}&ticket=${encodeURIComponent(ticket)}&format=JSON`;
    const response = await request("GET", url);
    return JSON.parse(response.body).serviceResponse;
}

async function exchangeAuthorizationCode(code) {
    const response = await postForm(`${CAS_PREFIX}/oidc/token`, {
        client_id: OIDC_CLIENT_ID,
        client_secret: OIDC_CLIENT_SECRET,
        grant_type: "authorization_code",
        redirect_uri: OIDC_REDIRECT_URI,
        code
    });
    return {status: response.status, body: response.status === 200 ? JSON.parse(response.body) : response.body};
}

async function fetchUserProfile(accessToken) {
    const response = await postForm(`${CAS_PREFIX}/oidc/profile`, {access_token: accessToken});
    return {status: response.status, body: response.status === 200 ? JSON.parse(response.body) : response.body};
}

async function oidcCodeFlow(jar, username) {
    const state = crypto.randomUUID();
    const authorizeUrl = `${CAS_PREFIX}/oidc/authorize?response_type=code&client_id=${OIDC_CLIENT_ID}`
        + `&scope=openid%20profile&redirect_uri=${encodeURIComponent(OIDC_REDIRECT_URI)}&state=${state}`;
    const location = new URL(await followRedirects(authorizeUrl, jar, OIDC_REDIRECT_URI));
    assert.equal(location.searchParams.get("state"), state);
    const code = location.searchParams.get("code");
    assert(code !== null, `No authorization code in ${location}`);

    const tokens = await exchangeAuthorizationCode(code);
    assert.equal(tokens.status, 200, `Token request failed: ${JSON.stringify(tokens.body)}`);
    assert(tokens.body.id_token !== undefined);
    const profile = await fetchUserProfile(tokens.body.access_token);
    assert.equal(profile.status, 200);
    assert.equal(profile.body.sub, username);
    assert.equal(profile.body.organization, "apereo");
    return {code, accessToken: tokens.body.access_token, refreshToken: tokens.body.refresh_token};
}

async function proxyFlow(jar, username) {
    const serviceTicket = await ssoServiceTicket(jar, PROXY_SERVICE);
    assert(serviceTicket.ticket !== null, "No service ticket for the proxying service");
    const proxyValidation = await request("GET", `${CAS_PREFIX}/p3/proxyValidate?service=${encodeURIComponent(PROXY_SERVICE)}`
        + `&ticket=${encodeURIComponent(serviceTicket.ticket)}&pgtUrl=${encodeURIComponent(PROXIED_SERVICE)}`);
    const proxyGrantingTicket = /<cas:proxyGrantingTicket>(.*?)<\/cas:proxyGrantingTicket>/.exec(proxyValidation.body);
    assert(proxyGrantingTicket !== null, `No proxy-granting ticket in ${proxyValidation.body}`);

    const proxyResponse = await request("GET", `${CAS_PREFIX}/proxy?service=${encodeURIComponent(PROXIED_SERVICE)}`
        + `&pgt=${encodeURIComponent(proxyGrantingTicket[1])}`);
    const proxyTicket = /<cas:proxyTicket>(.*?)<\/cas:proxyTicket>/.exec(proxyResponse.body);
    assert(proxyTicket !== null, `No proxy ticket in ${proxyResponse.body}`);

    const validation = await validateTicket(PROXIED_SERVICE, proxyTicket[1], "proxyValidate");
    assert(validation.authenticationSuccess !== undefined, JSON.stringify(validation));
    assert.equal(validation.authenticationSuccess.user, username);
    assert.equal(validation.authenticationSuccess.attributes.organization[0], "apereo");
}

async function assertTicketGrantingCookieSize(page) {
    const cookies = await page.cookies(`${CAS_PREFIX}/login`);
    const cookie = cookies.find((candidate) => candidate.name === "TGC");
    assert(cookie !== undefined, "No ticket-granting cookie");
    const size = cookie.name.length + cookie.value.length;
    await cas.log(`Ticket-granting cookie size is ${size} bytes`);
    assert(size < MAX_COOKIE_SIZE, `Ticket-granting cookie is ${size} bytes`);
    for (const sessionCookie of cookies.filter((candidate) => candidate.name.startsWith("DISSESSION"))) {
        const sessionCookieSize = sessionCookie.name.length + sessionCookie.value.length;
        await cas.log(`Session cookie ${sessionCookie.name} size is ${sessionCookieSize} bytes`);
        assert(sessionCookieSize < MAX_COOKIE_SIZE, `Session cookie ${sessionCookie.name} is ${sessionCookieSize} bytes`);
    }
    return cookie;
}

async function verifyInterruptNotification(context) {
    const page = await cas.newPage(context);
    await cas.gotoLogin(page, CAS_SERVICE);
    await cas.loginWith(page);
    await cas.sleep(1000);
    await cas.assertTextContent(page, "#content h1", "Authentication Interrupt");
    await cas.assertTextContent(page, "#interruptMessage", "We interrupted your login");
    await cas.assertVisibility(page, "#interruptLinks");
    await cas.assertVisibility(page, "#field1");
    await cas.assertCookie(page, false);
    await cas.submitForm(page, "#fm1");
    await cas.sleep(1000);

    const ticket = await cas.assertTicketParameter(page);
    const validation = await validateTicket(CAS_SERVICE, ticket);
    assert.equal(validation.authenticationSuccess.user, "casuser");
    assert.equal(validation.authenticationSuccess.attributes.employeeNumber[0], "123456");
    assert.equal(validation.authenticationSuccess.attributes.organization[0], "apereo");
    await assertTicketGrantingCookieSize(page);

    await cas.log("Single sign-on must not interrupt again, and must see attributes from the attribute repository");
    for (const service of [PROTECTED_SERVICE, RESTRICTED_SERVICE]) {
        await cas.gotoLogin(page, service);
        await cas.sleep(1000);
        const ssoTicket = await cas.assertTicketParameter(page);
        const ssoValidation = await validateTicket(service, ssoTicket);
        assert.equal(ssoValidation.authenticationSuccess.user, "casuser");
        assert.equal(ssoValidation.authenticationSuccess.attributes.employeeNumber[0], "123456");
    }
    await cas.gotoLogout(page);
    await cas.assertCookie(page, false);
}

async function verifyBlockedInterruptNotification(context) {
    const page = await cas.newPage(context);
    await cas.gotoLogin(page, CAS_SERVICE);
    await cas.loginWith(page, "casblock", "Mellon");
    await cas.sleep(1000);
    await cas.assertTextContent(page, "#content h1", "Authentication Interrupt");
    await cas.assertTextContent(page, "#interruptMessage", "You are blocked");
    await cas.assertInvisibility(page, "#fm1");
    await cas.assertMissingParameter(page, "ticket");
}

function watchCookieSizes(page) {
    const oversized = [];
    page.on("response", (response) => {
        const header = response.headers()["set-cookie"];
        for (const setCookie of header ? header.split("\n") : []) {
            const pair = setCookie.split(";")[0];
            if (pair.length >= MAX_COOKIE_SIZE) {
                oversized.push(`${pair.split("=")[0]} is ${pair.length} bytes, set by ${response.url()}`);
            }
        }
    });
    return oversized;
}

async function verifySamlIdentityProviderWithOidc(context, username) {
    const page = await cas.newPage(context);
    const oversizedCookies = watchCookieSizes(page);
    await cas.goto(page, SAML_SP);
    await cas.sleep(2000);
    await cas.loginWith(page, username, "Mellon");
    await cas.sleep(3000);
    await page.waitForSelector("#table_with_attributes", {visible: true});
    const authData = JSON.parse(await cas.innerHTML(page, "details pre"));
    assert(authData["Attributes"]["organization"].includes("apereo"), JSON.stringify(authData));
    await assertTicketGrantingCookieSize(page);

    const state = crypto.randomUUID();
    await cas.goto(page, `${CAS_PREFIX}/oidc/authorize?response_type=code&client_id=${OIDC_CLIENT_ID}`
        + `&scope=openid%20profile&redirect_uri=${OIDC_REDIRECT_URI}&state=${state}`);
    await cas.sleep(2000);
    assert.deepEqual(oversizedCookies, [], "Browsers drop cookies of 4096 bytes or more");
    await cas.assertPageUrlStartsWith(page, OIDC_REDIRECT_URI);
    const code = await cas.assertParameter(page, "code");
    const tokens = await exchangeAuthorizationCode(code);
    assert.equal(tokens.status, 200);
    const profile = await fetchUserProfile(tokens.body.access_token);
    assert.equal(profile.body.sub, username);
    await page.close();
}

async function verifyDelegatedSamlIdentityProvider(context) {
    await cas.log("Loading the external identity provider metadata, which was not reachable when CAS started");
    await cas.doRequest(`${CAS_PREFIX}/sp/metadata`, "GET", {}, 200);
    await cas.doRequest(`${CAS_PREFIX}/sp/idp/metadata`, "GET", {}, 200);

    const page = await cas.newPage(context);
    await cas.goto(page, SAML_SP);
    await cas.sleep(2000);
    await cas.assertVisibility(page, "li #SAML2Client");
    await cas.click(page, "li #SAML2Client");
    await cas.sleep(6000);
    await cas.loginWith(page, "user1", "password");
    await cas.sleep(4000);
    await cas.logPage(page);
    await page.waitForSelector("#table_with_attributes", {visible: true});
    await cas.assertInnerTextContains(page, "#content p", "status page of SimpleSAMLphp");
    await assertTicketGrantingCookieSize(page);

    await cas.gotoLogin(page, CAS_SERVICE);
    await cas.sleep(1000);
    const ticket = await cas.assertTicketParameter(page);
    const validation = await validateTicket(CAS_SERVICE, ticket);
    assert(validation.authenticationSuccess !== undefined, JSON.stringify(validation));
    assert.equal(validation.authenticationSuccess.attributes.organization[0], "apereo");
    assert.equal(validation.authenticationSuccess.attributes.credentialType[0], "ClientCredential");
    assert.equal(validation.authenticationSuccess.attributes.authenticationMethod[0], "DelegatedClientAuthenticationHandler");
    assert.equal(validation.authenticationSuccess.attributes.clientName[0], "SAML2Client");
}

async function verifyUnhappyPaths(context) {
    const page = await cas.newPage(context);
    await cas.gotoLogin(page);
    await cas.loginWith(page, "loaduser0", "Mellon");
    await cas.sleep(1000);
    const cookie = await assertTicketGrantingCookieSize(page);

    await cas.log("A service ticket for one service is rejected for another");
    await cas.gotoLogin(page, CAS_SERVICE);
    const ticket = await cas.assertTicketParameter(page);
    const wrongService = await validateTicket(PROTECTED_SERVICE, ticket);
    assert(wrongService.authenticationFailure !== undefined, JSON.stringify(wrongService));

    await cas.log("Service tickets are not single-use, as documented; expiration is checked after the load runs");
    for (let count = 0; count < 2; count++) {
        const reused = await validateTicket(CAS_SERVICE, ticket);
        assert(reused.authenticationSuccess !== undefined, JSON.stringify(reused));
    }
    pendingExpirations.push({ticket, expiresAt: Date.now() + (SERVICE_TICKET_TIME_TO_KILL_SECONDS + 3) * 1000});

    await cas.log("Access is denied at single sign-on when the attribute repository does not release the required attribute");
    await cas.gotoLogin(page, RESTRICTED_SERVICE);
    await cas.sleep(1000);
    await cas.assertMissingParameter(page, "ticket");

    await cas.log("A tampered ticket-granting cookie asks for credentials");
    const tamperedContext = await context.browser().createBrowserContext();
    const tamperedPage = await cas.newPage(tamperedContext);
    await tamperedPage.setCookie({...cookie, value: tamper(cookie.value)});
    await cas.gotoLogin(tamperedPage, CAS_SERVICE);
    await cas.sleep(1000);
    await cas.assertVisibility(tamperedPage, "#username");
    await cas.assertMissingParameter(tamperedPage, "ticket");
    await tamperedContext.close();

    await cas.log("Logging out removes the cookie, but a copy stays valid until it expires, as documented");
    await cas.gotoLogout(page);
    await cas.assertCookie(page, false);
    const replayContext = await context.browser().createBrowserContext();
    const replayPage = await cas.newPage(replayContext);
    await replayPage.setCookie(cookie);
    await cas.gotoLogin(replayPage, CAS_SERVICE);
    await cas.sleep(1000);
    await cas.assertTicketParameter(replayPage);
    await replayContext.close();
}

async function verifyUnhappyPathsOverHttp(jar, serviceTicket, oidc) {
    const tamperedCookie = await ssoServiceTicket(jar.with("TGC", tamper(jar.get("TGC"))), CAS_SERVICE);
    assert.equal(tamperedCookie.ticket, null, "A tampered ticket-granting cookie produced a service ticket");
    assert.equal(tamperedCookie.status, 200);

    const garbageCookie = await ssoServiceTicket(jar.with("TGC", "garbage"), CAS_SERVICE);
    assert.equal(garbageCookie.ticket, null, "A garbage ticket-granting cookie produced a service ticket");

    const tamperedTicket = await validateTicket(CAS_SERVICE, tamper(serviceTicket));
    assert.equal(tamperedTicket.authenticationFailure?.code, "INVALID_TICKET", JSON.stringify(tamperedTicket));

    const forgedTicket = await validateTicket(CAS_SERVICE, `ST-${crypto.randomUUID()}`);
    assert.equal(forgedTicket.authenticationFailure?.code, "INVALID_TICKET", JSON.stringify(forgedTicket));

    const relabelledTicket = await validateTicket(CAS_SERVICE, `PT-${serviceTicket.substring(3)}`, "proxyValidate");
    assert(relabelledTicket.authenticationFailure !== undefined, JSON.stringify(relabelledTicket));

    if (oidc !== undefined) {
        const tamperedCode = await exchangeAuthorizationCode(tamper(oidc.code));
        assert.notEqual(tamperedCode.status, 200, "A tampered authorization code produced tokens");
        const tamperedAccessToken = await fetchUserProfile(tamper(oidc.accessToken));
        assert.notEqual(tamperedAccessToken.status, 200, "A tampered access token produced a profile");
        if (oidc.refreshToken !== undefined) {
            const refreshTokenAsAccessToken = await fetchUserProfile(oidc.refreshToken);
            assert.notEqual(refreshTokenAsAccessToken.status, 200, "A refresh token was accepted as an access token");
        }
    }
}

async function timed(metrics, name, operation) {
    const start = process.hrtime.bigint();
    try {
        return await operation();
    } finally {
        const elapsed = Number(process.hrtime.bigint() - start) / 1e6;
        if (!metrics.has(name)) {
            metrics.set(name, []);
        }
        metrics.get(name).push(elapsed);
    }
}

async function loadIteration(index, metrics, cookieSizes) {
    const username = `loaduser${index % LOAD_USERS}`;
    const login = await timed(metrics, "login", () => loginOverHttp(username));
    assert.equal(login.status, 200, `Login for ${username} returned ${login.status}`);
    assert(login.ticketGrantingCookie !== undefined, `Login for ${username} returned no ticket-granting cookie`);
    cookieSizes.push(login.ticketGrantingCookie.length + "TGC".length);

    let lastServiceTicket = null;
    for (let count = 0; count < 3; count++) {
        const service = `${CAS_SERVICE}/${index}/${count}`;
        const sso = await timed(metrics, "sso", () => ssoServiceTicket(login.jar, service));
        assert(sso.ticket !== null, `Single sign-on for ${service} returned ${sso.status}`);
        const validation = await timed(metrics, "validate", () => validateTicket(service, sso.ticket));
        assert(validation.authenticationSuccess !== undefined, JSON.stringify(validation));
        assert.equal(validation.authenticationSuccess.user, username);
        assert.equal(validation.authenticationSuccess.attributes.organization[0], "apereo");
        lastServiceTicket = sso.ticket;
    }

    const allowed = await timed(metrics, "sso", () => ssoServiceTicket(login.jar, `${PROTECTED_SERVICE}/${index}`));
    assert(allowed.ticket !== null, `Single sign-on for the protected service returned ${allowed.status}`);
    const denied = await timed(metrics, "sso", () => ssoServiceTicket(login.jar, `${RESTRICTED_SERVICE}/${index}`));
    assert.equal(denied.ticket, null, "The restricted service received a service ticket");

    const oidc = await timed(metrics, "oidc", () => oidcCodeFlow(login.jar, username));
    if (index % 5 === 0) {
        await timed(metrics, "proxy", () => proxyFlow(login.jar, username));
    }
    if (index % 10 === 0) {
        const failed = await timed(metrics, "login", () => loginOverHttp(username, "wrong"));
        assert.equal(failed.status, 401, `A wrong password returned ${failed.status}`);
        assert.equal(failed.ticketGrantingCookie, undefined);
    }
    await timed(metrics, "negative", () => verifyUnhappyPathsOverHttp(login.jar, lastServiceTicket, oidc));
}

async function runLoad() {
    const metrics = new Map();
    const failures = [];
    const cookieSizes = [];
    const counter = {next: 0};
    const start = Date.now();

    const worker = async () => {
        while (counter.next < LOAD_ITERATIONS) {
            const index = counter.next;
            counter.next += 1;
            try {
                await loadIteration(index, metrics, cookieSizes);
            } catch (error) {
                failures.push({index, message: error.message});
            }
        }
    };
    await Promise.all(Array.from({length: LOAD_CONCURRENCY}, () => worker()));

    const elapsedSeconds = (Date.now() - start) / 1000;
    await cas.logb(`Completed ${LOAD_ITERATIONS} iterations with ${LOAD_CONCURRENCY} workers in ${elapsedSeconds.toFixed(1)}s`);
    for (const [name, durations] of metrics) {
        await cas.log(`${name.padEnd(10)} count=${String(durations.length).padStart(5)} `
            + `p50=${percentile(durations, 50).toFixed(0)}ms p95=${percentile(durations, 95).toFixed(0)}ms `
            + `p99=${percentile(durations, 99).toFixed(0)}ms max=${Math.max(...durations).toFixed(0)}ms`);
    }
    await cas.log(`Ticket-granting cookie size: max=${Math.max(...cookieSizes)} bytes`);
    for (const failure of failures.slice(0, 20)) {
        await cas.logr(`Iteration ${failure.index} failed: ${failure.message}`);
    }
    assert.equal(failures.length, 0, `${failures.length} of ${LOAD_ITERATIONS} iterations failed`);
    assert(Math.max(...cookieSizes) < MAX_COOKIE_SIZE);
}

async function runBrowserLoad(browser) {
    const start = Date.now();
    await Promise.all(Array.from({length: BROWSER_CONCURRENCY}, async (_, index) => {
        const context = await browser.createBrowserContext();
        try {
            await verifySamlIdentityProviderWithOidc(context, `loaduser${index % LOAD_USERS}`);
        } finally {
            await context.close();
        }
    }));
    await cas.logb(`Completed ${BROWSER_CONCURRENCY} concurrent SAML2 and OpenID Connect browser sessions in ${Date.now() - start}ms`);
}

async function verifyExpiredServiceTickets() {
    for (const pending of pendingExpirations) {
        const remaining = pending.expiresAt - Date.now();
        if (remaining > 0) {
            await cas.log(`Waiting ${remaining}ms for the service ticket to expire`);
            await cas.sleep(remaining);
        }
        const expired = await validateTicket(CAS_SERVICE, pending.ticket);
        assert.equal(expired.authenticationFailure?.code, "INVALID_TICKET", JSON.stringify(expired));
    }
}

async function verifySimpleMultifactorAuthentication(context, browser) {
    const page = await cas.newPage(context);
    await cas.gotoLogin(page, MFA_SERVICE);
    await cas.loginWith(page, "loaduser1", "Mellon");
    await cas.sleep(2000);
    await cas.assertVisibility(page, "#token");
    const code = await cas.extractFromEmail(browser);

    await cas.log("A code is only accepted in the login flow that sent it");
    const otherContext = await browser.createBrowserContext();
    try {
        const otherPage = await cas.newPage(otherContext);
        await cas.gotoLogin(otherPage, MFA_SERVICE);
        await cas.loginWith(otherPage, "loaduser2", "Mellon");
        await cas.sleep(2000);
        await cas.assertVisibility(otherPage, "#token");
        await cas.type(otherPage, "#token", code);
        await cas.submitForm(otherPage, "#fm1");
        await cas.sleep(1000);
        await cas.assertTextContentStartsWith(otherPage, "div .banner-danger p", "Multifactor authentication attempt has failed");
    } finally {
        await otherContext.close();
    }

    await cas.log("A wrong code is rejected");
    await cas.type(page, "#token", "CASMFA-000000");
    await cas.submitForm(page, "#fm1");
    await cas.sleep(1000);
    await cas.assertTextContentStartsWith(page, "div .banner-danger p", "Multifactor authentication attempt has failed");

    await cas.type(page, "#token", code);
    await cas.submitForm(page, "#fm1");
    await cas.sleep(2000);
    const ticket = await cas.assertTicketParameter(page);
    const validation = await validateTicket(MFA_SERVICE, ticket);
    assert.equal(validation.authenticationSuccess.user, "loaduser1");
    assert(validation.authenticationSuccess.attributes.authnContextClass.includes("mfa-simple"), JSON.stringify(validation));
    await assertTicketGrantingCookieSize(page);

    await cas.log("Single sign-on must not ask for the code again");
    await cas.gotoLogin(page, MFA_SERVICE);
    await cas.sleep(2000);
    const ssoTicket = await cas.assertTicketParameter(page);
    const ssoValidation = await validateTicket(MFA_SERVICE, ssoTicket);
    assert(ssoValidation.authenticationSuccess.attributes.authnContextClass.includes("mfa-simple"), JSON.stringify(ssoValidation));
    await cas.gotoLogout(page);
}

async function verifyHealth() {
    const health = await request("GET", `${CAS_PREFIX}/actuator/health`);
    await cas.log(`Health after load (${health.status}): ${health.body}`);
    assert.equal(JSON.parse(health.body).components.ping.status, "UP");
    const loginPage = await request("GET", `${CAS_PREFIX}/login`);
    assert.equal(loginPage.status, 200);
}

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    let failed = false;
    const steps = [
        ["Interrupt notification with single sign-on", verifyInterruptNotification],
        ["Blocking interrupt notification", verifyBlockedInterruptNotification],
        ["SAML2 identity provider with OpenID Connect single sign-on", (context) => verifySamlIdentityProviderWithOidc(context, "loaduser0")],
        ["Delegation to an external SAML2 identity provider", verifyDelegatedSamlIdentityProvider],
        ["Simple multifactor authentication", (context) => verifySimpleMultifactorAuthentication(context, browser)],
        ["Unhappy paths", verifyUnhappyPaths]
    ];
    try {
        for (const [name, step] of steps) {
            await cas.logb(name);
            const context = await browser.createBrowserContext();
            try {
                await step(context);
            } finally {
                await context.close();
            }
            await cas.separator();
        }
        await cas.logb("Concurrent browser sessions");
        await runBrowserLoad(browser);
        await cas.separator();

        await cas.logb("Load over HTTP");
        await runLoad();
        await verifyExpiredServiceTickets();
        await verifyHealth();
    } catch (e) {
        failed = true;
        throw e;
    } finally {
        await cas.log("Closing connections and the browser");
        HTTPS_AGENT.destroy();
        HTTP_AGENT.destroy();
        await cas.removeDirectoryOrFile(path.join(__dirname, "/saml-md"));
        await cas.closeBrowser(browser);
        if (!failed) {
            await cas.logg("Scenario completed");
            await process.exit(0);
        }
    }
})();
