---
layout: default
title: CAS - Service Discovery
category: High Availability
---
{% include variables.html %}


# Service Discovery

Service Discovery is one of the key tenets of a microservice based architecture. This guide aims to describe built-in 
CAS supported options that can be used for locating nodes for the purpose of load balancing and failover.

<div class="row pt-4">
  <div class="col-sm-4">
    <div class="card">
      <div class="card-body">
        <h4 class="card-title">:scroll: Consul Server Discovery Service</h4>
        <p class="card-text pb-2">
            HashiCorp Consul has multiple components, but as a whole, it is a tool for discovering and configuring 
            services in your infrastructure. It provides key features such as service discovery, health checking and more.
        </p>
        <a href="Service-Discovery-Guide-Consul.html" class="btn btn-primary">More...</a>
      </div>
    </div>
    <div class="card">
      <div class="card-body">
        <h4 class="card-title">:scroll: Eureka Server Discovery Service</h4>
        <p class="card-text pb-2">
            Eureka is a REST-based service, offered by Spring Cloud Netflix, that is primarily used for locating services for the 
            purpose of load balancing and failover of middle-tier servers.
        </p>
        <a href="Service-Discovery-Guide-Consul.html" class="btn btn-primary">More...</a>
      </div>
    </div>
  </div>
</div>
