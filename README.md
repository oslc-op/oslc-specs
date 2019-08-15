# OSLC Open Project specifications

[![CircleCI](https://circleci.com/gh/oslc-op/oslc-specs.svg?style=svg)](https://circleci.com/gh/oslc-op/oslc-specs)
[![Discourse status](https://img.shields.io/discourse/https/meta.discourse.org/status.svg)](https://forum.open-services.net/c/oslc-op)

This repository is for managing the development of OSLC Open Project
specifications including Core 3.0, OSLC Query, Tracked Resource Sets,
Configuration Management, and all the various OSLC domain specifications.

| Title | Description |Editor|
|-------|-------------|------|
| [Core 3.0 WD](https://oslc-op.github.io/oslc-specs/specs/core/oslc-core.html) | A collection of specifications that defines the basic patterns, protocols and capabilities of OSLC clients and servers. | Jim Amsden |
| [Change Management 3.0 WD](https://oslc-op.github.io/oslc-specs/specs/cm/change-mgt-spec.html) | TBD | Jim Amsden |
| [Query 3.0 WD](https://oslc-op.github.io/oslc-specs/specs/query/oslc-query.html) | Defines a simple, implementation independent selection and projection query capability. | TBD |
| [Tracked Resource Set 3.0 WD](https://oslc-op.github.io/oslc-specs/specs/trs/tracked-resource-set.html) | Allows servers to expose a set of resources whose state can be tracked by clients. | Nick Crossley |
| [Requirements Management 2.1 WD](https://oslc-op.github.io/oslc-specs/specs/rm/requirements-management-spec.html) | TBD | Jad El-khoury |
| [Configuration Management 1.0 WD](https://oslc-op.github.io/oslc-specs/specs/config/oslc-config-mgt.html) | Domain spec for managing versions and configurations of linked data resources from multiple domains. |Nick Crossley|
| [Quality Management 2.1 WD](https://oslc-op.github.io/oslc-specs/specs/qm/quality-management-spec.html) | TBD | TBD |
| [Asset Management 2.1 WD](https://oslc-op.github.io/oslc-specs/specs/asset/asset-management-spec.html) | TBD | NA |
| [Automation 2.1 WD](https://oslc-op.github.io/oslc-specs/specs/auto/automation-spec.html) | Domain spec for automation of sequences of actions on OSLC resources. | NA |
| [Performance Monitoring 2.1 WD](https://oslc-op.github.io/oslc-specs/specs/perfmon/performance-monitoring-spec.html) | TBD | NA |

## Getting started

In order to preview the specifications in your local workspace, you should run a local webserver
(it is inadvisable to disable the security protections against running AJAX requests on local filesystem).
With Python 3 installed:

    cd docs/
    python3 -m http.server 8000 --bind 127.0.0.1

Now you can browse all specs via http://localhost:8000/. For an even better experience, install Browsersync (`npm i -g browser-sync`) and run it the following way:

    browser-sync start --config bs-config.js

Now you can browse all specs via http://localhost:3000/. **Anytime you save a spec file, every browser tab where this file is opened will be reloaded automatically.**

## Contributions

Read [how to contribute to the OSLC Open Project](https://github.com/oslc-op/oslc-admin/blob/master/CONTRIBUTING.md).

## Licensing

Please see the
[LICENSE](https://github.com/oslc-op/oslc-admin/blob/master/LICENSE.md)
file for description of the license terms and OASIS policies applicable
to the OSLC Open Project work items.
