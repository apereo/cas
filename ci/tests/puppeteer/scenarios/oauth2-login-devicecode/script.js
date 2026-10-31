const assert = require("assert");
const cas = require("../../cas.js");

const DEVICE_CODE_TTL_SECONDS = 45;
const API_RESOURCE = "https://localhost:9859/anything/api";

async function requestDeviceCode(resource) {
    const scopes = `${encodeURIComponent("read write create")}`;
    let url = `https://localhost:8443/cas/oauth2.0/accessToken?response_type=device_code&client_id=client&scope=${scopes}`;
    if (resource !== undefined) {
        url += `&resource=${encodeURIComponent(resource)}`;
    }
    return cas.doPost(url, "", {
        "Content-Type": "application/json"
    }, (res) => {
        assert(res.data.device_code !== undefined);
        assert(res.data.user_code !== undefined);
        assert(res.data.verification_uri !== undefined);
        assert(res.data.interval !== undefined);
        assert(res.data.expires_in !== undefined);
        return res.data;
    }, (error) => {
        throw `Operation failed to obtain device token: ${error}`;
    });
}

function deviceCodeTokenUrl(data) {
    const params = new URLSearchParams();
    params.append("grant_type", "urn:ietf:params:oauth:grant-type:device_code");
    params.append("client_id", "client");
    params.append("device_code", data.device_code);
    return `https://localhost:8443/cas/oauth2.0/accessToken?${params.toString()}`;
}

async function verifyExpiredDeviceCode(data, issuedAt) {
    const remaining = issuedAt + (DEVICE_CODE_TTL_SECONDS + 2) * 1000 - Date.now();
    if (remaining > 0) {
        await cas.log(`Waiting ${remaining}ms for device code to expire`);
        await cas.sleep(remaining);
    }
    const url = deviceCodeTokenUrl(data);
    for (const expected of ["expired_token", "invalid_grant"]) {
        await cas.doPost(url, "", {
            "Content-Type": "application/json"
        }, (res) => {
            throw `An expired device code must not grant tokens: ${JSON.stringify(res.data)}`;
        }, (error) => {
            assert(error.response.status === 400);
            assert(error.response.data.error === expected, `Expected ${expected} but got ${error.response.data.error}`);
        });
    }
}

async function verifyDeviceCodeForUnknownResource() {
    const url = "https://localhost:8443/cas/oauth2.0/accessToken?response_type=device_code&client_id=client"
        + `&resource=${encodeURIComponent("https://unknown.example.org/")}`;
    await cas.doPost(url, "", {
        "Content-Type": "application/json"
    }, (res) => {
        throw `A device code must not be issued for an unknown resource: ${JSON.stringify(res.data)}`;
    }, (error) => {
        assert(error.response.status === 400);
        assert(error.response.data.error === "invalid_target");
    });
}

(async () => {
    await cas.log("A device authorization request for a resource the client may not request is refused");
    await verifyDeviceCodeForUnknownResource();
    await cas.separator();
    const expiring = await requestDeviceCode();
    const issuedAt = Date.now();
    await verifyDeviceCode(await requestDeviceCode(API_RESOURCE));
    await cas.separator();
    await cas.log("An expired device code must be refused with expired_token, and then be gone");
    await verifyExpiredDeviceCode(expiring, issuedAt);
})();

async function verifyDeviceCode(data) {
    const url = deviceCodeTokenUrl(data);
    await cas.doPost(url, "", {
        "Content-Type": "application/json"
    }, () => {
        throw "Operation must fail";
    }, (error) => {
        assert(error.response.status === 400);
        assert(error.response.data.error === "slow_down");
    });
    await cas.sleep(3000);
    await cas.doPost(url, "", {
        "Content-Type": "application/json"
    }, () => {
        throw "Operation must fail";
    }, (error) => {
        assert(error.response.status === 400);
        assert(error.response.data.error === "authorization_pending");
    });

    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLogout(page);
    await page.goto(data.verification_uri);
    await cas.sleep(1000);
    await cas.logPage(page);
    await cas.loginWith(page);

    await cas.log("An unknown user code must be refused");
    await cas.type(page, "#usercode", "UNKNOWNCODE");
    await cas.pressEnter(page);
    await cas.waitForNavigation(page);
    await cas.sleep(1000);
    await cas.assertVisibility(page, "div.banner-danger");

    await cas.log("A user code posted without the approval token of the form must be refused, and stay unapproved");
    await page.$eval("input[name='approvaltoken']", (input) => input.remove());
    await cas.type(page, "#usercode", data.user_code);
    await cas.pressEnter(page);
    await cas.waitForNavigation(page);
    await cas.sleep(1000);
    await cas.assertVisibility(page, "div.banner-danger");
    const approvalToken = await page.$eval("input[name='approvaltoken']", (input) => input.value);
    assert(approvalToken !== undefined && approvalToken.length > 0);

    await cas.type(page, "#usercode", data.user_code);
    await cas.pressEnter(page);
    await cas.waitForNavigation(page);
    await cas.sleep(2000);
    await cas.assertInvisibility(page, "div.banner-danger");

    await cas.log("A user code that is already approved must not be approved again");
    await page.goto(data.verification_uri);
    await cas.sleep(1000);
    await cas.type(page, "#usercode", data.user_code);
    await cas.pressEnter(page);
    await cas.waitForNavigation(page);
    await cas.sleep(1000);
    await cas.assertVisibility(page, "div.banner-danger");
    await cas.closeBrowser(browser);
    
    const payload = await cas.doPost(url, "", {
        "Content-Type": "application/json"
    }, (res) => {
        assert(res.data.access_token !== undefined);
        assert(res.data.token_type !== undefined);
        assert(res.data.expires_in !== undefined);
        assert(res.data.scope !== undefined);
        assert(res.data.refresh_token !== undefined);
        return res.data;
    }, (error) => {
        throw `Operation failed ${error}`;
    });

    await cas.log("A redeemed device code must not be redeemed again");
    await cas.doPost(url, "", {
        "Content-Type": "application/json"
    }, (res) => {
        throw `Device code was redeemed twice: ${JSON.stringify(res.data)}`;
    }, (error) => {
        assert(error.response.status === 400);
        assert(error.response.data.error === "invalid_grant");
        assert(error.response.data.access_token === undefined);
    });

    await cas.log(`Introspecting access token ${payload.access_token}`);
    const value = "client:secret";
    const buff = Buffer.alloc(value.length, value);
    const authzHeader = `Basic ${buff.toString("base64")}`;
    await cas.log(`Authorization header: ${authzHeader}`);
    await cas.doGet(`https://localhost:8443/cas/oauth2.0/introspect?token=${payload.access_token}`,
        (res) => {
            assert(res.data.active === true);
            assert(res.data.sub === "casuser");
            assert(res.data.uniqueSecurityName === "casuser");
            assert.deepStrictEqual(res.data.aud, ["client", API_RESOURCE]);
        },
        (error) => {
            throw `Introspection operation failed: ${error}`;
        }, {
            "Authorization": authzHeader,
            "Content-Type": "application/json"
        });
}
