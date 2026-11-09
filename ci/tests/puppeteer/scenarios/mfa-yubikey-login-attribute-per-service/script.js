const assert = require("assert");
const crypto = require("crypto");
const express = require("express");
const cas = require("../../cas.js");

const SECRET_KEY = "aBIehjui12aK8x82oe5qzGeb0As=";
const PUBLIC_ID = "cccccccccccb";

const OTP = {
    bad: `${PUBLIC_ID}${"b".repeat(32)}`,
    tampered: `${PUBLIC_ID}${"t".repeat(32)}`,
    nonce: `${PUBLIC_ID}${"n".repeat(32)}`,
    replayed: `${PUBLIC_ID}${"r".repeat(32)}`,
    valid: `${PUBLIC_ID}${"v".repeat(32)}`
};

function sign(params) {
    const data = Object.keys(params)
        .filter((key) => key !== "h")
        .sort()
        .map((key) => `${key}=${params[key]}`)
        .join("&");
    return crypto.createHmac("sha1", Buffer.from(SECRET_KEY, "base64")).update(data).digest("base64");
}

function verificationResponse(query) {
    const otp = query.otp;
    const response = {
        nonce: query.nonce,
        otp: otp,
        status: "OK",
        t: new Date().toISOString().replace(/[:.]/g, "")
    };
    if (otp === OTP.bad) {
        response.status = "BAD_OTP";
    } else if (otp === OTP.replayed) {
        response.status = "REPLAYED_OTP";
    } else if (otp === OTP.nonce) {
        response.nonce = "0".repeat(32);
    }
    response.h = sign(response);
    if (otp === OTP.tampered) {
        response.h = sign({...response, status: "BAD_OTP"});
    }
    return Object.keys(response).map((key) => `${key}=${response[key]}`).join("\r\n");
}

async function submitToken(page, token) {
    await cas.type(page, "#token", token);
    await cas.submitForm(page, "#yubiKeyForm", (response) => response.status() === 200);
}

(async () => {
    const requests = [];
    const app = express();
    app.get("/wsapi/2.0/verify", (req, res) => {
        cas.log(`YubiCloud verification request for ${req.query.otp} from client ${req.query.id}`);
        requests.push(req.query);
        res.type("text/plain").send(verificationResponse(req.query));
    });

    const server = app.listen(5611, async () => {
        const service = "https://localhost:9859/anything/cas";
        const browser = await cas.newBrowser(cas.browserOptions());
        const page = await cas.newPage(browser);
        await cas.gotoLogin(page, service);
        await cas.loginWith(page);

        await cas.sleep(1000);
        await cas.assertTextContent(page, "#login h3", "Use your registered YubiKey device(s) to authenticate.");

        await cas.assertInvisibility(page, "button[name=register]");

        for (const [mode, token] of Object.entries(OTP).filter(([name]) => name !== "valid")) {
            await cas.log(`Submitting YubiKey OTP that the verification service answers as ${mode}`);
            await submitToken(page, token);
            await cas.assertTextContentStartsWith(page, "div .banner-danger span", "Authentication attempt has failed");
            await cas.assertCookie(page, false);
        }

        const signedRequest = requests.find((query) => query.otp !== undefined);
        assert(signedRequest !== undefined);
        assert.equal(signedRequest.id, "18421");
        assert.equal(signedRequest.h, sign(signedRequest));

        await cas.log("Submitting YubiKey OTP that the verification service accepts with a valid signature");
        await cas.type(page, "#token", OTP.valid);
        await cas.pressEnter(page);
        await cas.waitForNavigation(page);
        await cas.sleep(1000);
        const ticket = await cas.assertTicketParameter(page);
        const json = await cas.validateTicket(service, ticket);
        const authenticationSuccess = json.serviceResponse.authenticationSuccess;
        assert.equal(authenticationSuccess.user, "casuser");
        assert.equal(authenticationSuccess.attributes.authnContextClass[0], "mfa-yubikey");
        assert(requests.some((query) => query.otp === OTP.valid));

        const endpoints = ["yubikeyAccountRepository/casuser"];
        const baseUrl = "https://localhost:8443/cas/actuator/";
        for (let i = 0; i < endpoints.length; i++) {
            const url = baseUrl + endpoints[i];
            const response = await cas.goto(page, url);
            await cas.logg(`Status: ${response.status()} ${response.statusText()}`);
            assert(response.ok());
        }

        server.close(() => {
            cas.log("Exiting server...");
            cas.closeBrowser(browser);
        });
    });
})();
