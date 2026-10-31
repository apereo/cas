
const cas = require("../../cas.js");
const fs = require("fs");
const path = require("path");
const jose = require("jose");

async function buildToken() {
    const privateKey = await jose.importPKCS8(fs.readFileSync(path.join(__dirname, "private.key"), "utf8"), "RS256");
    const publicKey = await jose.importSPKI(fs.readFileSync(path.join(__dirname, "public.key"), "utf8"), "RSA-OAEP-256");
    const jwt = await new jose.SignJWT({})
        .setProtectedHeader({alg: "RS256"})
        .setSubject("casuser")
        .setIssuedAt()
        .setExpirationTime("5m")
        .sign(privateKey);
    return new jose.CompactEncrypt(new TextEncoder().encode(jwt))
        .setProtectedHeader({alg: "RSA-OAEP-256", enc: "A128CBC-HS256", cty: "JWT"})
        .encrypt(publicKey);
}

async function loginWithToken(page, service, token) {
    await cas.log(`Logging in with SSO token to service ${service}`);
    await cas.gotoLogout(page);
    await cas.goto(page, `https://localhost:8443/cas/login?service=${service}&token=${token}`);
    await cas.sleep(1000);
    await cas.assertTicketParameter(page);
    await cas.gotoLogin(page);
    await cas.assertCookie(page);
    await cas.assertInnerText(page, "#content div h2", "Log In Successful");
    await cas.logg("Login is successful");
}

(async () => {
    const service = "https://localhost:9859/anything/cas";
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLogout(page);

    await cas.log("Logging in with a token signed and encrypted with the keys of the service");
    await loginWithToken(page, service, await buildToken());

    await cas.log("Checking for SSO token in service validation response");
    await cas.gotoLogout(page);
    await cas.gotoLogin(page, service);
    await cas.loginWith(page);
    const ticket = await cas.assertTicketParameter(page);
    const body = await cas.validateTicket(service, ticket, "XML");
    const token = body.match(/<cas:token>(.+)<\/cas:token>/)[1];
    await cas.log(`SSO Token ${token}`);
    await loginWithToken(page, service, token);
    
    await cas.closeBrowser(browser);
})();
