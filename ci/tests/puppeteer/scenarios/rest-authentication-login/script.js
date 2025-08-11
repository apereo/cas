
const cas = require("../../cas.js");
const express = require("express");

const auth = require("basic-auth");

(async () => {
    const authenticate = (req, res, next) => {
        const credentials = auth(req);
        if (!credentials || credentials.name !== "restapi" || credentials.pass !== "YdCP05HvuhOH^*Z") {
            res.set("WWW-Authenticate", "Basic realm=\"Authentication Required\"");
            res.status(401).send("Authentication Required");
        } else {
            cas.log("Authentication successful");
            next();
        }
    };

    const app = express();
    app.post(/^(.+)$/, authenticate, (req, res) => {
        cas.log("Received request");
        const data = {
            "@class": "org.apereo.cas.authentication.principal.SimplePrincipal",
            "id": "casuser",
            "attributes": {
                "@class": "java.util.LinkedHashMap",
                "names": [
                    "java.util.List", ["cas", "user"]
                ]
            }
        };
        res.json(data);
    });
    app.use(authenticate);
    
    const server = app.listen(5432, async () => {
        let failed = false;
        try {
            await cas.log("Listening...");

            const browser = await cas.newBrowser(cas.browserOptions());
            const page = await cas.newPage(browser);
            await cas.gotoLogin(page);
            await cas.loginWith(page, "restapi", "YdCP05HvuhOH^*Z");
            await cas.assertCookie(page);

            server.close(() => {
                cas.log("Exiting server...");
                cas.closeBrowser(browser);
            });
        } catch (e) {
            failed = true;
            throw e;
        } finally {
            if (!failed) {
                await process.exit(0);
            }
        }
    });

})();
