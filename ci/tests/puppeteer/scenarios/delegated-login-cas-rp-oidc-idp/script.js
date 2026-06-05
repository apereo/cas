const assert = require("assert");
const cas = require("../../cas.js");

async function exerciseFlow(page, service) {
    await cas.log(`Running the flow with service ${service}`);

    await cas.goto(page, "http://localhost:8988/realms/cas/protocol/openid-connect/logout");
    await cas.sleep();
    await cas.click(page, "#kc-logout");
    await page.waitForNavigation();
    await cas.sleep();
    await cas.gotoLogout(page);

    await cas.gotoLogin(page, service);
    await cas.assertVisibility(page, "li #Keycloak");
    await cas.click(page, "li #Keycloak");
    await cas.waitForNavigation(page);
    await cas.sleep(2000);
    await cas.screenshot(page);
    await cas.loginWith(page, "caskeycloak", "r2RlZXz6f2h5");
    await cas.sleep(2000);

    await cas.logPage(page);
    if (service !== undefined) {
        const ticket = await cas.assertTicketParameter(page);
        const json = await cas.validateTicket(service, ticket);
        const authenticationSuccess = json.serviceResponse.authenticationSuccess;
        assert(authenticationSuccess.user === "caskeycloak@example.org");
        assert(authenticationSuccess.attributes.name !== undefined);
        assert(authenticationSuccess.attributes.email !== undefined);
        assert(authenticationSuccess.attributes.department !== undefined);
        assert(authenticationSuccess.attributes.cas_role !== undefined);
        assert(authenticationSuccess.attributes.sid !== undefined);
        assert(authenticationSuccess.attributes.access_token !== undefined);
        assert(authenticationSuccess.attributes.refresh_token !== undefined);
    }

    await cas.gotoLogin(page);
    await cas.assertCookie(page);
    await cas.separator();

    await cas.log("Logging out of CAS...");
    await cas.sleep(2000);
    await cas.gotoLogout(page, service);
    await cas.sleep(2000);
    await cas.logPage(page);

    if (service !== undefined) {
        await cas.assertPageUrlStartsWith(page, service);
    }
    await cas.gotoLogin(page);
    await cas.assertCookie(page, false);

    await cas.log("Navigate back to keycloak to confirm keycloak session is destroyed");
    await cas.gotoLogin(page, service);
    await cas.click(page, "li #Keycloak");
    await cas.waitForNavigation(page);
    await cas.sleep(2000);
    await cas.loginWith(page, "caskeycloak", "r2RlZXz6f2h5");
    await cas.sleep(2000);
    if (service !== undefined) {
        const ticket = await cas.assertTicketParameter(page);
        const json = await cas.validateTicket(service, ticket);
        const authenticationSuccess = json.serviceResponse.authenticationSuccess;
        const sid = authenticationSuccess.attributes.sid[0];
        /**
         * This logout token is not signed with a private key from the identity provider
         * but we have turned off the logout token validation here to demonstrate the change.
         */
        const privateKey = "enTHR15K28p0N6f404HaC9Vp1cfIBgQiHhmbgBiO7UHEnSiNJudxtDhPQNFjFQtOVSjEYu0pr5yxEeBAiO6IlA";
        const jwt = await cas.createJwt({
            "jti": "THJZGsQDP26OuwQn",
            "iss": "https://localhost:8989/realms/cas",
            "sid": sid,
            "exp": 185542587100,
            "aud": "kc_client",
            "sub": "casuser",
            "client_id": "caskeycloak",
            "events": {
                "http://schemas.openid.net/event/backchannel-logout": {}
            }
        }, privateKey, "HS512");
        const logoutUrl = `https://localhost:8443/cas/login?logout_token=${jwt}&client_name=Keycloak`;

        await cas.doPost(logoutUrl, "",
            {
                "Content-Type": "application/json"
            }, (res) => assert(res.status === 200),
            (error) => {
                throw `Operation failed: ${error}`;
            });

        await cas.gotoLogin(page);
        await cas.assertCookie(page, false);
    }
}

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);

    const har = await cas.startHar(page);
    await cas.log("Running the flow with a service...");
    await exerciseFlow(page, "https://localhost:9859/anything/cas");
    await cas.separator(3);
    await cas.log("Running the flow without a service...");
    await exerciseFlow(page);

    await cas.stopHar(har);
    await cas.closeBrowser(browser);
})();
