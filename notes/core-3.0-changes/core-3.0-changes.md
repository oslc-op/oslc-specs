---
title: OSLC Core v3.0 Changes Version 1.0 
abstract:
  This note will serve as a guide for existing OSLC 2.0 users to learn what's new and navigate around the changes in the specifications.
---

## Introduction

We are happy to announce a publication of OSLC 3.0, the next generation of the core OSLC specification, as the OASIS Standard. The specification in full can be viewed freely [online](https://docs.oasis-open-projects.org/oslc-op/core/v3.0/os/oslc-core.html) and this note will serve as a guide for existing OSLC 2.0 users to learn what's new and navigate around the changes in the specifications.

The OSLC Core 2.0 evolution from 1.0 strengthened its definition of the use of RESTful HTTP web services and adopted an existing standard-based approach to representing a data model that was flexible and had multiple representation formats, namely RDF. A primary goal of OSLC Core 3.0 was upwards compatibility with OSLC 2.0, preserving the investment in existing implementations. Most OSLC Core 3.0 changes are optional and OSLC 2.0 servers will not be mandated to adopt them to be compliant with 3.0. In most cases, existing OSLC 2.0 clients should be able to consume OSLC 3.0 servers with no changes. This should result in a less disruptive change from 2.0 to 3.0. This document provides a summary of the changes in OSLC Core 3.0 with respect to OSLC Core 2.0.

From the standardization point of view, there have been two major changes in OSLC 3.0 that further the openness of OSLC and strengthen its integration value. First, the specification development was moved to OASIS, a vendor-neutral standardization body with a proven track record of assisting in the publication of standards that are widely adopted and further standardized by international bodies like ISO (e.g. [MQTT](https://docs.oasis-open.org/mqtt/mqtt/v5.0/mqtt-v5.0.html), [ODF (ISO/IEC 26300-1:2015)](https://www.oasis-open.org/committees/tc_home.php?wg_abbrev=office)). Throughout the development of the specifications, our member section at OASIS featured as many as [16 members](https://web.archive.org/web/20170814105435/http://www.oasis-oslc.org:80/members) before executing a transition to an Open Project that has drastically lowered the bar to [entry](https://github.com/oslc-op/oslc-admin/blob/master/CONTRIBUTING.md). Second, the OSLC 3.0 specification is now aligned with the [Linked Data Platform 1.0](https://www.w3.org/TR/ldp/), a W3C Recommendation, that was adopted by projects such as [Solid](https://solidproject.org/), focused on decentralizing data storage and spearheaded by Sir Tim Berners-Lee, the founding father of the Web.

## Resource representations

OSLC Core 2.0 mandates support for RDF/XML (`application/rdf+xml`) and made support for other RDF formats optional. OSLC Core 3.0 requires that servers support at least one RDF format, and recommends servers support RDF/XML, Turtle (`text/turtle`), and JSON-LD (`application/ld+json`), and use standard HTTP content negotiation in choosing which RDF format to return. Turtle is the preferred format used for examples in OSLC 3.0 specifications. Clients that specify both RDF/XML and Turtle formats in an `Accept` request header are likely to operate with both OSLC 2.0 and 3.0 servers without change. New clients that _only_ accept Turtle might not work with older OSLC 2.0 servers that might only support RDF/XML.

## OSLC Discovery

The discovery mechanisms defined in OSLC Core 2.0 for discovering OSLC service provider catalogs, service providers, services, creation factories, selection dialogs, and query capabilities continue to be supported in OSLC Core 3.0. If new OSLC 3.0 server implementations continue to support these discovery mechanisms, they should be consumable by existing OSLC 2.0 clients.

OSLC Core 3.0 extends OSLC 2.0 Discovery to allow servers to optionally support the use of OPTIONS and `Link` headers as an alternative mechanism to discover creation factories, selection dialogs, and resource preview. OSLC Core 3.0 further specifies well-known URIs registered with accordance to the [RFC 8615](https://datatracker.ietf.org/doc/html/rfc8615) for probing the existence of OSLC Service Provider Catalogs as well as Jazz RootServices documents at well-known URIs.

## Creation factories

Creation factories in OSLC Core 3.0 are upwards compatible with Core 2.0 and existing OSLC 2.0 clients should be able to consume creation factories in an OSLC 3.0 server.

## Delegated dialogs

OSLC Core 3.0 continues to support the use of delegated dialogs for creation and/or selection, and existing OSLC 2.0 clients should be able to consume these without change where these are declared in OSLC services.

## Query capabilities

In OSLC Core 2.0, the use of query capabilities was defined by a section on discovery and a separate document on query syntax. In OSLC Core 3.0, there is a separate OSLC Query 3.0 specification that covers usage and query syntax, and describes the changes from, and compatibility with, query in OSLC Core 2.0. OSLC 2.0 clients should be able to consume query capabilities in an OSLC 3.0 server without change.

## Prefixes

OSLC Core 2.0 did not define a minimum set of prefixes that OSLC servers should predefine and declare in services. OSLC Core 3.0 defines a set of recommended predefined prefixes. Existing servers are not required to implement this to be compliant with OSLC Core 3.0, but it is recommended they do so for convenience to clients.

## Resource Paging

Resource paging is unchanged in OSLC Core 3.0 with respect to Core 2.0.

## Resource preview

In OSLC Core 2.0, clients can request a compact representation of a resource using `Accept=application/x-oslc-compact+xml`. This continues to be supported in OSLC Core 3.0. OSLC 3.0 servers that support this functionality will work with OSLC 2.0 clients.

OSLC Core 3.0 also supports a new way of discovering and consuming compact data for resource preview. An OSLC 3.0 server can include a `Link` header where the link relation is `http://open-services.net/ns/core#Compact` and the target URI is the URI of a compact resource. This also supports getting a compact representation in JSON. OSLC 2.0 clients won't be aware of the new `Link` header and hence will not be impacted by this change.

## Shapes and vocabularies

Resource shapes in OSLC Core 3.0 are upwards compatible with those defined in OSLC Core 2.0. Core 3.0 supports some additional optional constraints that include:
1. On a resource shape, `oslc:hidden` may be used to indicate the type should be hidden. Some OSLC 2.0 servers may have implemented this prior to its inclusion in OSLC Core 3.0.
2. On a property, `oslc:queryable` may be used to indicate whether a property is queryable and can be specified in an `oslc.where` query expression.

OSLC Core 3.0 provides separately fetchable files for resource shapes defined in the specification. These may be fetched as RDF (in RDF/XML or Turtle formats) or as rendered HTML using standard HTTP content negotiation. The OSLC Core 3.0 vocabulary and constraints multi-part specification documents are generated from these RDF ResourceShape constraint representations.

## Authentication

OSLC Core 2.0 supports the use of HTTP Basic Authentication, OAuth (1.0), or both. OSLC Core 3.0 recommends that servers support OAuth 2.0 and OpenIdConnect, and only support HTTP Basic Authentication via SSL. Existing clients that support none of these may be unable to use an OSLC 3.0 server.

## CORS protocol

OSLC Core 2.0 did not address the issue of new security constraints being imposed by some web browsers to limit the loading of resources with cross-origin. See [Cross-origin resource sharing](https://[https://en.wikipedia.org/wiki/Cross-origin_resource_sharing). An example is where a dialog served by one application embeds content from another application, such as in an iFrame. OSLC Core 3.0 recommends that servers support the CORS protocol to address these emerging security requirements.
