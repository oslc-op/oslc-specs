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
2. *Given the versions chosen in (1), which are actually members of the resolved structure under the current effectivity context, and which, if any, must be re-bound to a different version on the basis of release effectivity?*

This section defines `oslc_config:EffectivitySelections`, a selection type that names the second question explicitly and integrates its result with the resolution defined in Section 12. The predicate language used to express effectivity, and the structure of effectivity parameters, are not defined by this specification and *SHOULD* be defined by domain specifications that import this mechanism. [CONFIG-RES-N1] The proposed OSLC-OP Product Lifecycle Management specification is the intended location for the effectivity-predicate language and the effectivity-context resource shape; existing PLM vocabularies and standards (including concepts borrowed from STEP and EXPRESS) are candidate sources.

This mechanism is independent of, and *MAY* be combined with, `oslc_config:UnboundSelections` and `oslc_config:VariabilitySelections` (Section M). Where multiple selection types are present in a configuration, `oslc_config:UnboundSelections` is resolved first (by mechanisms outside the scope of this specification) to produce concrete version bindings; `oslc_config:VariabilitySelections` is then applied to determine which option-conditioned bindings survive; and `oslc_config:EffectivitySelections` is applied last to refine the surviving bindings against the supplied effectivity context. The composition order is normative because effectivity expressions on a Part revision MAY presuppose option choices already resolved by variability.

### N.2 Resource Shape for EffectivitySelections

- **Describes:** `http://open-services.net/ns/config#EffectivitySelections`
- **Summary:** A selections resource whose selected version resources participate in effectivity-based binding refinement.
- **Description:** An `oslc_config:EffectivitySelections` resource is a specialization of `oslc_config:Selections` whose `oslc_config:selects` triples reference version resources that carry effectivity predicates. At resolution time, these predicates are evaluated against an effectivity context supplied with the request (see N.4), and the result either confirms the selected version, replaces it with a different version of the same versioned resource, or removes the selection entirely.

**EffectivitySelections Properties**

| *Prefixed Name* | *Occurs* | *Read-only* | *Value-type* | *Representation* | *Range* | *Description* |
| --- | --- | --- | --- | --- | --- | --- |
| `oslc_config:selects` | Zero-or-many | unspecified | Resource | Reference | `oslc_config:VersionResource` | The target resource *MUST* be a version resource [CONFIG-RES-N2]. Targets *MUST NOT* be of type `oslc_config:Configuration` or `oslc_config:Component` [CONFIG-RES-N3]. The version resource targeted by this property is a *candidate*: it is the version that would be selected by normal version resolution (Section 12) and that will be subjected to effectivity evaluation before final binding. Targets MAY be of any versioned resource type that carries effectivity predicates, including (in the PLM domain) `oslc_plm:Part` and `oslc_plm:PartUsage` versions. This property is read-only for selections of a baseline, but *MAY* be modifiable for selections of a stream [CONFIG-RES-N4]. |
| `rdf:type` | One-or-many | unspecified | Resource | Reference | `rdfs:Class` | A resource type URI. An `EffectivitySelections` resource *MUST* have at least the resource types `oslc_config:Selections` and `oslc_config:EffectivitySelections` [CONFIG-RES-N5]. An `EffectivitySelections` resource *MUST NOT* also have the type `oslc_config:UnboundSelections` or `oslc_config:VariabilitySelections` [CONFIG-RES-N6]; these selection mechanisms are independent and apply at different stages of resolution. |

Servers *MAY* define additional properties on `EffectivitySelections` resources, including domain-specific properties that constrain or annotate the effectivity evaluation (for example, the version of an effectivity predicate language assumed by the selections, or a reference to an effectivity-parameter schema). [CONFIG-RES-N7]

### N.3 Effectivity Predicates on Version Resources

