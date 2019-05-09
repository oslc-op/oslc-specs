# OSLC Open Project specifications

[![CircleCI](https://circleci.com/gh/oasis-tcs/oslc-core.svg?style=svg)](https://circleci.com/gh/oasis-tcs/oslc-core)
[![Discourse status](https://img.shields.io/discourse/https/meta.discourse.org/status.svg)](https://forum.open-services.net/)
[![Gitter](https://img.shields.io/gitter/room/nwjs/nw.js.svg)](https://gitter.im/OSLC/chat)


Members of the [OASIS OSLC Lifecycle Integration Core (OSLC Core)
TC](https://www.oasis-open.org/committees/oslc-core/) create and manage
technical content in this TC GitHub repository
(<https://github.com/oasis-tcs/oslc-core>) as part of the TC\'s
chartered work (*i.e.*, the program of work and deliverables described
in its
[charter](https://www.oasis-open.org/committees/oslc-core/charter.php)).

OASIS TC GitHub repositories, as described in [GitHub Repositories for
OASIS TC Members\' Chartered
Work](https://www.oasis-open.org/resources/tcadmin/github-repositories-for-oasis-tc-members-chartered-work),
are governed by the OASIS [TC
Process](https://www.oasis-open.org/policies-guidelines/tc-process),
[IPR Policy](https://www.oasis-open.org/policies-guidelines/ipr), and
other policies, similar to TC Wikis, TC JIRA issues tracking instances,
TC SVN/Subversion repositories, etc. While they make use of public
GitHub repositories, these TC GitHub repositories are distinct from
[OASIS Open
Repositories](https://www.oasis-open.org/resources/open-repositories),
which are used for development of open source
[licensed](https://www.oasis-open.org/resources/open-repositories/licenses)
content.

## Description

This repository is for managing the development of OSLC Core TC
specifications including Core 3.0, OSLC Query, Tracked Resource Sets,
and Configuration Management. The repository was initialized with work
product from the TC\'s Subversion, JIRA, and MainMain Wiki. The goal is
to provide a single infrastructure for all OSLC specification
development activities, to provide a simpler, more efficient set of
specification lifecycle management capabilities for the TC members, and
to improve the external user experience.

## Getting started

In order to preview the specifications, you need to run a local webserver (you shall never disable the security protections against running AJAX requests on local filesystem). With Python 3 installed:

    cd docs/
    python3 -m http.server 8000 --bind 127.0.0.1

Now you can browse all specs via http://localhost:8000/. For an even better experience, install Browsersync (`npm i -g browser-sync`) and run it the following way:

    cd docs/
    browser-sync start --server --directory

Now you can browse all specs via http://localhost:8000/. Anytime you save a spec file, every browser tab where this file is opened will be reloaded automatically. 

## Contributions

As stated in this repository\'s [CONTRIBUTING
file](https://github.com/oasis-tcs/oslc-core/blob/master/CONTRIBUTING.md),
contributors to this repository are expected to be Members of the OASIS
OSLC Lifecycle Integration Core (OSLC Core) TC, for any substantive
change requests. Anyone wishing to contribute to this GitHub project and
[participate](https://www.oasis-open.org/join/participation-instructions)
in the TC\'s technical activity is invited to join as an OASIS TC
Member. Public feedback is also accepted, subject to the terms of the
[OASIS Feedback
License](https://www.oasis-open.org/policies-guidelines/ipr#appendixa).

## Licensing

Please see the
[LICENSE](https://github.com/oasis-tcs/oslc-core/blob/master/LICENSE.md)
file for description of the license terms and OASIS policies applicable
to the TC\'s work in this GitHub project. Content in this repository is
intended to be part of the OSLC Core TC\'s permanent record of activity,
visible and freely available for all to use, subject to applicable OASIS
policies, as presented in the repository
[LICENSE](https://github.com/oasis-tcs/oslc-core/blob/master/LICENSE.md)
file.


## Contact

Please send questions or comments about [OASIS TC GitHub
repositories](https://www.oasis-open.org/resources/tcadmin/github-repositories-for-oasis-tc-members-chartered-work)
to the [OASIS TC Administrator](mailto:tc-admin@oasis-open.org). For
questions about content in this repository, please contact the TC Chair
or Co-Chairs as listed on the the OSLC Core TC\'s [home
page](https://www.oasis-open.org/committees/oslc-core/).
