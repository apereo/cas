const cas = require("../../cas.js");
const assert = require("assert");

(async () => {
    await sendRequest("cas.custom.properties.all", "everything");
    await sendRequest("cas.custom.properties.groovy1", "custom1");
    await sendRequest("cas.custom.properties.groovy2", "custom2");
    await sendRequest("cas.custom.properties.environment", "custom1");
    await sendRequest("cas.custom.properties.profile", "custom2");
    await sendRequest("cas.custom.properties.source", "yaml");

    await cas.doGet("https://localhost:8443/cas/actuator/loggers/org.apereo.cas",
        (res) => {
            assert(res.status === 200);
            assert(res.data.configuredLevel === "DEBUG");
            assert(res.data.effectiveLevel === "DEBUG");
        }, (error) => {
            throw error;
        });
})();

async function sendRequest(key, value) {
    await cas.logg(`Asking for property ${key}`);
    await cas.doGet(`https://localhost:8443/cas/actuator/env/${key}`,
        (res) => {
            assert(res.status === 200);

            let found = false;
            for (let i = 0; !found && i < res.data.propertySources.length; i++) {
                const casSource = res.data.propertySources[i];
                cas.log(`Property source: ${casSource.name}`);
                if (casSource.name === "bootstrapProperties-casCompositePropertySource") {
                    found = true;
                    cas.logg(`Comparing property source value ${casSource.property.value} with expected ${value}`);
                    assert(casSource.property.value === value);
                }
            }
            if (!found) {
                throw "Unable to locate CAS property source";
            }
        },
        (error) => {
            throw error;
        });
}
