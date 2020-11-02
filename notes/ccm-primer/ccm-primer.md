---
title: Configuration and Change Management 3.0 Primer
abstract: 
  Configuration and Change Management 3.0 Primer
  is an OSLC OP Project Note
---

# Outline






1. an explanation of the concepts in the specs (streams, baselines, config context, etc), and how they relate to each other. The specs is very exact about a listing of each of these terms. But unless you are an expert, it is hard to get an overview, and understand the overall model behind it all.

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

# Versioned artifacts



# Hello


Test

# 2nd

Try
