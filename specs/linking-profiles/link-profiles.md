---
title: OSLC Linking Profiles Version 1.0
abstract: OSLC Linking profiles are non-normative supplement to OSLC specifications that specify additional constraints on OSLC conformance clauses and vocabulary terms for a common purpose. The goal is to facilitate interoperability between OSLC enabled applications when linking across applications, domains, and heterogenious domain providers.  
---

# Introduction {.informative}

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
| Link Ownership         | | MUST | MUST | MUST | 
| PUT on Resources       | | MUST | MUST | MUST |
| OSLC Query             | | SHOULD | SHOULD | SHOULD |
| Config Management       | | | MUST | MUST  |
| Contribute links to TRS  | | | MUST | MUST  |
| OSLC Link Discovery Service  | | | | MUST  |

# Root Services document
| Capability | Basic | Bi-Directional | Config | Full |
| ---------- | ----- | -------------- | ------ | ---- |
| Root Services document | MUST | MUST | MUST | MUST |

Each section will list:

1. The applicable profiles
1. The applicable OSLC conformance clauses
2. How those conformance clauses are tightened in the profile
3. Any additional ResourceShape constrints
4. The intended purpose of the additional constrats
5. Any additional conformance clauses
6. Examples

Example some normative text: <span class="conformance">OSLC Services **MUST** support at least one RDF resource serialization format, and should support as many serialization formats as possible through content negotiation.</span>


# Authentication 

# CSP for friends
| Capability | Basic | Bi-Directional | Config | Full |
| ---------- | ----- | -------------- | ------ | ---- |
| CSP for friends    | MUST | MUST | MUST | MUST |

# Selection Dialogs <In context to Resource Creation>
| Capability | Basic | Bi-Directional | Config | Full |
| ---------- | ----- | -------------- | ------ | ---- |
| Selection Dialog   | MUST | MUST | MUST | MUST |

A OSLC server MUST register Dialog discovery service using oslc:selectionDialog property with a service provider. A OSLC consumer discovers the service by looking for oslc:selectionDialog property and making a GET request on the service resource OR
Clients can also use the HTTP Prefer header to find the dialogs for a container.The server responds with the dialog descriptors in the response body OR
The client discovers the dialogs by making an HTTP OPTIONS request on the container and The server response contains a Link header with URLs to the dialog descriptors.

OSLC server MUST provide a dialog, an HTML page for resource selection, search and select remote resources. Selection dialog provides OSLC consumers to search and select resources, to create trace links. Selection Dialog MUST accept user input and to filter out resources on basis of user input and MUST display filtered resources. Selection Dialogs MUST provide a way to user to select filtered resources and submit their selection. Once user selection is submitted, Dialogs send selected resource URI as result back to the client using the HTML5 function Window.postMessage [whatwg-web-messaging]. postMessage is called on window.opener if set. Otherwise, postMessage is called on window.parent.
    
OSLC client MUST Open the dialog in a new window or embed the dialog in an iframe. Selection Dialog consumer MUST add a message listener to receive messages from the dialog. When message from the dialog indicates a completed action, OSLC Consumer SHOULD free resources and MUST handle the action.

# Link Ownership
In bi-derctional linking scenarios both OSLC participants are aware of links across their owned resources and enable link visibility and navigation at each side. Nevertheless storing a link at both sides is considered a ill-practice as it is essentially replication of data. This that may result in inconsistencies as links are updated or deleted, since maintaining consistency requires synchrnization across the providers on any update. Therefore the recommended practice is to store links on one of the participants, and use link discovery by the other participant. Therefore there need to be an agreed convention on which side should store the link. OSLC links have an incoming and outgoing sides, determined by the role of the link. Usully one side will have an active predicate name, for example "implements" and the other side will have a passive predicate name, in this example it would be "implemented by". The active side is also considered the outgoing side, and the passive the incoming side. The convention is that the link is stored with the resource on the outgoing side, i.e. the resource with the active predicate. The incoming side would discover the links with one of the discovery methods discussed in the following sections. Note the the creation of the link can be initiated by both providers. In case that the link is intiated by the incoming side provider, it needs to store it with the resource on the outgoing side provider. This is discussed in the put on resources section. In addition, for historical reasons, certain OSLC providers such as IBM ELM, may have some variations of behaviors between configuration enabled and non-configuration enabled modes, where in non-configuration modes there may still be a usage of backlink storage. In addition, providers do not support configurations such as CM (change management providers) would have preference for link storage over configuration enabled providers, to prevent baselined links to refer to mutable entities.

