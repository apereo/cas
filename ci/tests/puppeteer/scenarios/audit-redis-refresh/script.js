const cas = require("../../cas.js");
const assert = require("assert");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLogin(page);
    await cas.loginWith(page, "unknown", "Mellon");

    await cas.doGet("https://localhost:8443/cas/actuator/auditLog",
        (res) => assert(res.data.length === 0),
        (error) => {
            throw (error);
        }, {
            "Content-Type": "application/json"
        });

    await cas.updateYamlConfigurationSource(__dirname, {
        cas: {
            audit: {
                redis: {
                    enabled: true
                }
            }
        }
    });
    await cas.sleep(5000);

    await cas.refreshContext();

    await cas.gotoLogin(page);
    await cas.loginWith(page, "unknown", "Mellon");

    try {
        await cas.doGet("https://localhost:8443/cas/actuator/auditLog",
            (res) => assert(res.data.length >= 1),
            (error) => {
                throw (error);
            }, {
                "Content-Type": "application/json"
            });
    } finally {
        await cas.updateYamlConfigurationSource(__dirname, {
            cas: {
                audit: {
                    redis: {
                        enabled: false
                    }
                }
            }
        });
    }
    await cas.closeBrowser(browser);
})();