The version resources referenced by `oslc_config:EffectivitySelections.selects` are expected to carry effectivity information that the server can evaluate against an effectivity context. This specification does not define the form of that information; it *MUST* be defined by a domain specification. [CONFIG-RES-N8] In the PLM domain, this definition is expected to appear in `plm-spec.html`, covering at minimum:

- The effectivity-predicate vocabulary supported on `oslc_plm:Part` versions (revision effectivity: which revision applies under a given date/serial/unit context).
- The effectivity-predicate vocabulary supported on `oslc_plm:PartUsage` versions (occurrence effectivity: whether and which version of a PartUsage participates in a BOM under a given context).
- The form of effectivity context values (date, serial range, unit range, model, end-item namespace).

A version resource referenced from an `EffectivitySelections` resource *MAY* declare effectivity that:

1. **Conditions its membership** — the predicate yields a Boolean value that, if false, causes the selection to be removed (see N.5).
2. **Conditions the selection of a sibling version** — the predicate yields a version URI for a different version of the same versioned resource, which replaces the selected version. This is the form taken by *release effectivity* on revisions of a versioned resource, where the candidate version is replaced by the version released as of the effectivity date or serial/unit value.

Domain specifications *MUST* define which of these forms (or both) apply to each kind of versioned resource they describe. [CONFIG-RES-N9]

Multiple effectivity predicates *MAY* apply to a single version resource — for example, a Part revision may be effective on Product ABC for units 1–5 and on Product XYZ for units 9–11 simultaneously. Domain specifications *MUST* define how multiple effectivity records on a single version interact under evaluation (typically a logical OR across records keyed by end-item context). [CONFIG-RES-N9a]

### N.4 Effectivity Context

Effectivity evaluation requires a context of parameter assignments (for example, date, serial number, unit number, end-item, option set). This context is distinct from the configuration context defined in Section 4, although the two are evaluated together when resolving requests.

A client requests an effectivity context by adding a query string `oslc_config.effectivity` to the request URI, whose value is an angle-bracket-delimited URI reference to an effectivity context resource, escaped as defined in [[OSLCCore3]]:

```
?oslc_config.effectivity=uri_ref_esc
```