Along those principles, here is a list of all the specified OSLC predicates and their respective ownership.

Source domain/link owner |	Primary predicate	| Target domain | Secondary  predicate
-------------------------|----------------------|---------------|---------------------
RM	|   oslc_rm:constraints |   RM	|   oslc_rm:constrainedBy
-------------------------|----------------------|---------------|---------------------
RM	|   oslc_rm:decomposes |   	RM	|   oslc_rm:decomposedBy
-------------------------|----------------------|---------------|---------------------
RM	|   oslc_rm:elaborates	|   RM	|   oslc_rm:elaboratedBy
-------------------------|----------------------|---------------|---------------------
RM	|   oslc_rm:satisfies	|   RM	|   oslc_rm:satisfiedBy
-------------------------|----------------------|---------------|---------------------
RM	|   oslc_rm:uses	|   RM	|   -unspcified-
-------------------------|----------------------|---------------|---------------------
QM	|   oslc_qm:affects	|   RM	|   oslc_rm:affectedBy
-------------------------|----------------------|---------------|---------------------
QM	|   oslc_qm:validatesRequirement	|   RM	|   oslc_rm:validatedBy
-------------------------|----------------------|---------------|---------------------
QM	|   oslc_qm:validatesRequirementCollection	|   RM	oslc_rm:validatedBy
-------------------------|----------------------|---------------|---------------------
CM	|   oslc_cm:implements	|   RM	|   oslc_rm:imlementedBy
-------------------------|----------------------|---------------|---------------------
CM	|   oslc_cm:tracksRequirement	|   RM	|   oslc_rm:trackedBy
-------------------------|----------------------|---------------|---------------------
CM	|   oslc_cm:affectsRequirement	|   RM	
-------------------------|----------------------|---------------|---------------------
CM	|   oslc_cm:relatedTestScript   	|   QM	oslc_qm:relatedChangeRequest
-------------------------|----------------------|---------------|---------------------
CM	|   oslc_cm:relatedTestCase	|   QM	|   oslc_qm:relatedChangeRequest
-------------------------|----------------------|---------------|---------------------
CM	|   oslc_cm:relatedTestPlan	|   QM	|   oslc_qm:relatedChangeRequest
-------------------------|----------------------|---------------|---------------------
CM	|   oslc_cm:relatedTestExecutionRecord	|   QM	|   oslc_qm:relatedChangeRequest
-------------------------|----------------------|---------------|---------------------
CM	|   oslc_cm:affectedPlanItem	|   CM	|   -unspecified-
-------------------------|----------------------|---------------|---------------------
CM	|   oslc_cm:affectedByDefect	|   CM   |   -unspecified-
-------------------------|----------------------|---------------|---------------------
CM	|   oslc_cm:relatedChangeRequest	|   CM	-unspecified-
-------------------------|----------------------|---------------|---------------------
AM	|   jazz_am:derives	|   -unspecified-	|   -unspecified-
-------------------------|----------------------|---------------|---------------------
AM	|   jazz_am:elaborates	|   -unspecified-	|   -unspecified-
-------------------------|----------------------|---------------|---------------------
AM	|   jazz_am:external	|   External resource	|   -unspecified-
-------------------------|----------------------|---------------|---------------------
AM	|   jazz_am:refine	|   RM	|   -unspecified-
-------------------------|----------------------|---------------|---------------------
AM	|   jazz_am:satisfy	|   RM	|   -unspecified-
-------------------------|----------------------|---------------|---------------------
AM	|   jazz_am:trace	|   RM	|   -unspecified-


# PUT on Resources

# CORS for friends

# Preview Dialogs 
| Capability | Basic | Bi-Directional | Config | Full |
| ---------- | ----- | -------------- | ------ | ---- |
| Preview Dialogs   | SHOULD | SHOULD | SHOULD | SHOULD |

# OSLC Query
| Capability | Basic | Bi-Directional | Config | Full |
| ---------- | ----- | -------------- | ------ | ---- |
| OSLC Query             | | SHOULD | SHOULD | SHOULD |
```
example text
```

# Configuration Management

# Contribute links to TRS

# OSLC Link Discovery service {.informative}

