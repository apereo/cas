
const assert = require("assert");
const cas = require("../../cas.js");

const redirectUri = "https://localhost:9859/anything/cas";
const apiResource = "https://localhost:9859/anything/api";
const reportsResource = "https://localhost:9859/anything/reports";
const clientCredentials = `Basic ${Buffer.from("client:secret").toString("base64")}`;

async function requestTokens(params, successHandler, failureHandler) {
    await cas.doPost(`https://localhost:8443/cas/oauth2.0/token?${params}`, "", {
        "Content-Type": "application/json"
    }, successHandler, failureHandler ?? ((error) => {
        throw `Token request failed: ${error}`;
    }));
}

async function verifyAudience(token, expectedAudience) {
    await cas.doGet(`https://localhost:8443/cas/oauth2.0/introspect?token=${token}`,
        (res) => {
            cas.log(res.data);
            assert(res.data.active === true);
            const audience = Array.isArray(res.data.aud) ? res.data.aud : [res.data.aud];
            assert.deepStrictEqual([...audience].sort(), [...expectedAudience].sort());
        }, (error) => {
            throw `Introspection failed: ${error}`;
        }, {
            "Authorization": clientCredentials,
            "Content-Type": "application/json"
        });
}

async function verifyRefreshWithResource(refreshToken, resource, expectedAudience) {
    let params = `client_id=client&client_secret=secret&grant_type=refresh_token&refresh_token=${refreshToken}`;
    if (resource !== undefined) {
        params += `&resource=${encodeURIComponent(resource)}`;
    }
    let accessToken = null;
    await requestTokens(params, (res) => {
        accessToken = res.data.access_token;
    });
    assert(accessToken !== null);
    await verifyAudience(accessToken, expectedAudience);
}

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLogout(page);

    const scopes = encodeURIComponent("read write profile email");
    const parameters = `client_id=client&scope=${scopes}&state=9qa3&response_type=code&redirect_uri=${redirectUri}`;

    await cas.log("A resource the client is not allowed to request is refused");
    await cas.goto(page, `https://localhost:8443/cas/oauth2.0/authorize?${parameters}&resource=${encodeURIComponent("https://unknown.example.org/")}`);
    await cas.sleep(1000);
    assert(await cas.assertParameter(page, "error") === "invalid_target");
    await cas.separator();

    const resources = `&resource=${encodeURIComponent(apiResource)}&resource=${encodeURIComponent(reportsResource)}`;
    const url = `https://localhost:8443/cas/oauth2.0/authorize?${parameters}${resources}`;

    await cas.goto(page, url);
    await cas.logPage(page);
    await cas.sleep(1000);
    await cas.loginWith(page);
    await cas.sleep(1000);
    await cas.assertVisibility(page, "#resources");
    await cas.assertInnerTextContains(page, "#resources", apiResource);
    await cas.assertInnerTextContains(page, "#resources", reportsResource);
    await cas.click(page, "#allow");
    await cas.waitForNavigation(page);
    await cas.sleep(2000);
    const code = await cas.assertParameter(page, "code");
    await cas.log(`OAuth code ${code}`);

    let accessTokenParams = "client_id=client&";
    accessTokenParams += "client_secret=secret&";
    accessTokenParams += "grant_type=authorization_code&";
    accessTokenParams += `redirect_uri=${redirectUri}`;

    await cas.log("The token request may not name a resource the code was not granted for");
    await requestTokens(`${accessTokenParams}&code=${code}&resource=${encodeURIComponent("https://localhost:9859/anything/other")}`,
        (res) => {
            throw `Token request should have failed: ${res.data}`;
        }, (error) => {
            assert(error.response.status === 400);
            assert(error.response.data.error === "invalid_target");
        });

    await cas.log("The access token is narrowed to the resource named by the token request");
    let accessToken = null;
    let refreshToken = null;
    await requestTokens(`${accessTokenParams}&code=${code}&resource=${encodeURIComponent(apiResource)}`, (res) => {
        cas.log(res.data);
        assert(res.data.access_token !== undefined);
        assert(res.data.refresh_token !== undefined);
        assert(res.data.scope !== undefined);

        assert(res.data.scope.includes("read"));
        assert(res.data.scope.includes("write"));
        assert(res.data.scope.includes("profile"));
        assert(res.data.scope.includes("email"));

        accessToken = res.data.access_token;
        refreshToken = res.data.refresh_token;
    });

    assert(accessToken !== null);
    await verifyAudience(accessToken, ["client", apiResource]);

    await cas.log("The refresh token keeps every resource of the grant");
    await verifyRefreshWithResource(refreshToken, reportsResource, ["client", reportsResource]);
    await verifyRefreshWithResource(refreshToken, undefined, ["client", apiResource, reportsResource]);
    await requestTokens(`client_id=client&client_secret=secret&grant_type=refresh_token&refresh_token=${refreshToken}&resource=${encodeURIComponent("https://unknown.example.org/")}`,
        (res) => {
            throw `Refresh request should have failed: ${res.data}`;
        }, (error) => {
            assert(error.response.status === 400);
            assert(error.response.data.error === "invalid_target");
        });

    const params = new URLSearchParams();
    params.append("access_token", accessToken);
    
    await cas.doPost("https://localhost:8443/cas/oauth2.0/profile", params, {},
        (res) => {
            const result = res.data;
            assert(result.id === "casuser");
            assert(result.client_id === "client");
            assert(result.service === redirectUri);
            assert(result.attributes.oauthClientId === "client");
            assert(result.attributes.organization === "apereo");
            assert(result.attributes.email === "casuser@apereo.org");
            assert(result.attributes.username === "casuser");
        }, (error) => {
            throw error;
        });

    await cas.closeBrowser(browser);
})();
