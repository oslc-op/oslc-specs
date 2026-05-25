# Proposed Additions to OSLC Configuration Management — Selections Extensions

*This document proposes two new specializations of `oslc_config:Selections`. Both are normative when adopted.*

The base specification defines version resolution against a configuration of `Selections`. Two domains in widespread industrial use — Product Lifecycle Management most prominently — require additional binding steps in which version membership depends on the evaluation of resource-carried predicates against a context supplied with the request. This document defines two such extensions:

- **`oslc_config:EffectivitySelections`** — selects version resources whose membership in a resolved structure depends on instance/timing parameters (date, serial number, lot, unit, model, end-item). Evaluated in an **Effectivity-Context**.
- **`oslc_config:VariabilitySelections`** — selects version resources whose membership depends on configuration option choices (feature selections, variant rules). Evaluated in a **Variability-Context**.

Both share an architectural pattern (two-step resolution: candidate selection + predicate evaluation), but they address different aspects of a product's lifecycle and are expected to use different predicate languages and different parameter schemas. They are specified separately and may be combined within a single configuration.

This specification deliberately does not define the predicate language or the parameter schema for either context. Those definitions are domain-specific and *MUST* be supplied by the importing domain specification. For the PLM domain, both are expected to be defined in the OSLC Product Lifecycle Management specification (`plm-spec.html`), which already defines the underlying versioned-resource model — including the `PartUsage` reification that enables structural-relationship membership to be expressed as a selection of a PartUsage version resource rather than as a separate "effectivity on relationships" mechanism.

## Relationship to PartUsage and BOM-line membership

A PLM Bill of Materials line is a "use" of one Part within another. In the OSLC PLM specification, this relationship is reified as `oslc_plm:PartUsage` — a first-class resource composed by a parent Part (`oslc_plm:composedOf`) and representing a child Part (`oslc_plm:represents`). Because PartUsage is itself a versioned resource, it participates in the standard OSLC Configuration Management version-resolution mechanism: a configuration selects PartUsage versions in the same way it selects Part versions.

This factoring removes a class of problem that would otherwise complicate effectivity-based resolution. There is no need for a separate "effectivity on usage links" mechanism in OSLC Configuration Management, because:

1. The configuration's selection of a Part version determines *which revision of the part* applies.
2. The configuration's selection of a PartUsage version determines *whether the usage participates in the BOM and which revision of the usage applies*.
3. Effectivity refinement (this specification) applies uniformly to both kinds of selected version resource. A Part revision and a PartUsage version may each carry their own effectivity predicate.

A resolved BOM view is therefore a configuration in which the effectivity (and where applicable, variability) predicates carried by the selected Part and PartUsage versions have been evaluated in the supplied context, yielding the concrete set of part-occurrence entries that constitute the BOM at that point.

---

## Section N. Effectivity-Based Resolution

*This section is normative.*

### N.1 Motivation and Scope

