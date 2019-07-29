# OSLC Open Project specifications

[![CircleCI](https://circleci.com/gh/oslc-op/oslc-specs.svg?style=svg)](https://circleci.com/gh/oslc-op/oslc-specs)
[![Discourse status](https://img.shields.io/discourse/https/meta.discourse.org/status.svg)](https://forum.open-services.net/c/oslc-op)

This repository is for managing the development of OSLC Open Project
specifications including Core 3.0, OSLC Query, Tracked Resource Sets,
Configuration Management, and all the various OSLC domain specifications.

| Title | Description |Editor|
|-------|-------------|------|
| [Core 3.0 WD](https://raw.githack.com/oslc-op/oslc-specs/master/specs/core/oslc-core.html) | A collection of specifications that defines the basic patterns, protocols and capabilities of OSLC clients and servers. | Jim Amsden |
| [Query 3.0 WD](https://raw.githack.com/oslc-op/oslc-specs/master/specs/query/oslc-query.html) | Defines a simple, implementation independent selection and projection query capability. ||
| [Tracked Resource Set 3.0 WD](https://raw.githack.com/oslc-op/oslc-specs/master/specs/trs/tracked-resource-set.html) | Allows servers to expose a set of resources whose state can be tracked by clients. | Nick Crossley|
| [Requirements Management 2.1 WD](https://raw.githack.com/oslc-op/oslc-specs/master/specs/rm/requirements-management-spec.html) | Defines the OSLC services and vocabulary for the Requirements Management domain. | Jad El-khoury|
| [Configuration Management 1.0 WD](https://raw.githack.com/oslc-op/oslc-specs/master/specs/config/oslc-config-mgt.html) | OSLC Configuration Management defines an RDF vocabulary and a set of REST APIs for managing versions and configurations of linked data resources from multiple domains. |Nick Crossley|
| [Quality Management 2.1 WD](https://raw.githack.com/oslc-op/oslc-specs/master/specs/qm/quality-management-spec.html) | Defines the OSLC services and vocabulary for the Quality Management domain. ||
| [Asset Management 2.1 WD](https://raw.githack.com/oslc-op/oslc-specs/master/specs/asset/asset-management-spec.html) | Defines the OSLC services and vocabulary for the Asset Management domain. |NA|
| [Automation 2.1 WD](https://raw.githack.com/oslc-op/oslc-specs/master/specs/auto/automation-spec.html) | Defines the OSLC services and vocabulary for the domain that supports automation of sequences of actions on OSLC resources. |NA|
| [Performance Monitoring 2.1 WD](https://raw.githack.com/oslc-op/oslc-specs/master/specs/perfmon/performance-monitoring-spec.html) | Defines the OSLC services and vocabulary for the Performance Monitoring domain. |NA|

## Getting started

In order to preview the specifications in your local workspace, you should run a local webserver
(it is inadvisable to disable the security protections against running AJAX requests on local filesystem).
With Python 3 installed:

    cd docs/
    python3 -m http.server 8000 --bind 127.0.0.1

Now you can browse all specs via http://localhost:8000/. For an even better experience, install Browsersync (`npm i -g browser-sync`) and run it the following way:

    cd docs/
    browser-sync start --server --directory

Now you can browse all specs via http://localhost:8000/. Anytime you save a spec file, every browser tab where this file is opened will be reloaded automatically.

## Contributions

Read [how to contribute to the OSLC Open Project](https://github.com/oslc-op/oslc-admin/blob/master/CONTRIBUTING.md).

## Licensing

Please see the
[LICENSE](https://github.com/oslc-op/oslc-admin/blob/master/LICENSE.md)
file for description of the license terms and OASIS policies applicable
to the OSLC Open Project work items.

