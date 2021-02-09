# OSLC Open Project specifications

[![CircleCI](https://circleci.com/gh/oslc-op/oslc-specs.svg?style=svg)](https://circleci.com/gh/oslc-op/oslc-specs)
[![Discourse status](https://img.shields.io/discourse/https/meta.discourse.org/status.svg)](https://forum.open-services.net/c/oslc-op)

This repository is for managing the development of OSLC Open Project
specifications including Core 3.0, OSLC Query, Tracked Resource Sets,
Configuration Management, and all the various OSLC domain specifications.

## Quick links

| Title | Description |Responsible|
|-------|-------------|------|
| **[Query 3.0 PS01](https://oslc-op.github.io/oslc-specs/specs/query/oslc-query.html)** | Defines a simple, implementation independent selection and projection query capability. | David |
| **[Quality Management v2.1 PS01](https://oslc-op.github.io/oslc-specs/specs/qm/quality-management-spec.html)** | This specification defines the OSLC Quality Management domain, a RESTful web services interface for the management of product, service or software quality artefacts, activities, tasks and relationships between those and related resources such as requirements, defects, change requests or architectural resources. To support these scenarios, this specification defines a set of HTTP-based RESTful interfaces in terms of HTTP methods: GET, POST, PUT and DELETE, HTTP response codes, content type handling and resource formats. | Andrew |
| **[Change Management v3.0 PS01](https://oslc-op.github.io/oslc-specs/specs/cm/change-mgt-spec.html)** | This specification defines the OSLC Change Management domain, a RESTful web services interface for the management of product change requests, activities, tasks and relationships between those and related resources such as requirements, test cases, or architectural resources. To support these scenarios, this specification defines a set of HTTP-based RESTful interfaces in terms of HTTP methods: GET, POST, PUT and DELETE, HTTP response codes, content type handling and resource formats. | Jim |
| **[Requirements Management v2.1 PS01](https://oslc-op.github.io/oslc-specs/specs/rm/requirements-management-spec.html)** | This specification defines the OSLC Requirements Management domain. The specification supports key RESTful web service interfaces for the management of Requirements, Requirements Collections and supporting resources defined in the OSLC Core specification. To support these scenarios, this specification defines a set of HTTP-based RESTful interfaces in terms of HTTP methods: GET, POST, PUT and DELETE, HTTP response codes, content type handling and resource formats. | Jad |
| **[Core 3.0 PS01](https://oslc-op.github.io/oslc-specs/specs/core/oslc-core.html)** | A collection of specifications that defines the basic patterns, protocols and capabilities of OSLC clients and servers.| Jim |
| [Configuration Management 1.0 WD](https://oslc-op.github.io/oslc-specs/specs/config/oslc-config-mgt.html) | Domain spec for managing versions and configurations of linked data resources from multiple domains. **PSD WIP** |Nick|
| [Tracked Resource Set 3.0 WD](https://oslc-op.github.io/oslc-specs/specs/trs/tracked-resource-set.html) | Allows servers to expose a set of resources whose state can be tracked by clients. **WD WIP** | Nick |
| [AM 2.1 WD](https://oslc-op.github.io/oslc-specs/specs/am/architecture-management-spec.html) | **PSD WIP** | Jim |
| [Automation 2.1 WD](https://oslc-op.github.io/oslc-specs/specs/auto/automation-spec.html) | Domain spec for automation of sequences of actions on OSLC resources. | Jim |
| [Actions 2.0 WD](https://oslc-op.github.io/oslc-specs/specs/actions/actions-spec.html) | TBD. | Ian |

## Project Notes

| Title | Description |Responsible|
|-------|-------------|------|
| [OSLC Configuration Management 3.0 Primer](https://github.com/oslc-op/oslc-specs/wiki/Configuration-Management-3.0-Primer) | TBD | David |
| [Summary of changes in OSLC Core 3.0](https://hackmd.io/ojZYshcATLyZ7ziowDnsCw) | TBD | Andrew |
| [OSLC Core 3.0 Link Guidance](https://oslc-op.github.io/oslc-specs/notes/link-guidance/link-guidance.html) | TBD | Jim |
| [TRS 3.0 Primer](https://github.com/oslc-op/oslc-specs/wiki/TRS-3.0-Primer) | TBD | David |

## Old specs

Specs that are not actively developed (help needed!):

| Title | Description |Responsible|
|-------|-------------|------|
| [Asset Management 2.1 WD](https://oslc-op.github.io/oslc-specs/specs/asset/asset-management-spec.html) | TBD | NA |
| [Performance Monitoring 2.1 WD](https://oslc-op.github.io/oslc-specs/specs/perfmon/performance-monitoring-spec.html) | TBD | NA |
| [Estimation and Measurement Service 1.0 WD](https://oslc-op.github.io/oslc-specs/specs/ems/estimation-measurement-spec.html) | TBD | NA |
| [Reconcilliation 2.0](https://oslc-op.github.io/oslc-specs/specs/recon/reconciliation-spec.html) | TBD | NA |

## Getting started

In order to preview the specifications in your local workspace, you should run a
local webserver (it is inadvisable to disable the security protections against
running AJAX requests on local filesystem). With Python 3 installed:

    cd specs/
    python3 -m http.server 8000 --bind 127.0.0.1

Now you can browse all specs via <http://localhost:8000/>. For an even better
experience, install Browsersync (`npm i -g browser-sync`) and run it the
following way:

    browser-sync start --config bs-config.js

Now you can browse all specs via <http://localhost:8000/>. **Anytime you save a
spec file, every browser tab where this file is opened will be reloaded
automatically.**

## Contributions

Read [how to contribute to the OSLC Open
Project](https://github.com/oslc-op/oslc-admin/blob/master/CONTRIBUTING.md).

**See [the call details](https://github.com/oslc-op/oslc-admin/blob/master/CONTRIBUTING.md#online-meetings) for instructions how to join our weekly calls.**

For this repository particularly, please ensure that your editor respects the
[Editorconfig](https://editorconfig.org/#download) settings either by installing
a plugin or copying the settings manually. Make sure your lines are wrapped!

> Pro tip: Atom editor does all of this automatically when you install an
> Editorconfig for it. Highly recommended. It works even better in a setup with
> Browsersync described in the section above.

<!-- -->

> **Warning!** Eclipse, VS Code do not support the wrapping configuration
> setting from Editorconfig. You should manually configure your editor and ensure
> the lines are wrapped.

## Licensing

Please see the
[LICENSE](https://github.com/oslc-op/oslc-admin/blob/master/LICENSE.md)
file for description of the license terms and OASIS policies applicable
to the OSLC Open Project work items.