The version resolution mechanism defined in [Section 12](#vresolution) selects, for each concept resource URI, a single version resource based on the selections of the configuration context and its recursive contributions. This mechanism is sufficient for resources whose membership in a configured structure depends only on version identity.

Some domains — notably Product Lifecycle Management — require an additional binding step in which the membership of selected versions depends on the evaluation of predicates carried by those versions against a context of effectivity parameters (for example, date, serial number, lot, unit, model, or end-item). In such domains, two distinct selection questions arise:

1. *Which version of each versioned resource is structurally in scope?* This is answered by the version resolution defined in Section 12, including the selection of PartUsage versions for BOM lines as described above.
2. *Of the candidate versions chosen in (1), which are actually effective under the current effectivity context, and which, if any, must be re-bound to a different version on the basis of release effectivity?*

This section defines `oslc_config:EffectivitySelections`, a selection type whose `oslc_config:selects` triples name the version resources that have survived both phases — those selected as candidates by the Configuration-Context and evaluated as effective by the Effectivity-Context attached to the containing configuration. A baseline holds frozen `selects` values; a stream's `selects` values are server-maintained against the stream's currently-attached contexts. The predicate language used to express effectivity, and the structure of effectivity parameters, are not defined by this specification and *SHOULD* be defined by domain specifications that import this mechanism. [CONFIG-RES-N1] The proposed OSLC-OP Product Lifecycle Management specification is the intended location for the effectivity-predicate language and the effectivity-context resource shape; existing PLM vocabularies and standards (including concepts borrowed from STEP and EXPRESS) are candidate sources.

This mechanism is independent of, and *MAY* be combined with, `oslc_config:UnboundSelections` and `oslc_config:VariabilitySelections` (Section M). Where multiple selection types are present in a configuration, `oslc_config:UnboundSelections` is resolved first (by mechanisms outside the scope of this specification) to produce concrete version candidates; `oslc_config:VariabilitySelections.selects` then names those candidates that also survive variability evaluation; and `oslc_config:EffectivitySelections.selects` names those that additionally survive effectivity evaluation. The composition order is normative because effectivity expressions on a Part or PartUsage version MAY presuppose option choices already resolved by variability.

### N.2 Resource Shape for EffectivitySelections

- **Describes:** `http://open-services.net/ns/config#EffectivitySelections`
- **Summary:** A selections resource whose `oslc_config:selects` triples name the version resources that have survived both the Configuration-Context candidate selection (Section 12) and effectivity-predicate evaluation against the Effectivity-Context attached to the containing configuration.
- **Description:** An `oslc_config:EffectivitySelections` resource is a specialization of `oslc_config:Selections` whose `oslc_config:selects` triples reference version resources that have already been determined to be effective for the Effectivity-Context identified by the containing configuration's `oslc_config:effectivityContext` property (see N.4). The Effectivity-Context is not stored on the `EffectivitySelections` resource itself; it is a property of the configuration that contains the `EffectivitySelections`, ensuring that a baseline (which freezes both its `selects` triples and its `effectivityContext` reference) is self-describing.

**EffectivitySelections Properties**

| *Prefixed Name* | *Occurs* | *Read-only* | *Value-type* | *Representation* | *Range* | *Description* |
| --- | --- | --- | --- | --- | --- | --- |
| `oslc_config:selects` | Zero-or-many | see description | Resource | Reference | `oslc_config:VersionResource` | The target resource *MUST* be a version resource [CONFIG-RES-N2]. Targets *MUST NOT* be of type `oslc_config:Configuration` or `oslc_config:Component` [CONFIG-RES-N3]. Each targeted version is one that (a) has been selected as a candidate by the Configuration-Context of the containing configuration through the version resolution defined in Section 12, and (b) has been evaluated as effective by its effectivity predicates against the Effectivity-Context identified by the containing configuration's `oslc_config:effectivityContext` property. Targets *MAY* be of any versioned resource type that carries effectivity predicates, including (in the PLM domain) `oslc_plm:Part` and `oslc_plm:PartUsage` versions. In a baseline, this property is read-only and frozen at baseline-creation time. In a stream, this property is server-maintained: the server re-computes its values when either the stream's candidate pool or the stream's `oslc_config:effectivityContext` reference changes [CONFIG-RES-N4]. |
| `rdf:type` | One-or-many | unspecified | Resource | Reference | `rdfs:Class` | A resource type URI. An `EffectivitySelections` resource *MUST* have at least the resource types `oslc_config:Selections` and `oslc_config:EffectivitySelections` [CONFIG-RES-N5]. An `EffectivitySelections` resource *MUST NOT* also have the type `oslc_config:UnboundSelections` or `oslc_config:VariabilitySelections` [CONFIG-RES-N6]; these selection mechanisms are independent and apply at different stages of resolution. |

Servers *MAY* define additional properties on `EffectivitySelections` resources, including domain-specific properties that annotate the effectivity evaluation (for example, the version of an effectivity predicate language assumed by the selections, or a reference to an effectivity-parameter schema). [CONFIG-RES-N7]

### N.3 Effectivity Predicates on Candidate Version Resources

Candidate version resources that participate in effectivity filtering carry effectivity information that the server evaluates against the Effectivity-Context attached to the containing configuration (N.4). The form of that information is not defined by this specification; it *MUST* be defined by a domain specification. [CONFIG-RES-N8] In the PLM domain, this definition is expected to appear in `plm-spec.html`, covering at minimum:

- The effectivity-predicate vocabulary supported on `oslc_plm:Part` versions (revision effectivity: which revision applies under a given date/serial/unit context).
- The effectivity-predicate vocabulary supported on `oslc_plm:PartUsage` versions (occurrence effectivity: whether and which version of a PartUsage participates in a BOM under a given context).
- The form of effectivity context values (date, serial range, unit range, model, end-item namespace).

A candidate version resource *MAY* declare effectivity that:

1. **Conditions its membership** — the predicate yields a Boolean value. If true, the version is included in the containing configuration's `EffectivitySelections.selects`; if false, it is excluded (see N.5).
2. **Conditions the selection of a sibling version** — the predicate yields a version URI for a different version of the same versioned resource. The sibling, rather than the candidate, is included in `EffectivitySelections.selects`. This is the form taken by *release effectivity* on revisions of a versioned resource, where the candidate is replaced by the version released as of the effectivity date or serial/unit value.

Domain specifications *MUST* define which of these forms (or both) apply to each kind of versioned resource they describe. [CONFIG-RES-N9]

Multiple effectivity records *MAY* apply to a single version resource — for example, a Part revision may be effective on Product ABC for units 1–5 and on Product XYZ for units 9–11 simultaneously. Domain specifications *MUST* define how multiple effectivity records on a single version interact under evaluation (typically a logical OR across records keyed by end-item context). [CONFIG-RES-N9a]

### N.4 Effectivity Context

An effectivity context is a resource that carries the parameter assignments (for example, date, serial number, unit number, end-item, option set) against which effectivity predicates are evaluated. The structure of this resource is defined by a domain specification; for the PLM domain, see `plm-spec.html` for `oslc_plm:EffectivityContext`.

This specification introduces an `oslc_config:effectivityContext` property on `oslc_config:Configuration` that identifies the effectivity context applicable to the configuration's `EffectivitySelections` resources. A configuration whose selections include one or more `oslc_config:EffectivitySelections` *MUST* carry exactly one `oslc_config:effectivityContext` property naming the context against which those selections' post-filter `selects` values were computed. [CONFIG-RES-N10]

**effectivityContext on Configuration**

| *Prefixed Name* | *Occurs* | *Read-only* | *Value-type* | *Representation* | *Range* | *Description* |
| --- | --- | --- | --- | --- | --- | --- |
| `oslc_config:effectivityContext` | Zero-or-one | see description | Resource | Reference | domain-defined | The effectivity context whose parameters were supplied to effectivity predicate evaluation when computing the `EffectivitySelections.selects` triples of this configuration. *MUST* be present on any configuration whose selections include one or more `oslc_config:EffectivitySelections`. On a baseline, this property is read-only and frozen. On a stream, this property *MAY* be modified by an authorized client; modification causes the server to re-compute the affected `EffectivitySelections.selects` against the new context. |

The property attaches the effectivity context to the configuration rather than to individual `EffectivitySelections` resources so that a baseline is self-describing: reading a baseline yields both its frozen `selects` triples and the context reference that produced them, without any external lookup. Re-pointing or editing the referenced context resource has no effect on a baseline's `selects` (which are frozen) but causes re-computation in a stream.

**Request-time conveyance.** A request *MAY* carry an `Effectivity-Context` request header, or an equivalent `oslc_config.effectivity` query parameter, whose value is an angle-bracket-delimited URI reference (escaped as defined in [[OSLCCore3]]) to an effectivity context resource:

```
?oslc_config.effectivity=uri_ref_esc
```

The semantics differ by configuration kind:

- **Baseline.** A baseline's `oslc_config:effectivityContext` is immutable and applies to all requests. If a request supplies an `Effectivity-Context` header or `oslc_config.effectivity` query parameter, the value *MUST* match the baseline's `effectivityContext` or the request *MUST* fail with 400 Bad Request. [CONFIG-RES-N15]
- **Stream.** A stream's `oslc_config:effectivityContext` identifies the default context whose computed `selects` is currently stored on the stream. A request *MAY* supply an alternative context via header or query parameter, in which case the server *MUST* either (a) re-compute the affected `EffectivitySelections.selects` against the supplied context for the scope of the request (without persisting the change), or (b) fail with 400 Bad Request if it does not support per-request override.

Where the `oslc_config.effectivity` query parameter or `Effectivity-Context` header is used, a server *SHOULD* include in the response a `Vary` header that names whichever mechanism was used, so that responses are not incorrectly cached across different effectivity contexts. [CONFIG-RES-N13]

If a configuration is created with `EffectivitySelections` but no `oslc_config:effectivityContext`, the server *MUST* fail the create operation with a 400 Bad Request indicating that an effectivity context is required. [CONFIG-RES-N14]

### N.5 Computation of EffectivitySelections.selects

This section defines the algorithm by which a server populates the `oslc_config:selects` triples of an `oslc_config:EffectivitySelections` resource. The algorithm is invoked:

- When an `EffectivitySelections` resource is first created in a stream;
- When the stream's `oslc_config:effectivityContext` reference is changed or the referenced context resource is edited;
- When the stream's candidate pool changes (a `oslc_config:Selections` or `oslc_config:UnboundSelections` contributing to the same configuration is created, updated, or removed);
- At baseline-creation time, to materialize the frozen `selects` triples that the baseline will record;
- Optionally, transiently, when a request supplies an alternative effectivity context per N.4 (the computed `selects` is used for the response only and is not persisted).

A baseline's `selects` triples *MUST NOT* be re-computed after baseline creation; the algorithm runs once and the result is frozen on the baseline (N.4). [CONFIG-RES-N16]

**Inputs.** The server *MUST* take as inputs:

1. The candidate pool — the version resources in scope for the containing configuration's Configuration-Context, as determined by Section 12 version resolution across the configuration's `oslc_config:Selections` and `oslc_config:UnboundSelections` and recursive contributions. Where `oslc_config:VariabilitySelections` is also present (Section M), the candidate pool is restricted to those candidates that also appear in the surviving `VariabilitySelections.selects` (variability is applied before effectivity; see N.7 and M.7).
2. The effectivity context identified by the containing configuration's `oslc_config:effectivityContext` property (N.4).

**Procedure.** For each candidate version resource `v` in the input pool:

1. If `v` carries no `oslc_plm:effectivity` (or domain-equivalent) records, include `v` in `EffectivitySelections.selects` unchanged.
2. Otherwise, evaluate each effectivity record carried by `v` against the effectivity context. Records combine by logical OR: `v` is effective if any one of its records evaluates to a positive outcome. Each record yields one of:
   - **Boolean true** — `v` is effective; include `v` in `EffectivitySelections.selects`.
   - **Boolean false** — the record does not apply to this context; continue to the next record.
   - **Version URI** — release-effectivity substitution: include the named replacement version (not `v`) in `EffectivitySelections.selects`. The replacement *MUST* be a version of the same versioned resource as `v`; otherwise the record is malformed and the server *MUST* fail the computation with a 500 Internal Server Error referencing an `oslc:Error` describing the failure. [CONFIG-RES-N17]
3. If every record carried by `v` evaluates to Boolean false, `v` is not included in `EffectivitySelections.selects`. The concept URI of `v` is left unbound by this `EffectivitySelections`.

The resulting set of version URIs is the `oslc_config:selects` value of the `EffectivitySelections`. A baseline freezes this set; a stream stores it as the current materialization, refreshed on the triggers listed above.

**Resolved-resource access.** A subsequent request for a versioned resource by concept URI in the context of this configuration *MUST* return:

- The version named in `EffectivitySelections.selects` for that concept URI, if exactly one is present;
- 404 Resource Not Found, if no version is named (the concept URI is unbound by effectivity); [CONFIG-RES-N18]
- The outcome determined by the multi-match rules of Section 12 (`oslc_config:contributionOrder` and equivalents), if more than one version is named across contributing `EffectivitySelections`. Servers *SHOULD* document their resolution behavior in this case. [CONFIG-RES-N19]

### N.6 Independence of Concerns

This specification deliberately preserves the separation of concerns between configurations and versioned resources established in Sections 3 and 12, while accommodating the domain reality that some versioned resources carry information that participates in selection.

The configuration is the selector: it identifies *which* version resources are in scope (its candidate pool) and *which* effectivity context applies (via `oslc_config:effectivityContext`). The candidate version resources are the selected: they carry both their domain content and, where domain specifications permit, the effectivity predicates that the server evaluates when populating `EffectivitySelections.selects`. The configuration does not embed the predicates; the version resources do not embed the parameter context. The server composes them when computing `selects` (N.5).

This factoring permits a configuration to be authored, baselined, and queried using the standard mechanisms of this specification, while supporting domain-specific effectivity logic without that logic being standardized here.

### N.7 Interaction with Existing Resolution

This section interacts with the existing resolution mechanisms of the specification as follows:

- Where a configuration contains only `oslc_config:Selections` resources (and no `EffectivitySelections`, `VariabilitySelections`, or `UnboundSelections`), resolution proceeds exactly as defined in Section 12. Effectivity and variability contexts, if supplied, have no effect.
- Where a configuration contains `oslc_config:UnboundSelections`, those name concept URIs that the candidate-pool computation in N.5 binds to specific version resources before effectivity evaluation. The base specification names "effectivity parameters" and "variability parameters" as canonical mechanisms for binding `UnboundSelections`; this specification supplies the former.
- Where a configuration contains `oslc_config:VariabilitySelections`, those are computed first (per Section M and §M.7). The surviving `VariabilitySelections.selects` set defines the candidate pool that N.5 then refines by effectivity.
- Where a configuration contains `oslc_config:EffectivitySelections`, those `selects` triples are populated as defined in N.5, against the configuration's `oslc_config:effectivityContext`.
- Where a change set overrides a configuration containing `EffectivitySelections` resources, the change set's selections contribute to the candidate pool. A change set *MAY* itself contain `EffectivitySelections` resources, in which case those `selects` triples are populated against the change set's own `oslc_config:effectivityContext` if present, or the overridden configuration's `effectivityContext` otherwise.

### N.8 Caching, Tracked Resource Sets, and Compact Rendering

Implementations *SHOULD* note the following operational considerations:

- A baseline's stored `EffectivitySelections.selects` triples are effectively a server-side cache of the resolution; no additional response-cache keying is needed for baselines beyond the configuration URI itself. A stream's stored `EffectivitySelections.selects` is refreshed on context or candidate-pool change; responses that derive from it *SHOULD* carry a `Vary` header naming the request mechanism (`Effectivity-Context` header or `oslc_config.effectivity` query parameter) where one was used to override the stream's default context. [CONFIG-RES-N20]
- Tracked Resource Sets (Section 16) only track version resources. It is the responsibility of the TRS consumer to provide an appropriate Configuration-Context (which carries with it the configuration's attached `effectivityContext` and, where applicable, `variabilityContext`) to resolve tracked version resources to a selected version.
- Compact rendering (Section 15) of resources resolved through `EffectivitySelections` *MUST* reflect the effectivity context attached to the containing configuration (and, where applicable, the variability context) at the time of the initial request for the compact resource, and the response *MUST* be valid under that combination of contexts. [CONFIG-RES-N22]

### N.9 Conformance

A configuration server claiming conformance to this section *MUST*:

1. Support `oslc_config:EffectivitySelections` resources in configurations as defined in N.2.
2. Support the `oslc_config:effectivityContext` property on `oslc_config:Configuration` as defined in N.4, enforcing the read-only/frozen behavior for baselines and the mutable behavior for streams.
3. Implement the computation of `EffectivitySelections.selects` defined in N.5, including the triggers for re-computation on streams and the freeze on baselines.
4. Freeze a baseline's `EffectivitySelections.selects` and `effectivityContext` at baseline creation, and reject subsequent modifications.
5. Provide caching headers consistent with N.8.

A configuration server *MAY* claim conformance to the base specification (Sections 1–20) without conforming to this section. Servers that do not conform to this section *MUST* reject any configuration POSTed to them that contains `oslc_config:EffectivitySelections` resources or an `oslc_config:effectivityContext` property with a 400 Bad Request response.

A server that supports the optional request-time `Effectivity-Context` header or `oslc_config.effectivity` query parameter (N.4) *MUST* honor the matching and override semantics defined there.

---

## Section M. Variability-Based Resolution

*This section is normative.*

### M.1 Motivation and Scope

A second category of domains — again, Product Lifecycle Management most prominently — requires resolution against configuration option choices that are orthogonal to the time/serial dimension addressed by Section N. A 150% product structure carries every candidate part and usage that *could* appear in *any* configuration of the product; the variability filter resolves it down to the 100% structure that applies to *this* configuration of options (Engine = V6, Color = Red, Market = EU, etc.).

Variability is distinct from effectivity:

- Effectivity asks "given the current date / serial / unit / model, which version applies?"
- Variability asks "given the chosen option values, which version applies?"

In practice all major PLM systems (PTC Windchill, Siemens Teamcenter, Aras Innovator) treat these as separate filtering passes with separate predicate languages — option-set boolean expressions (Teamcenter's Modular Variant Language, Windchill's assigned-expression filter, Aras's Configurator rules) for variability; range comparisons over date/serial/unit for effectivity. This specification preserves that separation by defining `oslc_config:VariabilitySelections` as a sibling to `oslc_config:EffectivitySelections` rather than collapsing both into one mechanism.

This section defines `oslc_config:VariabilitySelections`, a selection type whose `oslc_config:selects` triples name the version resources that have survived both phases — those selected as candidates by the Configuration-Context and evaluated as variant-applicable by the Variability-Context attached to the containing configuration. The mechanism parallels `oslc_config:EffectivitySelections` (Section N) and uses the same post-filter framing: a baseline holds frozen `selects` values; a stream's `selects` is server-maintained against the stream's currently-attached contexts. The predicate language used to express variability conditions, and the structure of variability parameters (option sets, feature trees, variant rules), are not defined by this specification and *MUST* be defined by domain specifications that import this mechanism. [CONFIG-RES-M1] The proposed OSLC-OP Product Lifecycle Management specification is the intended location for the variability-predicate language and the variability-context resource shape.

### M.2 Resource Shape for VariabilitySelections

- **Describes:** `http://open-services.net/ns/config#VariabilitySelections`
- **Summary:** A selections resource whose `oslc_config:selects` triples name the version resources that have survived both the Configuration-Context candidate selection (Section 12) and variability-predicate evaluation against the Variability-Context attached to the containing configuration.
- **Description:** An `oslc_config:VariabilitySelections` resource is a specialization of `oslc_config:Selections` whose `oslc_config:selects` triples reference version resources that have already been determined to apply under the Variability-Context identified by the containing configuration's `oslc_config:variabilityContext` property (see M.4). The Variability-Context is not stored on the `VariabilitySelections` resource itself; it is a property of the configuration that contains the `VariabilitySelections`, ensuring that a baseline (which freezes both its `selects` triples and its `variabilityContext` reference) is self-describing.

**VariabilitySelections Properties**

| *Prefixed Name* | *Occurs* | *Read-only* | *Value-type* | *Representation* | *Range* | *Description* |
| --- | --- | --- | --- | --- | --- | --- |
| `oslc_config:selects` | Zero-or-many | see description | Resource | Reference | `oslc_config:VersionResource` | The target resource *MUST* be a version resource [CONFIG-RES-M2]. Targets *MUST NOT* be of type `oslc_config:Configuration` or `oslc_config:Component` [CONFIG-RES-M3]. Each targeted version is one that (a) has been selected as a candidate by the Configuration-Context of the containing configuration through the version resolution defined in Section 12, and (b) has been evaluated as variant-applicable by its variability predicate against the Variability-Context identified by the containing configuration's `oslc_config:variabilityContext` property. Targets *MAY* be of any versioned resource type that carries variability predicates, including (in the PLM domain) `oslc_plm:Part` and `oslc_plm:PartUsage` versions. In a baseline, this property is read-only and frozen at baseline-creation time. In a stream, this property is server-maintained: the server re-computes its values when either the stream's candidate pool or the stream's `oslc_config:variabilityContext` reference changes [CONFIG-RES-M4]. |
| `rdf:type` | One-or-many | unspecified | Resource | Reference | `rdfs:Class` | A resource type URI. A `VariabilitySelections` resource *MUST* have at least the resource types `oslc_config:Selections` and `oslc_config:VariabilitySelections` [CONFIG-RES-M5]. A `VariabilitySelections` resource *MUST NOT* also have the type `oslc_config:UnboundSelections` or `oslc_config:EffectivitySelections` [CONFIG-RES-M6]. |

Servers *MAY* define additional properties on `VariabilitySelections` resources, including domain-specific properties that annotate the variability evaluation (for example, the version of a variability predicate language assumed by the selections, or a reference to an option-set schema). [CONFIG-RES-M7]

### M.3 Variability Predicates on Candidate Version Resources

Candidate version resources that participate in variability filtering carry variability information that the server evaluates against the Variability-Context attached to the containing configuration (M.4). The form of that information is not defined by this specification; it *MUST* be defined by a domain specification. [CONFIG-RES-M8] In the PLM domain, this definition is expected to appear in `plm-spec.html`, covering at minimum:

- The variability-predicate vocabulary supported on `oslc_plm:Part` versions and `oslc_plm:PartUsage` versions (boolean expressions over named option values).
- The form of the variability context (a set of option-value assignments, optionally with a variant rule reference).
- Constraints on the option set itself (typed options, named values, cross-option exclusions).

A candidate version resource *MAY* declare a variability predicate that yields a Boolean value. If the predicate yields true (or the candidate carries no predicate), the candidate is included in `VariabilitySelections.selects`; if false, it is excluded.

Variability predicates are *not* expected to yield substitute version URIs; the question variability answers is "is this version included?" and not "which sibling version applies?". Substitution between revisions of the same versioned resource based on lifecycle progression is the domain of effectivity (Section N), not variability.

### M.4 Variability Context

A variability context is a resource that carries the option-value assignments against which variability predicates are evaluated. The structure of this resource is defined by a domain specification; for the PLM domain, see `plm-spec.html` for `oslc_plm:VariabilityContext`.

This specification introduces an `oslc_config:variabilityContext` property on `oslc_config:Configuration` that identifies the variability context applicable to the configuration's `VariabilitySelections` resources. A configuration whose selections include one or more `oslc_config:VariabilitySelections` *MUST* carry exactly one `oslc_config:variabilityContext` property naming the context against which those selections' post-filter `selects` values were computed. [CONFIG-RES-M10]

**variabilityContext on Configuration**

| *Prefixed Name* | *Occurs* | *Read-only* | *Value-type* | *Representation* | *Range* | *Description* |
| --- | --- | --- | --- | --- | --- | --- |
| `oslc_config:variabilityContext` | Zero-or-one | see description | Resource | Reference | domain-defined | The variability context whose option-value assignments were supplied to variability predicate evaluation when computing the `VariabilitySelections.selects` triples of this configuration. *MUST* be present on any configuration whose selections include one or more `oslc_config:VariabilitySelections`. On a baseline, this property is read-only and frozen. On a stream, this property *MAY* be modified by an authorized client; modification causes the server to re-compute the affected `VariabilitySelections.selects` against the new context. |

The property attaches the variability context to the configuration so that a baseline is self-describing: reading a baseline yields both its frozen `selects` triples and the context reference that produced them, with no external lookup.

**Request-time conveyance.** A request *MAY* carry a `Variability-Context` request header, or an equivalent `oslc_config.variability` query parameter, with the same matching/override semantics defined for Effectivity-Context in N.4: a baseline's request *MUST* match the baseline's stored `variabilityContext` or fail with 400 Bad Request [CONFIG-RES-M15]; a stream's request *MAY* supply an alternative context to drive a transient re-computation for that request only.

Where the `oslc_config.variability` query parameter or `Variability-Context` header is used, a server *SHOULD* include in the response a `Vary` header that names whichever mechanism was used. [CONFIG-RES-M13]

If a configuration is created with `VariabilitySelections` but no `oslc_config:variabilityContext`, the server *MUST* fail the create operation with a 400 Bad Request indicating that a variability context is required. [CONFIG-RES-M14]

### M.5 Computation of VariabilitySelections.selects

This section defines the algorithm by which a server populates the `oslc_config:selects` triples of an `oslc_config:VariabilitySelections` resource. The triggers and freeze-vs-refresh semantics parallel those of N.5 exactly; substitute "variability" for "effectivity" and "variabilityContext" for "effectivityContext" throughout. [CONFIG-RES-M16]

**Inputs.** The server *MUST* take as inputs:

1. The candidate pool — the version resources in scope for the containing configuration's Configuration-Context, as determined by Section 12 version resolution across the configuration's `oslc_config:Selections` and `oslc_config:UnboundSelections` and recursive contributions.
2. The variability context identified by the containing configuration's `oslc_config:variabilityContext` property (M.4).

**Procedure.** For each candidate version resource `v` in the input pool:

1. If `v` carries no `oslc_plm:variabilityCondition` (or domain-equivalent) record, include `v` in `VariabilitySelections.selects` unchanged.
2. Otherwise, evaluate the variability predicate carried by `v` against the variability context. The predicate yields one of:
   - **Boolean true** — `v` is variant-applicable; include `v` in `VariabilitySelections.selects`.
   - **Boolean false** — `v` is excluded from `VariabilitySelections.selects`.
   - **Any other value** — the predicate is malformed; the server *MUST* fail the computation with a 500 Internal Server Error referencing an `oslc:Error` describing the failure. [CONFIG-RES-M17]

The resulting set is the `oslc_config:selects` value of the `VariabilitySelections`. Baselines freeze it; streams refresh it on the same triggers as N.5.

When `oslc_config:EffectivitySelections` is also present in the configuration, the surviving `VariabilitySelections.selects` defines the candidate pool that N.5 then refines (see M.7).

### M.6 Independence of Concerns

As with N.6, this specification preserves the separation between configurations (selectors and context-bearers) and version resources (selected, predicate-bearers). The configuration identifies the candidate pool, the variability context (via `oslc_config:variabilityContext`), and which selections participate in variability-based filtering. The version resources carry their own variability predicates. The server composes them when populating `VariabilitySelections.selects`.

### M.7 Interaction with EffectivitySelections

`VariabilitySelections` and `EffectivitySelections` are independent and *MAY* coexist in a single configuration. When both are present:

1. The candidate pool used by `VariabilitySelections` computation is the configuration's Section-12 candidates.
2. `VariabilitySelections.selects` is computed first per M.5.
3. The surviving `VariabilitySelections.selects` set defines the candidate pool that `EffectivitySelections` computation per N.5 then refines.
4. `EffectivitySelections.selects` is the final filtered set for the configuration.

This order is normative: effectivity records on a Part or PartUsage version *MAY* presuppose option choices that variability has already resolved, but the reverse is not expected. [CONFIG-RES-M18]

A change set *MAY* contain `VariabilitySelections` resources alongside `EffectivitySelections` resources; both participate in their respective computation steps under the change set's own context properties (or the overridden configuration's, if not present on the change set).

### M.8 Caching, Tracked Resource Sets, and Compact Rendering

Same considerations as N.8: baselines store their frozen `selects` as the resolution cache; streams refresh on context or candidate-pool change. Where a request supplies a `Variability-Context` header or `oslc_config.variability` query parameter to drive a transient re-computation, the server *SHOULD* return a `Vary` header naming the mechanism used. Tracked Resource Sets and Compact rendering must reflect the configuration's attached `variabilityContext` (and `effectivityContext`) at request time.

### M.9 Conformance

A configuration server claiming conformance to this section *MUST*:

1. Support `oslc_config:VariabilitySelections` resources in configurations as defined in M.2.
2. Support the `oslc_config:variabilityContext` property on `oslc_config:Configuration` as defined in M.4.
3. Implement the computation of `VariabilitySelections.selects` defined in M.5.
4. When also conforming to Section N, perform variability computation before effectivity computation as defined in M.7.
5. Freeze a baseline's `VariabilitySelections.selects` and `variabilityContext` at baseline creation, and reject subsequent modifications.
6. Provide caching headers consistent with M.8.

A configuration server *MAY* claim conformance to the base specification (Sections 1–20) without conforming to this section. Servers that do not conform to this section *MUST* reject any configuration POSTed to them that contains `oslc_config:VariabilitySelections` resources or an `oslc_config:variabilityContext` property with a 400 Bad Request response.

A server that supports the optional request-time `Variability-Context` header or `oslc_config.variability` query parameter (M.4) *MUST* honor the matching and override semantics defined there.

A server *MAY* conform to Section N alone, to Section M alone, or to both.

---

## Worked Example — Eco Line Washing Machine (Effectivity)

This non-normative example illustrates how the mechanisms defined in Section N populate the `oslc_config:selects` triples of an `oslc_config:EffectivitySelections` resource from a candidate pool of `oslc_plm:Part` and `oslc_plm:PartUsage` versions. The product structure and the effectivity records on the PartUsage versions are defined in `plm-spec.html`, [§ Worked Example — Eco Line Washing Machine](../plm/plm-spec.html#washing-machine-example); only the OSLC Configuration Management side of the story is shown here.

### Inputs

- **Configuration.** An `oslc_config:Stream` named `em:Stream_EcoLine` whose component is `em:WashingMachine` and whose `oslc_config:selections` includes the candidate pool of `WashingMachine→Motor`, `WashingMachine→Housing`, and `WashingMachine→ControlUnit` PartUsage versions defined in the PLM spec example. (The Housing's internal PartUsages to `UpperHousing` and `LowerHousing` are unconditional and live in the Housing's own configuration, one level down.)
- **Effectivity-Context.** A resource `em:Ctx_2027_06_01` attached to the stream via `oslc_config:effectivityContext`. It carries `oslc_plm:effectivityDate "2027-06-01"^^xsd:date`.

### Server-computed EffectivitySelections.selects

Applying the procedure in N.5 against the candidate pool — for each PartUsage version, evaluate the `oslc_plm:effectivity` records carried by the version against the date `2027-06-01`:

| PartUsage version           | Effectivity record           | Outcome     |
|-----------------------------|------------------------------|-------------|
| `Usage_WM_Motor_pre27`      | until 2027-06-30             | true        |
| `Usage_WM_Motor_27on`       | from 2027-07-01              | false       |
| `Usage_WM_Housing_pre27`    | until 2026-12-31             | false       |
| `Usage_WM_Housing_27`       | 2027-01-01 to 2027-12-31     | true        |
| `Usage_WM_Housing_28h1`     | 2028-01-01 to 2028-06-30     | false       |
| `Usage_WM_Housing_28h2`     | from 2028-07-01              | false       |
| `Usage_WM_ControlUnit_v1`   | always                       | true        |

The server populates the stream's `EffectivitySelections.selects` with the three survivors:

```turtle
em:Stream_EcoLine a oslc_config:Stream ;
  oslc_config:component          em:WashingMachine ;
  oslc_config:effectivityContext em:Ctx_2027_06_01 ;
  oslc_config:selections         em:sel_effectivity_stream .

em:sel_effectivity_stream a oslc_config:EffectivitySelections ;
  oslc_config:selects em:Usage_WM_Motor_pre27,
                      em:Usage_WM_Housing_27,
                      em:Usage_WM_ControlUnit_v1 .
```

The Housing's own configuration (the one pinned by `Usage_WM_Housing_27`, namely Housing V4.2) is itself a configuration whose own `selects` includes the unconditional `Housing→UpperHousing` and `Housing→LowerHousing` PartUsage versions. Recursive walking through PartUsage versions yields the resolved BOM.

### Freezing the result as a baseline

A baseline of the stream taken at this moment freezes both the `selects` triples and the `effectivityContext` reference:

```turtle
em:Baseline_EcoLine_2027_06 a oslc_config:Baseline ;
  oslc_config:component          em:WashingMachine ;
  oslc_config:baselineOfStream   em:Stream_EcoLine ;
  oslc_config:effectivityContext em:Ctx_2027_06_01 ;     # frozen
  oslc_config:selections         em:sel_effectivity_bl .

em:sel_effectivity_bl a oslc_config:EffectivitySelections ;
  oslc_config:selects em:Usage_WM_Motor_pre27,
                      em:Usage_WM_Housing_27,
                      em:Usage_WM_ControlUnit_v1 .
```

Editing `em:Ctx_2027_06_01` or replacing the reference on `em:Stream_EcoLine` re-triggers the N.5 computation for the stream; the baseline is unaffected. The baseline carries its own audit trail — the `effectivityContext` reference identifies exactly which context produced its survivors.

A parallel example showing `oslc_config:VariabilitySelections` and `oslc_config:variabilityContext` is left for a future revision (Section M is normative; the example will be added when the variability surface is exercised).

---

## Open Questions for the OSLC-OP Configuration Management TC

1. **Resolution order normativity.** Section M.7 specifies variability-before-effectivity computation. Is this the right normative order, or should the spec leave composition order to domain specs? Existing PLM systems differ in framing — Windchill applies the option filter first, then ConfigSpec for revisions; Teamcenter treats Revision Rule and Variant Rule as independent and the order is effectively client-driven; Aras runs Configurator Services and Effectivity Services separately, composed by the calling application. A normative order simplifies interoperability at the cost of ruling out a small number of edge cases.

2. **Is the request-time header/query mechanism needed at all?** Sections N.4 and M.4 attach the effectivity and variability contexts to the configuration via `oslc_config:effectivityContext` / `oslc_config:variabilityContext` properties. This covers baselines (frozen) and streams (default context with server-maintained `selects`). A request-time `Effectivity-Context` / `Variability-Context` header (or `?oslc_config.effectivity=` / `?oslc_config.variability=` query parameter) is currently described as an optional secondary mechanism for transient overrides on streams. Is this still useful, or does the property mechanism alone suffice? Removing the header simplifies the spec but loses the ability to do one-off resolutions without persisting the context on the stream.

3. **Inline vs. resource-by-reference contexts.** Both attachment mechanisms (property and request-time) require the context to be a server-resident resource referenced by URI. Should an inline context (a small RDF body) be permitted for one-off resolutions, particularly in the header/query case if it is retained?

4. **Predicate validation.** The spec says malformed predicate evaluations result in 500 errors. Should there be a lightweight conformance check that domain specifications can require, so authoring tools catch malformed predicates at creation time rather than at computation time?

5. **Authorization metadata.** PLM domains (notably Windchill) require every effectivity record to be tied to an authorizing Change Notice. Should `oslc_config:authorizedBy` be a defined property of `EffectivitySelections` and `VariabilitySelections` (pointing to a change-management resource), or is this best left to the domain spec?

6. **Override semantics.** Teamcenter Revision Rules support `Override` and `Precise` clauses that pin a specific revision regardless of effectivity. Should the spec define a "pinned" outcome — a way for the predicate to express "use this version regardless of context" — or rely on domain spec conventions?

## Coordination with the OSLC PLM Specification

Adoption of this extension implies corresponding work in the OSLC Product Lifecycle Management specification (`plm-spec.html`). At minimum:

- Define the effectivity-predicate vocabulary on `oslc_plm:Part` versions (revision effectivity) and `oslc_plm:PartUsage` versions (occurrence effectivity).
- Define the Effectivity-Context resource shape, including parameter types (date, serial range, unit range with end-item qualifier, lot, model) and any namespace requirements.
- Define the variability-predicate vocabulary (option-set boolean expressions) on the same version types.
- Define the Variability-Context resource shape, including the option-set / feature-tree model and any rule-resolution semantics.
- Provide worked examples that exercise both kinds of selection against a single configured product structure.
- Note the relationship to STEP / EXPRESS effectivity concepts where alignment is intended.

These edits to `plm-spec.html` are out of scope for this Configuration Management extension document but are normative dependencies of any PLM-domain implementation of these selection types.
