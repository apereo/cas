
const cas = require("../../cas.js");
const assert = require("assert");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    const service = "https://localhost:9859/anything/1";
    await cas.gotoLogin(page, service);
    await cas.loginWith(page);
    await cas.sleep(2000);
    const ticket = await cas.assertTicketParameter(page);

    await cas.doGet(`https://localhost:8443/cas/actuator/jwtTicketSigningPublicKey?service=${service}`,
        (res) => {
            const publickey = res.data;

            // const keyPath = path.join(__dirname, 'public.key');
            // let publickey = fs.readFileSync(keyPath);

            cas.verifyJwt(ticket, publickey, {
                algorithms: ["RS512"],
                complete: true
            }).then((decoded) => {
                const payload = decoded.payload;
                
                assert(payload.successfulAuthenticationHandlers === "Static Credentials");
                assert(payload.authenticationMethod === "Static Credentials");
                assert(payload.aud === "https://localhost:9859/anything/1");
                assert(payload.credentialType === "UsernamePasswordCredential");
                assert(payload.sub === "casuser");
                assert(payload.username === "casuser");
                assert(payload.email === "casuser@apereo.org");
                assert(payload.name === "CAS");
                assert(payload.gender === "female");
                assert(payload.jti.startsWith("ST-"));
            });
        }, (error) => {
            throw `Introspection operation failed: ${error}`;
        }, {
            "Content-Type": "application/json"
        });
    
    await cas.closeBrowser(browser);
})();
