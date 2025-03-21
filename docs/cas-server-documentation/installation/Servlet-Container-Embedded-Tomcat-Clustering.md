---
layout: default
title: CAS - Servlet Container
category: Installation
---
{% include variables.html %}

# Apache Tomcat - Embedded Servlet Container Clustering

Enable in-memory session replication to replicate web application session deltas.

| Clustering Type | Description                                                                                                                                                                                                                                                                     |
|-----------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `DEFAULT`       | Discovers cluster members via multicast discovery and optionally via staticly defined cluster members using the `clusterMembers`. [SimpleTcpCluster with McastService](http://tomcat.apache.org/tomcat-11.0-doc/cluster-howto.html)                                             |
| `CLOUD`         | For use in Kubernetes where members are discovered via accessing the Kubernetes API or doing a DNS lookup of the members of a Kubernetes service. [Documentation](https://cwiki.apache.org/confluence/display/TOMCAT/ClusteringCloud) is currently light, see code for details. |

| Membership Providers       | Description                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  |
|----------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `kubernetes`               | Uses [Kubernetes API](https://github.com/apache/tomcat/blob/master/java/org/apache/catalina/tribes/membership/cloud/KubernetesMembershipProvider.java) to find other pods in a deployment. API is discovered and accessed via information in environment variables set in the container. The KUBERNETES_NAMESPACE environment variable is used to query the pods in the namespace and it will treat other pods in that namespace as potential cluster members but they can be filtered using the KUBERNETES_LABELS environment variable which are used as a [label selector](https://kubernetes.io/docs/concepts/overview/working-with-objects/labels/#api). |
| `dns`                      | Uses [DNS lookups](https://github.com/apache/tomcat/blob/master/java/org/apache/catalina/tribes/membership/cloud/DNSMembershipProvider.java) to find addresses of the cluster members behind a DNS name specified by DNS_MEMBERSHIP_SERVICE_NAME environment variable. Works in Kubernetes but doesn't rely on Kubernetes.                                                                                                                                                                                                                                                                                                                                   |
| `MembershipProvider` class | Use a [membership provider implementation](https://github.com/apache/tomcat/blob/master/java/org/apache/catalina/tribes/MembershipProvider.java) of your choice.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                             |

Most settings apply to the `DEFAULT` clustering type, which requires members to be defined via `clusterMembers` if multicast discovery doesn't work. The `cloudMembershipProvider` setting applies to the `CLOUD` type.

{% include_cached casproperties.html properties="cas.server.tomcat.clustering." %}


