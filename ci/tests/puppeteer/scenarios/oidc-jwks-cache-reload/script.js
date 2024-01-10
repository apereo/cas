const cas = require("../../cas.js");
const assert = require("assert");
const fs = require("fs");
const os = require("os");

(async () => {
    const kid = (Math.random() + 1).toString(36).substring(4);
    const tempDir = os.tmpdir();
    await cas.log(`Generated kid ${kid}`);
    const configFilePath = `${tempDir}/keystore.jwks`;
    const config = JSON.parse(fs.readFileSync(configFilePath));
    await cas.doGet("https://localhost:8443/cas/oidc/jwks",
        (res) => {
            assert(res.status === 200);
            assert(res.data.keys[0]["kid"] !== kid);
        },
        (error) => {
            throw error;
        });

    config.keys[0]["kid"] = kid;
    await cas.log(`Updated configuration:\n${JSON.stringify(config, undefined, 2)}`);
    await fs.writeFileSync(configFilePath, JSON.stringify(config, undefined, 2));
    await cas.sleep(1000);
    await cas.doGet("https://localhost:8443/cas/oidc/jwks",
        (res) => {
            assert(res.status === 200);
            assert(res.data.keys[0]["kid"] === kid);
        },
        (error) => {
            throw error;
        });

})();
