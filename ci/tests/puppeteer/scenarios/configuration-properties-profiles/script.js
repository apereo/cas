const cas = require('../../cas.js');
const assert = require("assert");

(async () => {
     await sendRequest("cas.custom.properties.all", "everything")
     // await sendRequest("cas.custom.properties.direct", "directValue")
     await sendRequest("cas.custom.properties.groovy1", "custom1")
     await sendRequest("cas.custom.properties.groovy2", "custom2")
     await sendRequest("cas.custom.properties.environment", "custom1")
     await sendRequest("cas.custom.properties.profile", "custom2")
     await sendRequest("cas.custom.properties.source", "yaml")
})();

async function sendRequest(key, value) {
    await cas.logg(`Asking for property ${key}`)
    await cas.doGet(`https://localhost:8443/cas/actuator/env/${key}`,
        res => {
            assert(res.status === 200)
            let casSource = res.data.propertySources[2];
            assert(casSource !== null);
            assert(casSource.name === "bootstrapProperties-casCompositePropertySource")
            cas.logg(`Comparing ${casSource.property.value} with ${value}`);
            assert(casSource.property.value === value)
        },
        error => {
            throw error;
        })
}
