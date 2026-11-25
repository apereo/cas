const assert = require("assert");
const cas = require("../../cas.js");

const PUBSUB = "http://localhost:8085/v1/projects/apereo-cas-gcp";
const TOPIC = "projects/apereo-cas-gcp/topics/CasTicketRegistryTopic";

/*
 * Each node subscribes to the ticket registry topic under a subscription of its own, named after its queue identifier
 * (CasGoogleCloudPubSubTicketRegistryAutoConfiguration.subscriptionNameFor). The dead-letter topic is named after the
 * ticket registry topic, and a node that subscribed to it would never see the changes of the other node.
 */
async function verifySubscriptions() {
    const response = await fetch(`${PUBSUB}/subscriptions?pageSize=1000`);
    assert.equal(response.status, 200);
    const subscriptions = (await response.json()).subscriptions ?? [];
    await cas.log(subscriptions);
    for (const node of ["cas-node-8443", "cas-node-8444"]) {
        const name = `projects/apereo-cas-gcp/subscriptions/CasTicketRegistryTopicSubscription-${node}`;
        const subscription = subscriptions.find((entry) => entry.name === name);
        assert(subscription !== undefined, `Subscription ${name} must exist`);
        assert.equal(subscription.topic, TOPIC);
    }
}

async function verifyHealth(port) {
    const json = JSON.parse(await cas.doRequest(`https://localhost:${port}/cas/actuator/health`, "GET", {"Accept": "application/json"}, 0));
    await cas.log(json.components);
    const component = Object.entries(json.components ?? {}).find(([name]) => name.toLowerCase() === "pubsub");
    assert(component !== undefined, `Node ${port} must report Pub/Sub health`);
    assert.equal(component[1].status, "UP");
}

(async () => {
    await verifySubscriptions();
    await cas.verifyTicketRegistryAcrossNodes();
    await verifyHealth(8443);
    await verifyHealth(8444);
})();
