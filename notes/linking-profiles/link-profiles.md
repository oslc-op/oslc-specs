---
title: OSLC Linking Profiles Version 1.0
abstract: OSLC Linking profiles are non-normative supplement to OSLC specifications that specify additional constraints on OSLC conformance clauses and vocabulary terms for a common purpose. The goal is to facilitate interoperability between OSLC enabled applications when linking across applications, domains, and heterogenious domain providers.  
---

# Introduction

Linking profiles are non-normative supplement to OSLC specifications, aiming to ensure interoperability between OSLC enabled applications when linking across applications, domains, and heterogenious domain providers.  It is important to note that OSLC interoperability is not automatically implied by OSLC specifications, as the specifications allow for high degree of conformance variability.

Linking use cases result in establishing OSLC links across OSLC resources in different applications, possibly within the same OSLC domain or across domains. For example, a requirements provider from one vendor establishes traceability links to a requirements provider from another vendor. A cross domain example would be a PLM BOM application establishing traceability links to a requirements application by a different vendor. The cross vendors emphasis is because this is the context for interoperability challenges.  Linking use cases may vary in capability as described later in this document, but all linking use cases require establishing connections between the resources in the consumer and provider applications, ability to select a resource with the provider to link with a resource of the consumer, and storage of the link in one of the applications. From that point onwards there is a variability across the use cases related to bi-directional navigability (are both applications aware of the link) and also behaviors that depend if the application supports configuration management or not. 

The introduction of the profile comes to help users qualify whether two OSLC applications meet compatibility level to perform a set of use OSLC linking cases, and software vendors on how to build/consume OSLC linking related services that ensure interoperability. 

The linking profiles specify 4 (3+1 future) levels of conformance, each aims at certain implementation level and set of linking use cases, and can also be related to “OSLC maturity” usage. 
The profiles should be based on accumulated practice. Since IBM ELM has the longest and broadest OSLC integration adoptions  of OSLC integrations, IBM ELM is used as a baseline for spec variability choices, so the profiles will ensure ELM linking interoperability. However, the profiles are meant provide an interoperability baseline to any other vendor and future interoperability related or unrelated to IBM OSLC applications. 

# Motivation

## The Problem
The fundamental set of interoperability use cases in the context of OSLC linked data architecture describe cross linking across lifecycle resources and domain providers. Cross linking forms the foundation for “digital continuity” and establishment of “digital threads”. Using linking profiles guarantees a set of use cases across providers implemented by different software vendors.

When originally developed, the OSLC specifications (Core, GCM) were purposefully kept general avoiding over-prescription.  The intent was to allow a considerable degree of implementation variability to speed vendor adoption as well as let adopting vendors identify areas (though their adoption) that needed more prescription.by. This is reflected in the minimal set of mandatory (“must”) services , and rest that are only recommended (“should”). 

The goal of interoperability across different vendor OSLC specification implementations requires peer to peer testing and adjustments to satisfy certain common interoperability use cases. Also, implementors complain that practically implementing interoperable OSLC providers or consumers is very time consuming and requires lots of discovery, trial and error.  Additionally, when vendors label  their applications as “OSLC compliant”, the possible implementation variability makes that label definition very broad becoming difficult for end users to know for certain what aspects of interoperability exist. 

In addition to the variability caused by the mandatory/optional requirements, implementors also struggle to follow the specification without detailed explanations or examples. Unfortunately, while some guides exist, they are not always known about or found, requiring web searches.  In some cases, guides simply do not exist. 

## Solution scope

Specify profiles based on maturity and use cases to reduce interoperability variability across providers complying to a specific profile, and provide guidance with examples to eliminate ambiguities and confusion related to the implementation of the specifications. A key aspect of reducing spec variabilities is converting “should” clauses in the spec to “must” in certain profiles. Guidance may be reference existing articles and/or include a supplementary spec in the profile. 

As agreed in preliminary discussions, the linking profiles will accommodate various degrees of maturity and need of OSLC implementations, that are based on two key characteristics related to providers/consumers: 

* The need to maintain bi-directional navigability or not
* Usage of configuration management by the provider or not

Based on that we aim to specify 3+1 profiles as follows

* **Basic**: focuses on establishing links to a provider, without bi-directional navigation. The main objective of this profile is to establish proper connection with a provider by a consumer and obtaining a resource selection UI. The challenges related to the basic profile are around establishing OSLC connection with a provider with proper authentication and discovery of services.
* **Bi-directional**: this profile extends the capabilities of the basic profile, but in addition it should result in by-directional navigation across the two applications. It also requires conformance with the basic profile but requires additional capabilities. This profile needs to clarify where a physical link is actually stored, and how the non-storing application can discover incoming links. The issues related to this profile are around link storage, discovery, and OSLC query.
* **Config management Linking**: Linking across configuration management enabled OSLC providers. This profile extends the bi-directional profile with additional guidance in case of the application enables OSLC configuration management. 
* **Full**: This profile is future and is not currently feasible, as it depends on future standardization of a link discovery service. It aims to prescribe providers on how to use an incoming link service if one exists, to optimize incoming links discovery, on top of the config management linking profile. 

### Solution Capability Matrix

The following table summarizes the OSLC capabilities for ach of the profiles. 

| Capability | Basic | Bi-Directional | Config | Full |
| ---------- | ----- | -------------- | ------ | ---- |
| Root Services document | MUST | MUST | MUST | MUST |
| CSP for friends        | MUST | MUST | MUST | MUST |
| Selection Dialogs      | MUST | MUST | MUST | MUST |
| CORS for friends       | SHOULD | SHOULD | SHOULD | MUST |
| Preview Dialogs        | SHOULD | SHOULD | SHOULD | SHOULD |
| PUT on Resources       | MAY | SHOULD | SHOULD | SHOULD |
| OSLC Query             | MAY | SHOULD | SHOULD | SHOULD |
| Link Ownership         | MAY | SHOULD | MUST | MUST | 
| Config Management       | MAY | MAY | MUST | MUST  |
| Contribute links to TRS  | MAY | MAY | MUST | MUST  |
| OSLC Link Discovery Service  | MAY | MAY | MAY | MUST  |

# Root Services document

Each section will list:

1. The applicable profiles
1. The applicable OSLC conformance clauses
2. How those conformance clauses are tightened in the profile
3. Any additional ResourceShape constrints
4. The intended purpose of the additional constrats
5. Any additional conformance clauses
6. Examples

# CSP for friends

# Selection Dialogs

# CORS for friends

# Preview Dialogs

# PUT on Resources

# OSLC Query

# Link Ownership

# Configuration Management

# Contribute links to TRS

# OSLC Link Discovery service

