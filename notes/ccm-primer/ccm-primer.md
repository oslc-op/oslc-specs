---
title: Configuration and Change Management 3.0 Primer
abstract: 
  Configuration and Change Management 3.0 Primer
  is an OSLC OP Project Note
---

# Outline

1. an explanation of the concepts in the specs (streams, baselines, config context, etc), and how they relate to each other. The specs is very exact about a listing of each of these terms. But unless you are an expert, it is hard to get an overview, and understand the overall model behind it all.
   * A graphical model representing all the concepts/shapes in the specs, as well as their relationships, and main properties.

2. an explanation of Global vs. Local Configuration. What role does each play in a toolchain? Which kinds of tool should implement which part?

3. For someone trying to implement CCM for any given tool, a hands-on incremental introduction of the concepts
   * How to introduce simple linear version control
   * How to introduce branching
   * How to introduce baselines
   * etc.

# Concepts

Wikipedia (https://en.wikipedia.org/wiki/Configuration_management) defines Configuration Management as "Configuration management (CM) is a systems engineering process for establishing and maintaining consistency of a product's performance, functional, and physical attributes with its requirements, design, and operational information throughout its life.".

In Configuration Management, artifacts are versioned. For example, a new requirement R1 might be defined and created. Once the content of that requirement is checked in or committed, version 1 of that requirement R1 exists. At that point, the contents of version 1 cannot be changed. If changes are required, the content is changed, and when checked in or delivered, results in version 2 of requirement R1. Version 2 of R1 was created from, or derived from, version 1 of R1. The sequence of versions of that requirement comprise its _version history_. In OSLC Configuration Management, the term _concept resource_ is used to mean all the versions of some artifact. In our example, requirement R1 is a concept resource (no version is specified), and requirement R1 version 1 is a specific version of that concept resource. Many versioning tools exist, and there are a number of different approaches as to how artifacts are versioned, when those versions are created, and when a version becomes non-modifiable. However, all tools support some notion of when a change is committed, the version that records that change becomes non-modifiable. The OSLC Configuration Management specification does not define how versioned should be implemented. Servers are free to choose existing versioning tools, or implement a versioning system of their own design.

An important element of Configuration Management is a _configuration_. A configuration defines what set of versioned artifacts are used in that configuration. For example, configuration C1 might use version 1 of requirement R1, and configuration C2 might use version 2 of requirement R1. New artifacts might be added to or obsolete artifacts removed from a configuration. The configuration therefore provides a view of the appropriate artifacts and versions of those artifacts that apply in that configuration. The OSLC Configuration Management specification defines this in terms of _selections_. A configuration might reference a _selection_ that in turn references the specific versioned artifacts that belong in that configuration.

In OSLC Configuration Management, a _stream_ is a modifiable configuration in which artifacts may be added or removed, or a different version of an artifact may be selected by a user to replace some other version of that artifact. Streams are the configurations in which ongoing work is performed. An essential element of Configuration Management is the ability to create a non-modifiable record of the set of versioned artifacts at specific milestones or points in time in order to provide traceability and auditing. In OSLC Configuration Management, a _baseline_ is a non-modifiable configuration whose set of versioned artifacts are also non-modifiable. Usually a baseline is created from a stream, recording the state of that stream. The stream continues to be modifiable, but the baseline is now a non-modifiable record of the state of the stream at the time the baseline was created. Every configuration is associated with a _component_. A _component_ is a unit of organization consisting of a set of version resources. When a _baseline_ is created from a _stream_, both the baseline and the stream are for the same _component_.

OSLC Configuration Management supports the idea that a configuration may be a container of other configurations. The term _contribution_ describes a configuration that is used by a parent configuration. Configurations may thus form a _hierarchy_ of configurations. The term _global configuration_ is used to describe a configuration that aggregates configurations, especially those from other configuration management servers. For example, a global configuration might have contributions from a requirements management server, a quality management server, and a source code control server. The global configuration thus represents the state of version artifacts across those application servers.

# Step1 - Simple linear version control - without Configuration Context

Versioning support is the most basic functionanlity.
We will start exploring support for versioning. 
The value here would be limited: a server could express different versions of each concept resource, and have these versions related into predecessor/successor history graphs. But the server would have no defined way to resolve concept resources to versions. This will be dealt with in the tutorial part that follows.

## Example
We will use this example throughout the exercise.
This should be a class/object diagram.
```
Requirement_a
id: a
version: v23
name: "Requirement A"
description: "A description of requirement A"
refines: Requirement_b

Requirement_b
id: b
version: v34
name: "Requirement B"
description: "A description of requirement B"
refines: Requirement_c
```

In most (if not all) versioning systems, each individual artefact is attributd with some version id. A version id can be:
* local: where each artefact has its own versioning sequence, such as v1, v2, v3, etc. 
* global: where artects share a single versioning sequence. 2 artefcats that have the same version number implies that the versions were created as part of the same commit.

## REST methods on resource versions  

### GET on version resource
let's assume for now that the client (somehow) got hold of the URL for a specific version resource URL. When a client performs a GET request, the received response is 

```
:conceptResourceA-v23
    a oslc_config:VersionResource ;
    dcterms:isVersionOf :conceptResourceA;
    prov:wasRevisionOf : conceptResourceA-v22.
:conceptResourceA
    a oslc_rm:requirement ;
    oslc_config:versionId "v23" ;
    dcterms:title "Requirement A" ;
    dcterms:description "A description of requirement A - as it appears in the state with the URI :conceptResourceA-v23" ;
    oslc_rm:refines <conceptResourceB-v24> .
```

This OSLC representation of the Requirement_a artefact would consist of two RDF resources: a Version Resource, and a Concept Resource:

Content to develop:
* Give here a short explanation on the difference between Concept Resource and Version Resource.
* explain the properties prov:wasRevisionOf, oslc_config:versionId.
* explain how resources should link to each other. Should it be Version or Concept resource? this can be examplified using oslc_rm:refines in artefacts above.
* The client can then navigate to the previous versions by following the prov:wasRevisionOf property.
* The client can also navigate to the concept resource by making a GET request to the dcterms:isVersionOf property.

### GET on concept resource

What should the client obtain when the do a GET on the concept resource URI?
Is this decided in the Specs? Or, is it up to the server to decide to - say - return the latest version of the resource?
Is the response also 2 RDF resources: a Version and a Concept? or just a concept with all the latest information?

### PUT/POST on version resource
How to create a new version? 
Or should we say that this is not yet possible, and we wait until later in the tutorial. Since we need to introduce other concepts first.

### ??? on Version resources?
But how did I even get hold of a version resource in example above?
A query/DUI that returned a list of concept resources. From that, one could navigate down the version tree.

### Conclusion
The server so far can work with version resource URIs.
But such a server would have no defined way to resolve concept resources to versions. To start with, we let the server invent its own ways to do this.

In the next section, we will go beyond this to provide version resolution using Configuration Context.  


# Versioning support - with Configuration Context

In the previous section, the server defined its own way to resolve concept resources to versions. We will here introduce the concept of "Conf Context" in order to ...
To start with, the server supports only local configurations (not global), only a single stream, with baselines of that single stream, so there is no branching or merging, and no support for change sets.


# Branching support


## Baselining support

# References
https://oslc-op.github.io/oslc-specs/specs/config/oslc-config-mgt.html
