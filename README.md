# OSLC Open Project specifications

[![CircleCI](https://circleci.com/gh/oslc-op/oslc-specs.svg?style=svg)](https://circleci.com/gh/oslc-op/oslc-specs)
[![Discourse status](https://img.shields.io/discourse/https/meta.discourse.org/status.svg)](https://forum.open-services.net/c/oslc-op)

This repository is for managing the development of OSLC Open Project
specifications including Core 3.0, OSLC Query, Tracked Resource Sets,
Configuration Management, and all the various OSLC domain specifications.

| Title | Description |Responsible|
|-------|-------------|------|
| **[Change Management 3.0 PSD 02](https://oslc-op.github.io/oslc-specs/specs/cm/change-mgt-spec.html)** | This specification defines the OSLC Change Management domain, a RESTful web services interface for the management of product change requests, activities, tasks and relationships between those and related resources such as requirements, test cases, or architectural resources. To support these scenarios, this specification defines a set of HTTP-based RESTful interfaces in terms of HTTP methods: GET, POST, PUT and DELETE, HTTP response codes, content type handling and resource formats. | Andrew |
| **[Requirements Management 2.1 PSD 02](https://oslc-op.github.io/oslc-specs/specs/rm/requirements-management-spec.html)** | This specification defines the OSLC Requirements Management domain. The specification supports key RESTful web service interfaces for the management of Requirements, Requirements Collections and supporting resources defined in the OSLC Core specification. To support these scenarios, this specification defines a set of HTTP-based RESTful interfaces in terms of HTTP methods: GET, POST, PUT and DELETE, HTTP response codes, content type handling and resource formats. | Jad |
| [Quality Management 2.1 WD](https://oslc-op.github.io/oslc-specs/specs/qm/quality-management-spec.html) | **PSD WIP** | Andrew |
| [Core 3.0 WD](https://oslc-op.github.io/oslc-specs/specs/core/oslc-core.html) | A collection of specifications that defines the basic patterns, protocols and capabilities of OSLC clients and servers. **PSD WIP** | Jim |
| [Configuration Management 1.0 WD](https://oslc-op.github.io/oslc-specs/specs/config/oslc-config-mgt.html) | Domain spec for managing versions and configurations of linked data resources from multiple domains. **PSD WIP** |Andrew|
| [Tracked Resource Set 3.0 WD](https://oslc-op.github.io/oslc-specs/specs/trs/tracked-resource-set.html) | Allows servers to expose a set of resources whose state can be tracked by clients. **WD WIP** | Nick |
| [Query 3.0 WD](https://oslc-op.github.io/oslc-specs/specs/query/oslc-query.html) | Defines a simple, implementation independent selection and projection query capability. | David |
| [AM 2.1 WD](https://oslc-op.github.io/oslc-specs/specs/am/architecture-management-spec.html) | TBD | NA |


Specs that are not actively developed (help needed!):

| Title | Description |Responsible|
|-------|-------------|------|
| [Asset Management 2.1 WD](https://oslc-op.github.io/oslc-specs/specs/asset/asset-management-spec.html) | TBD | NA |
| [Automation 2.1 WD](https://oslc-op.github.io/oslc-specs/specs/auto/automation-spec.html) | Domain spec for automation of sequences of actions on OSLC resources. | NA |
| [Performance Monitoring 2.1 WD](https://oslc-op.github.io/oslc-specs/specs/perfmon/performance-monitoring-spec.html) | TBD | NA |
| [Estimation and Measurement Service 1.0 WD](https://oslc-op.github.io/oslc-specs/specs/ems/estimation-measurement-spec.html) | TBD | NA |
| [Reconcilliation 2.0](https://oslc-op.github.io/oslc-specs/specs/recon/reconciliation-spec.html) | TBD | NA |

## Getting started

In order to preview the specifications in your local workspace, you should run a
local webserver (it is inadvisable to disable the security protections against
running AJAX requests on local filesystem). With Python 3 installed:

    cd docs/
    python3 -m http.server 8000 --bind 127.0.0.1

Now you can browse all specs via http://localhost:8000/. For an even better
experience, install Browsersync (`npm i -g browser-sync`) and run it the
following way:

    browser-sync start --config bs-config.js

Now you can browse all specs via http://localhost:8000/. **Anytime you save a spec
file, every browser tab where this file is opened will be reloaded
automatically.**

## Contributions

Read [how to contribute to the OSLC Open
Project](https://github.com/oslc-op/oslc-admin/blob/master/CONTRIBUTING.md).

For this repository particularly, please ensure that your editor respects the
[Editorconfig](https://editorconfig.org/#download) settings either by installing
a plugin or copying the settings manually. Make sure your lines are wrapped!

> Pro tip: Atom editor does all of this automatically when you install an
Editorconfig for it. Highly recommended. It works even better in a setup with
Browsersync described in the section above.

> **Warning!** Eclipse, VS Code do not support the wrapping configuration
setting from Editorconfig. You should manually configure your editor and ensure
the lines are wrapped.

## Licensing

Please see the
[LICENSE](https://github.com/oslc-op/oslc-admin/blob/master/LICENSE.md)
file for description of the license terms and OASIS policies applicable
to the OSLC Open Project work items.