The referenced resource *MUST* be of a type defined by a domain specification, and *MUST* carry the parameter assignments to be supplied to effectivity predicate evaluation. [CONFIG-RES-N10] The PLM domain specification is expected to define the effectivity-context resource shape, including the parameter types it admits (date, serial range, unit range, model, end-item) and any namespace requirements (for example, that unit numbers be qualified by an end-item reference, since the same unit number is meaningful only within an end-item's serial namespace).

A configuration context and an effectivity context *MAY* be supplied on the same request. If both are supplied, the configuration context is evaluated first (per Sections 4 and 12), and the effectivity context is then applied to refine the resulting bindings as defined in N.5.

A server that supports effectivity-based resolution *MUST* support the `oslc_config.effectivity` query parameter. [CONFIG-RES-N11] Servers *MAY* additionally support conveying effectivity context through a request header `Effectivity-Context`, using the same syntax as the `Configuration-Context` header. [CONFIG-RES-N12]

Where the `oslc_config.effectivity` query parameter or `Effectivity-Context` header is used, a server *SHOULD* include in the response a `Vary` header that names whichever mechanism was used, so that responses are not incorrectly cached across different effectivity contexts. [CONFIG-RES-N13]

If a configuration contains one or more `oslc_config:EffectivitySelections` resources and a request supplies no effectivity context, the server *MUST* either:

1. Apply a default effectivity context defined by the server or the configuration, if such a default exists, or
2. Fail the request with a 400 Bad Request response indicating that an effectivity context is required.

[CONFIG-RES-N14]

A baseline *MAY* declare an immutable effectivity context as part of its definition, by referencing an effectivity context resource through a property defined by the relevant domain specification. Where a baseline declares such a context, requests against that baseline *MUST* use the declared context, and the `oslc_config.effectivity` query parameter, if supplied, *MUST* either match the declared context or cause the request to fail with 400 Bad Request. [CONFIG-RES-N15] A stream *MUST NOT* declare an immutable effectivity context; effectivity for streams is always supplied per-request.

### N.5 Resolution Algorithm

When resolving a request for a versioned resource in the context of a configuration containing one or more `oslc_config:EffectivitySelections` resources, the server *MUST* apply the following algorithm. [CONFIG-RES-N16]

**Step 1 — Candidate version resolution.** Apply the version resolution algorithm defined in Section 12 across all selections in the configuration and its recursive contributions, treating `oslc_config:EffectivitySelections` resources identically to `oslc_config:Selections` resources for the purposes of candidate selection. The result is, for each concept URI in scope, either a single candidate version resource or no candidate. Where `oslc_config:VariabilitySelections` is also present (see Section M), variability resolution *MUST* be applied before this step.

**Step 2 — Effectivity binding.** For each candidate version resource produced by Step 1 that was contributed by an `oslc_config:EffectivitySelections` resource:

1. Evaluate the effectivity predicate carried by the candidate version against the effectivity context supplied with the request.
2. If the predicate yields a Boolean true, the candidate version is the final binding.
3. If the predicate yields a Boolean false, the binding is removed (no version is selected for the corresponding concept URI).
4. If the predicate yields a version URI for a different version of the same versioned resource, that version becomes the final binding, replacing the candidate.
5. If the predicate yields any other value, the server *MUST* return a 500 Internal Server Error with an `oslc:Error` describing the failure. [CONFIG-RES-N17]

Candidate version resources contributed by selections that are *not* of type `oslc_config:EffectivitySelections` are passed through Step 2 unchanged.

**Step 3 — Final resolution.** Apply the outcomes defined in Section 12 to the final bindings:

- If no version is bound for the requested concept URI, the request *MUST* fail with 404 Resource Not Found. This case includes both (a) no candidate version was selected in Step 1, and (b) a candidate version was selected in Step 1 but its effectivity predicate evaluated to false in Step 2. [CONFIG-RES-N18]
- If exactly one version is bound for the requested concept URI, the request *MUST* return that version.
- If multiple versions are bound for the requested concept URI (which *MAY* occur if multiple `EffectivitySelections` resources in different contributions evaluate to different version URIs in Step 2 for the same concept URI), the rules of Section 12 for multiple matches apply, with `oslc_config:contributionOrder` used to determine the chosen version where applicable. Servers *SHOULD* document their resolution behavior in this case. [CONFIG-RES-N19]

### N.6 Independence of Concerns

This specification deliberately preserves the separation of concerns between configurations and versioned resources established in Sections 3 and 12, while accommodating the domain reality that some versioned resources carry information that participates in selection.

The configuration is the selector: it identifies *which* version resources are in scope and *which* of those participate in effectivity-based binding. The version resources are the selected: they carry both their domain content and, where domain specifications permit, the effectivity predicates evaluated during Step 2 of N.5. The configuration does not embed the predicates; the version resources do not embed the parameter context. The resolution algorithm composes them at request time.

This factoring permits a configuration to be authored, baselined, and queried using the standard mechanisms of this specification, while supporting domain-specific effectivity logic without that logic being standardized here.

### N.7 Interaction with Existing Resolution

This section interacts with the existing resolution mechanisms of the specification as follows:

- Where a configuration contains only `oslc_config:Selections` resources (and no `EffectivitySelections`, `VariabilitySelections`, or `UnboundSelections`), resolution proceeds exactly as defined in Section 12. Effectivity and variability contexts, if supplied, have no effect.
- Where a configuration contains `oslc_config:UnboundSelections`, those are resolved as defined in Section 3.7 before Step 1 of N.5 is performed. Bindings produced by unbound-selection resolution are not subjected to effectivity refinement unless the resolved version also appears in an `EffectivitySelections` resource.
- Where a configuration contains `oslc_config:VariabilitySelections`, those are resolved as defined in Section M before Step 1 of N.5 is performed.
- Where a configuration contains `oslc_config:EffectivitySelections`, those participate in Step 1 of N.5 as candidates and are refined in Step 2.
- Where a change set overrides a configuration containing `EffectivitySelections` resources, the change set's selections are applied as defined in Section 13 before Step 1 of N.5 is performed. A change set *MAY* itself contain `EffectivitySelections` resources, in which case those participate in Step 2.

### N.8 Caching, Tracked Resource Sets, and Compact Rendering

Implementations *SHOULD* note the following operational considerations:

- Cached responses *MUST* be keyed on the configuration context, the variability context (where applicable), and the effectivity context. Servers *SHOULD* return appropriate `Vary` headers on responses to requests that supplied an effectivity or variability context. [CONFIG-RES-N20]
- Tracked Resource Sets (Section 16) only track version resources. It is the responsibility of the TRS consumer to provide an appropriate Configuration-Context, Variability-Context (where applicable), and Effectivity-Context to resolve tracked version resources to a selected version.
- Compact rendering (Section 15) of resources resolved through `EffectivitySelections` *MUST* reflect the effectivity context (and, where applicable, the variability context) supplied with the initial request for the compact resource, and the response *MUST* be valid under that combination of contexts. [CONFIG-RES-N22]

### N.9 Conformance

A configuration server claiming conformance to this section *MUST*:

1. Support `oslc_config:EffectivitySelections` resources in configurations as defined in N.2.
2. Support the `oslc_config.effectivity` query parameter as defined in N.4.
3. Implement the resolution algorithm defined in N.5.
4. Honor declared effectivity contexts on baselines, where present, as defined in N.4.
5. Provide caching headers consistent with N.8.

A configuration server *MAY* claim conformance to the base specification (Sections 1–20) without conforming to this section. Servers that do not conform to this section *MUST* reject any configuration POSTed to them that contains `oslc_config:EffectivitySelections` resources with a 400 Bad Request response, and *MUST NOT* honor the `oslc_config.effectivity` query parameter.

---

## Section M. Variability-Based Resolution

*This section is normative.*

### M.1 Motivation and Scope

A second category of domains — again, Product Lifecycle Management most prominently — requires resolution against configuration option choices that are orthogonal to the time/serial dimension addressed by Section N. A 150% product structure carries every candidate part and usage that *could* appear in *any* configuration of the product; the variability filter resolves it down to the 100% structure that applies to *this* configuration of options (Engine = V6, Color = Red, Market = EU, etc.).

Variability is distinct from effectivity:

- Effectivity asks "given the current date / serial / unit / model, which version applies?"
- Variability asks "given the chosen option values, which version applies?"

In practice all major PLM systems (PTC Windchill, Siemens Teamcenter, Aras Innovator) treat these as separate filtering passes with separate predicate languages — option-set boolean expressions (Teamcenter's Modular Variant Language, Windchill's assigned-expression filter, Aras's Configurator rules) for variability; range comparisons over date/serial/unit for effectivity. This specification preserves that separation by defining `oslc_config:VariabilitySelections` as a sibling to `oslc_config:EffectivitySelections` rather than collapsing both into one mechanism.

This section defines `oslc_config:VariabilitySelections`. The predicate language used to express variability conditions, and the structure of variability parameters (option sets, feature trees, variant rules), are not defined by this specification and *MUST* be defined by domain specifications that import this mechanism. [CONFIG-RES-M1] The proposed OSLC-OP Product Lifecycle Management specification is the intended location for the variability-predicate language and the variability-context resource shape.

### M.2 Resource Shape for VariabilitySelections

- **Describes:** `http://open-services.net/ns/config#VariabilitySelections`
- **Summary:** A selections resource whose selected version resources participate in variability-based binding refinement against a context of configuration option choices.
- **Description:** An `oslc_config:VariabilitySelections` resource is a specialization of `oslc_config:Selections` whose `oslc_config:selects` triples reference version resources that carry variability predicates over a configuration option set. At resolution time, these predicates are evaluated against a variability context supplied with the request (see M.4), and the result either confirms the selected version or removes the selection entirely.

**VariabilitySelections Properties**

| *Prefixed Name* | *Occurs* | *Read-only* | *Value-type* | *Representation* | *Range* | *Description* |
| --- | --- | --- | --- | --- | --- | --- |
| `oslc_config:selects` | Zero-or-many | unspecified | Resource | Reference | `oslc_config:VersionResource` | The target resource *MUST* be a version resource [CONFIG-RES-M2]. Targets *MUST NOT* be of type `oslc_config:Configuration` or `oslc_config:Component` [CONFIG-RES-M3]. The version resource targeted by this property is a *candidate*: it is the version that would be selected by normal version resolution and that will be subjected to variability evaluation before final binding. Targets MAY be of any versioned resource type that carries variability predicates, including (in the PLM domain) `oslc_plm:Part` and `oslc_plm:PartUsage` versions. This property is read-only for selections of a baseline, but *MAY* be modifiable for selections of a stream [CONFIG-RES-M4]. |
| `rdf:type` | One-or-many | unspecified | Resource | Reference | `rdfs:Class` | A resource type URI. A `VariabilitySelections` resource *MUST* have at least the resource types `oslc_config:Selections` and `oslc_config:VariabilitySelections` [CONFIG-RES-M5]. A `VariabilitySelections` resource *MUST NOT* also have the type `oslc_config:UnboundSelections` or `oslc_config:EffectivitySelections` [CONFIG-RES-M6]. |

Servers *MAY* define additional properties on `VariabilitySelections` resources, including domain-specific properties that constrain or annotate the variability evaluation (for example, the version of a variability predicate language assumed by the selections, or a reference to an option-set schema). [CONFIG-RES-M7]

### M.3 Variability Predicates on Version Resources

The version resources referenced by `oslc_config:VariabilitySelections.selects` are expected to carry variability information that the server can evaluate against a variability context. This specification does not define the form of that information; it *MUST* be defined by a domain specification. [CONFIG-RES-M8] In the PLM domain, this definition is expected to appear in `plm-spec.html`, covering at minimum:

- The variability-predicate vocabulary supported on `oslc_plm:Part` versions and `oslc_plm:PartUsage` versions (boolean expressions over named option values).
- The form of the variability context (a set of option-value assignments, optionally with a variant rule reference).
- Constraints on the option set itself (typed options, named values, cross-option exclusions).

A version resource referenced from a `VariabilitySelections` resource *MAY* declare a variability predicate that yields a Boolean value. If the predicate yields true, the candidate version is included in the resolved binding; if false, the binding is removed.

Variability predicates are *not* expected to yield substitute version URIs; the question variability answers is "is this version included?" and not "which sibling version applies?". Substitution between revisions of the same versioned resource based on lifecycle progression is the domain of effectivity (Section N), not variability.

### M.4 Variability Context

Variability evaluation requires a context of option-value assignments. This context is distinct from both the configuration context defined in Section 4 and the effectivity context defined in N.4.

A client requests a variability context by adding a query string `oslc_config.variability` to the request URI, whose value is an angle-bracket-delimited URI reference to a variability context resource, escaped as defined in [[OSLCCore3]]:

```
?oslc_config.variability=uri_ref_esc
```

The referenced resource *MUST* be of a type defined by a domain specification, and *MUST* carry the option-value assignments to be supplied to variability predicate evaluation. [CONFIG-RES-M10]

A configuration context, a variability context, and an effectivity context *MAY* all be supplied on the same request. If multiple are supplied, the configuration context is evaluated first (per Sections 4 and 12), the variability context is then applied to filter option-conditioned candidates, and the effectivity context is applied last to refine the surviving bindings against instance/timing parameters.

A server that supports variability-based resolution *MUST* support the `oslc_config.variability` query parameter. [CONFIG-RES-M11] Servers *MAY* additionally support conveying variability context through a request header `Variability-Context`, using the same syntax as the `Configuration-Context` header. [CONFIG-RES-M12]

Where the `oslc_config.variability` query parameter or `Variability-Context` header is used, a server *SHOULD* include in the response a `Vary` header that names whichever mechanism was used. [CONFIG-RES-M13]

If a configuration contains one or more `oslc_config:VariabilitySelections` resources and a request supplies no variability context, the server *MUST* either:

1. Apply a default variability context defined by the server or the configuration, if such a default exists, or
2. Fail the request with a 400 Bad Request response indicating that a variability context is required.

[CONFIG-RES-M14]

A baseline *MAY* declare an immutable variability context as part of its definition, by referencing a variability context resource through a property defined by the relevant domain specification. Where a baseline declares such a context, requests against that baseline *MUST* use the declared context, and the `oslc_config.variability` query parameter, if supplied, *MUST* either match the declared context or cause the request to fail with 400 Bad Request. [CONFIG-RES-M15] A stream *MUST NOT* declare an immutable variability context; variability for streams is always supplied per-request.

### M.5 Resolution Algorithm

When resolving a request for a versioned resource in the context of a configuration containing one or more `oslc_config:VariabilitySelections` resources, the server *MUST* apply the following algorithm. [CONFIG-RES-M16]

**Step 1 — Candidate version resolution.** Apply the version resolution algorithm defined in Section 12 across all selections in the configuration and its recursive contributions, treating `oslc_config:VariabilitySelections` resources identically to `oslc_config:Selections` resources for the purposes of candidate selection. The result is, for each concept URI in scope, either a single candidate version resource or no candidate.

**Step 2 — Variability binding.** For each candidate version resource produced by Step 1 that was contributed by an `oslc_config:VariabilitySelections` resource:

1. Evaluate the variability predicate carried by the candidate version against the variability context supplied with the request.
2. If the predicate yields a Boolean true, the candidate version is the final binding (subject to subsequent effectivity refinement, if applicable; see N.5).
3. If the predicate yields a Boolean false, the binding is removed (no version is selected for the corresponding concept URI).
4. If the predicate yields any other value, the server *MUST* return a 500 Internal Server Error with an `oslc:Error` describing the failure. [CONFIG-RES-M17]

Candidate version resources contributed by selections that are *not* of type `oslc_config:VariabilitySelections` are passed through Step 2 unchanged.

**Step 3 — Final resolution.** As in N.5 Step 3, with effectivity refinement (where present) applied to the surviving bindings before final resolution.

### M.6 Independence of Concerns

As with N.6, this specification preserves the separation between configurations (selectors) and version resources (selected). The configuration identifies which version resources are in scope and which participate in variability-based binding. The version resources carry their own variability predicates. The variability context is supplied separately at request time.

### M.7 Interaction with Existing Resolution and with EffectivitySelections

`VariabilitySelections` and `EffectivitySelections` are independent and *MAY* coexist in a single configuration. When both are present:

1. Candidate version resolution (M.5 Step 1 / N.5 Step 1) is performed identically — both selection types contribute candidates to the version-resolution pass of Section 12.
2. Variability binding (M.5 Step 2) is performed *before* effectivity binding (N.5 Step 2). This order is normative: effectivity predicates on a Part revision MAY presuppose option choices that variability has already resolved, but the reverse is not expected. [CONFIG-RES-M18]
3. The surviving bindings from variability are then subjected to effectivity refinement.
4. Step 3 final resolution is performed once at the end.

A change set *MAY* contain `VariabilitySelections` resources alongside `EffectivitySelections` resources; both participate in their respective resolution steps.

### M.8 Caching, Tracked Resource Sets, and Compact Rendering

Same considerations as N.8: cached responses *MUST* be keyed on every context supplied (configuration, variability, effectivity), `Vary` headers should reflect every mechanism used, and Compact rendering must reflect the full context combination.

### M.9 Conformance

A configuration server claiming conformance to this section *MUST*:

1. Support `oslc_config:VariabilitySelections` resources in configurations as defined in M.2.
2. Support the `oslc_config.variability` query parameter as defined in M.4.
3. Implement the resolution algorithm defined in M.5.
4. When also conforming to Section N, apply variability binding before effectivity binding as defined in M.7.
5. Honor declared variability contexts on baselines, where present, as defined in M.4.
6. Provide caching headers consistent with M.8.

A configuration server *MAY* claim conformance to the base specification (Sections 1–20) without conforming to this section. Servers that do not conform to this section *MUST* reject any configuration POSTed to them that contains `oslc_config:VariabilitySelections` resources with a 400 Bad Request response, and *MUST NOT* honor the `oslc_config.variability` query parameter.

A server *MAY* conform to Section N alone, to Section M alone, or to both.

---

## Open Questions for the OSLC-OP Configuration Management TC

1. **Resolution order normativity.** Section M.7 specifies variability-before-effectivity binding. Is this the right normative order, or should the spec leave composition order to domain specs? Existing PLM systems differ in framing — Windchill applies the option filter first, then ConfigSpec for revisions; Teamcenter treats Revision Rule and Variant Rule as independent and the order is effectively client-driven; Aras runs Configurator Services and Effectivity Services separately, composed by the calling application. A normative order simplifies interoperability at the cost of ruling out a small number of edge cases.

2. **Context as URI reference vs. inline.** The query parameter mechanism (`?oslc_config.effectivity=<uri>`) requires the client to first create a context resource on the server. Should an inline context (a small RDF body in the request, perhaps via a header containing a Turtle blob) also be permitted for short-lived per-request contexts? The current spec mirrors the existing `Configuration-Context` pattern for consistency.

3. **Predicate validation.** The spec says invalid predicate evaluations result in 500 errors. Should there be a lightweight conformance check that domain specifications can require, so authoring tools catch malformed predicates at creation time rather than at resolution time?

4. **Authorization metadata.** PLM domains (notably Windchill) require every effectivity record to be tied to an authorizing Change Notice. Should `oslc_config:authorizedBy` be a defined property of `EffectivitySelections` and `VariabilitySelections` (pointing to a change-management resource), or is this best left to the domain spec?

5. **Override semantics.** Teamcenter Revision Rules support `Override` and `Precise` clauses that pin a specific revision regardless of effectivity. Should the spec define a "pinned" outcome — a way for the predicate to express "use this version regardless of context" — or rely on domain spec conventions?

## Coordination with the OSLC PLM Specification

Adoption of this extension implies corresponding work in the OSLC Product Lifecycle Management specification (`plm-spec.html`). At minimum:

- Define the effectivity-predicate vocabulary on `oslc_plm:Part` versions (revision effectivity) and `oslc_plm:PartUsage` versions (occurrence effectivity).
- Define the Effectivity-Context resource shape, including parameter types (date, serial range, unit range with end-item qualifier, lot, model) and any namespace requirements.
- Define the variability-predicate vocabulary (option-set boolean expressions) on the same version types.
- Define the Variability-Context resource shape, including the option-set / feature-tree model and any rule-resolution semantics.
- Provide worked examples that exercise both kinds of selection against a single configured product structure.
- Note the relationship to STEP / EXPRESS effectivity concepts where alignment is intended.

These edits to `plm-spec.html` are out of scope for this Configuration Management extension document but are normative dependencies of any PLM-domain implementation of these selection types.
