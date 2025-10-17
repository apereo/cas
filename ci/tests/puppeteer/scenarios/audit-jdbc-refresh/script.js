const cas = require("../../cas.js");
const assert = require("assert");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLogin(page);
    await cas.loginWith(page, "unknown", "Mellon");

    await cas.doGet("https://localhost:8443/cas/actuator/auditLog",
        (res) => assert(res.data.length > 0),
        (error) => {
            throw (error);
        }, {
            "Content-Type": "application/json"
        });

    const name = (Math.random() + 1).toString(36).substring(4);
    await cas.log("Updating configuration and waiting for changes to reload...");
    const dbFile = `/tmp/db/${name}`;
    await cas.updateYamlConfigurationSource(__dirname, {
        cas: {
            audit: {
                jdbc: {
                    url: `jdbc:hsqldb:file:${dbFile}`
                }
            }
        }
    });
    await cas.log(`Updated database configuration to use ${dbFile}`);
    await cas.sleep(5000);

    await cas.refreshContext();

    await cas.gotoLogin(page);
    await cas.loginWith(page, "unknown", "Mellon");

    try {
        await cas.doGet("https://localhost:8443/cas/actuator/auditLog",
            (res) => assert(res.data.length === 2),
            (error) => {
                throw (error);
            }, {
                "Content-Type": "application/json"
            });
    } finally {
        await cas.updateYamlConfigurationSource(__dirname, {
            cas: {
                audit: {
                    jdbc: {
                        url: "jdbc:hsqldb:mem:cas-hsql-database"
                    }
                }
            }
        });
    }
    await cas.closeBrowser(browser);
})();

