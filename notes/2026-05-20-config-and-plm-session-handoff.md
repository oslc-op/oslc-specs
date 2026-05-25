# Session Handoff — OSLC Configuration Selections Extensions and PLM Effectivity/Variability

*Authored 2026-05-20 to capture context from the design session that ran in `~/Developer/OSLC/oslc4js/`. The work product lives in this repo and is ready to continue here. Read this top-to-bottom and you'll have the full design state, including decisions, open questions, and external context.*

## What is being handed off

Two related work products were drafted together:

1. **OSLC Configuration Management Selections Extensions** — a proposed normative addition to OSLC Configuration Management. Defines `oslc_config:EffectivitySelections` and `oslc_config:VariabilitySelections` as sibling specializations of `oslc_config:Selections`, with cross-document conjunctive resolution.
2. **OSLC PLM specification edits** — adds an "Effectivity and Variability" section to the PLM spec, defining the predicate languages, context resource shapes, and resolution semantics that the Configuration Management extensions defer to a domain specification.

A third document captures how three commercial PLM systems (Windchill, Teamcenter, Aras Innovator) would conform to these specifications.

## Working-tree state in `oslc-specs/`

All five work-product files are committed and pushed to `https://github.com/oslc-op/oslc-specs` master at commit:

```
248dfd2 Added proposals for extending OSLC configuration management to refine selections
        based on effectivity and variability, and extended to PLM spec to support the
        same concepts on Parts and PartUsages.
```

Files in that commit:

```
specs/config/oslc-config-selections-extensions.md           Created (~430 lines)
specs/config/oslc-config-selections-extensions-plm-mapping.md  Created (~382 lines)
specs/plm/plm-spec.html                                     Modified (+412 lines)
specs/plm/plm-vocab.ttl                                     Modified (+218 lines)
specs/plm/plm-shapes.ttl                                    Modified (+322 lines)
```

This handoff document itself (`notes/2026-05-20-config-and-plm-session-handoff.md`) is the only remaining untracked file. Commit it separately when you're satisfied — or leave it as a working note that doesn't need to ship with the proposals.

## Where the related (already-committed) work lives

Outside this repo:

- **OSLC-Shape-Extensions proto-spec** (`oslc:superShape`, `oslc:inversePropertyLabel`, `oslc:icon`): pushed to `https://github.com/OSLC/oslc4js` master, commit `3db85108` (`docs: proto-spec — add oslc:superShape as Part 3 of OSLC-Shape-Extensions for OSLC-OP review`). The doc is at `~/Developer/OSLC/oslc4js/docs/OSLC-Shape-Extensions.md`. Already shared for OSLC-OP review.
- **Implementation plan for `oslc:superShape`**: pushed to the same repo, commit `b22589d5`, at `~/Developer/OSLC/oslc4js/docs/superpowers/plans/2026-05-14-oslc-supershape-extension.md`. Parked pending OSLC-OP feedback before any code lands.

The Selections Extensions in *this* repo reference `oslc:superShape` in their PLM-mapping document but do not depend on it for adoption. The Effectivity/Variability mechanisms work without `oslc:superShape`; the proto-spec just makes the AAKI ontology elegant when it lands.

## Design intent and decisions

### Why two sibling selection types, not one

Effectivity and variability address different lifecycle aspects:

- **Effectivity** = instance/timing dimension (date, serial, unit, lot, model, end-item).
- **Variability** = configuration option dimension (feature/option choices, variant rules).

All three commercial PLM systems researched (Windchill, Teamcenter, Aras) treat these as separate filtering passes with separate predicate languages — Windchill's Option Filter vs. ConfigSpec, Teamcenter's Variant Rule vs. Revision Rule, Aras's Configurator Services vs. Effectivity Services. Collapsing them in one OSLC mechanism would break the conceptual alignment with all three.

### PartUsage as the structural-relationship handle

`oslc_plm:PartUsage` reifies the parent-to-child "uses" relationship in a BOM, with `oslc_plm:represents` pointing at the child Part. Since PartUsage is itself a versioned resource, BOM-line participation falls out naturally: a configuration's selections include PartUsage versions, and effectivity/variability predicates can sit on those versions directly. This means there is no need for a separate "effectivity on usage links" mechanism in OSLC Configuration Management — the existing Section 12 version-resolution mechanism handles it as soon as PartUsage is a first-class versioned resource.

### Conjunctive semantics (no override)

The earlier override-semantics design was dropped in favor of IS-A-style conjunction: an instance must satisfy the subshape's own constraints AND every constraint inherited from any reachable super shape, transitively. Contradictions are unsatisfiable shape errors at parse time. This matches the principled vocabulary IS-A inherent in `rdfs:subClassOf` and removes ambiguity about how multiple effectivity records combine (they OR together; conflicts in the conjoined constraint surface as authoring errors).

This decision is documented in the OSLC-Shape-Extensions proto-spec; the Selections Extensions inherit it by reference.

### Per-constraint-type conjunction rules

For each `oslc:property` constraint type, a specific conjunction rule is specified:

- `oslc:occurs` → interval intersection (most-restrictive cardinality wins; all four OSLC named cardinalities have non-empty pairwise intersections so contradictions are impossible with v1)
- `oslc:valueType`, `oslc:range`, `oslc:representation`, `oslc:name` → equality required; mismatch is a contradiction
- `oslc:readOnly`, `oslc:hidden` → logical OR (any contributor's `true` makes the effective value `true`; subshape cannot reopen what a parent has frozen)
- `oslc:allowedValue(s)` → set intersection (empty intersection is a contradiction)
- `dcterms:description`, `oslc:inversePropertyLabel` → display-only; subshape's value wins for rendering; super values discoverable through introspection
- `oslc:icon` (on shape, not on `oslc:property`) → identity attribute; not inherited

These rules live in the OSLC-Shape-Extensions proto-spec but are referenced normatively from the Selections Extensions.

### Cross-document resolution via linked-data dereference

`oslc:superShape` (and by analogous reasoning, cross-document predicates in effectivity/variability) URIs dereference at the namespace authority that controls them. `http://open-services.net/ns/{am,rm,cm,qm,ccm}` already host the canonical base shapes that domain extensions inherit from. The AAKI ontology (separate work in `oslc4js/`) demonstrates inheriting from `http://open-services.net/ns/am/shapes/3.0#ResourceShape` — the OASIS-standard AM Resource shape — to pick up the common OSLC core properties (`title`, `description`, `creator`, `created`, `modified`, `serviceProvider`, `instanceShape`, plus the six common link types `derives` / `elaborates` / `refine` / `external` / `satisfy` / `trace`).

For Configuration Management: the resolution algorithm for cross-document parents specifies HTTP fetch (`Accept: text/turtle, application/rdf+xml`) per linked-data convention. Local caching is an optimization, not a substitute for the canonical namespace-authority dereference. Unresolved parents are a hard error, never a silent skip.

### `rdfs:subClassOf` consistency

When a subshape declares `oslc:superShape <SuperShape>` and both declare `oslc:describes`, the subshape's described class SHOULD also be `rdfs:subClassOf` the super shape's described class (transitively) in the vocabulary. This is SHOULD, not MUST — two reasons documented in the proto-spec:

1. Cross-document authoring: vocabulary and shape documents are often separate (BMM today has `BMM.ttl` vocab vs. `BMM-Shapes.ttl` shapes), and validating consistency at shape-parse time would couple parsing to vocabulary resolution.
2. Mixin shapes: a super shape MAY omit `oslc:describes` to act as a pure constraint mixin (a "metadata-fields-mixin" sharing dublin core terms across many unrelated shapes). Subshapes inheriting from such a mixin have no `rdfs:subClassOf` obligation.

### Variability-before-effectivity resolution order

The Selections Extensions specify normatively that when both `VariabilitySelections` and `EffectivitySelections` are present in a configuration, variability resolution is performed first, then effectivity refinement. Justification: effectivity predicates on a Part revision MAY presuppose option choices that variability has already resolved (e.g., "this revision is effective starting unit 50 only when Market = EU"), but the reverse is not expected.

This order matches all three commercial PLM systems:

- Windchill: Option Filter → ConfigSpec → Effectivity ConfigSpec
- Teamcenter: Variant Rule (independent) → Revision Rule with Effectivity Cursor
- Aras: Configurator Services → Effectivity Services

### Three operations, one conjunction operator

The conjunction operator governs three related-but-distinct scenarios. The PLM-mapping doc has a three-axis comparison table:

| Operation | Consumer | Conflict semantics | Author-controlled? |
|---|---|---|---|
| Authored inheritance (`oslc:superShape`) | Server-side validation | Conjunction with hard rejection on contradiction | Yes — explicit in shape document |
| Ad-hoc multi-shape application | Server-side validation | Same | Yes — server runtime configuration |
| Cross-context merging (LQE `merge:mergeShape`) | Federated reporting | Union with UI flagging | No — system-injected |

Only the first two are in scope for this extension; the third (LQE-style cross-context merging) is called out as future OSLC-OP work.

### Predicate languages deferred to domain spec

The Configuration Management Selections Extensions deliberately do not define the predicate language or the parameter schema for either Effectivity or Variability. Those are domain-specific and supplied by the PLM specification. This keeps the CM extension small and reusable; the PLM spec carries the domain-specific weight.

The PLM spec edits in this session add:

- `oslc_plm:Effectivity` class with `effectiveValueType` (one of `oslc_plm:{Date, UnitNumber, SerialNumber, Lot, Model}`), `effectiveForEndItem`, `effectiveFrom`, `effectiveTo`, `effectiveReplacement` (for release-effectivity supersede), `authorizedBy` (change-management linkage).
- `oslc_plm:EffectivityContext` class with `effectivityDate`, `effectivityEndItem`, `effectivityUnitNumber`, `effectivitySerialNumber`, `effectivityLot`, `effectivityModel`. Note: unit/serial values MUST be qualified by `effectivityEndItem` since the same unit/serial number is meaningful only within an end-item's namespace.
- `oslc_plm:OptionSet`, `oslc_plm:Option`, `oslc_plm:OptionValue` classes for the variability option model.
- `oslc_plm:VariabilityCondition` — a boolean expression AST with `oslc_plm:{and, or, not, optionEquals}` terminals.
- `oslc_plm:VariabilityContext` carrying `choice` (multi-valued OptionValue assignments) and `appliesToOptionSet`.

## Open questions for OSLC-OP

### From the Selections Extensions document

1. **Flattened representation negotiation.** Should servers be required to advertise the flattened (conjoined) shape representation under a content-type or query parameter, so legacy clients see effective constraints without resolving the inheritance chain themselves?
2. **Per-constraint-type conjunction completeness.** Is the conjunction table complete enough, or do specific OSLC profiles (RM, CM, QM, AM, AAKI/BMM) introduce constraint types that need their own conjunction rules?
3. **Cross-context shape merging.** Should a future OSLC-OP extension formalize the LQE pattern (`merge:mergeShape`, deterministic merged URIs from `https://jazz.net/ns/lqe/merge/gensym/<domain>/<shapeName>`) under the `oslc:` namespace?

### From the PLM-mapping document (recommendations to the CM TC)

1. **Add `oslc_plm:Block` to the effectivity value-type identifiers.** Block effectivity is distinct from Lot in Windchill (used in aerospace); folding it into Lot loses fidelity.
2. **Open-extension mechanism for `EffectivityContext`.** Aras's customizable variables (Plant, Region, etc.) and similar features in other products suggest the fixed property list is insufficient. An `oslc_plm:effectivityParameter` key/value bag with declared types would help.
3. **Standardize lifecycle-state-keyed effectivity.** Teamcenter's per-`ReleaseStatus` effectivity is a useful pattern; adding `oslc_plm:lifecycleState` on `Effectivity` would let adapters preserve the distinction.
4. **Explicit pinned outcome.** Teamcenter's `Override` and `Precise` Revision Rule clauses pin a specific revision regardless of context. The current spec uses `effectiveReplacement` as the only substitution mechanism; an explicit "pinned" outcome would be cleaner.
5. **Make `authorizedBy` MAY rather than SHOULD.** Only Windchill standardizes the change-authorization linkage reliably; an aspirational SHOULD may prompt adapters in other systems to invent linkage that doesn't exist.
6. **Document the "Option vs. OptionValue" naming hazard.** Aras uses "Option" for OSLC's "OptionValue"; an informative note in the PLM spec would prevent confusion in adapter implementations.

## PLM product research summary

Three subagents researched the three systems' effectivity and variability models. Full findings are in the PLM-mapping document. Distilled takeaways:

### PTC Windchill

- **Data model**: WTPartMaster (identity), WTPart (revision), WTPartUsageLink (BOM line, points at child master, not child revision). Child revision selected by ConfigSpec at navigation time.
- **Effectivity**: directly on the WTPart revision via `wt.effectivity` framework. Types: Date, Lot, Block, Serial, MSN, Unit (gated by part trace code). `EffContext` = end-item / product instance (first-class). Multiple effectivity records per revision per context.
- **Variability**: Option Sets, Choices, assigned expressions on configurable modules. Evaluated by the Option Filter as a separate pass.
- **Resolution**: Option Filter → ConfigSpec → Effectivity ConfigSpec. Three-stage filter with Effectivity ConfigSpec being `EffConfigSpecGroup`.
- **Authorization**: Every Effectivity object tied to a Change Notice (`PendingEffectivity` → `ActualEffectivity`). REST: OData EffectivityMgmt domain.

### Siemens Teamcenter

- **Data model**: Item / ItemRevision / BOMViewRevision / ChildElement (occurrence). Revision rule + Variant rule supplied separately at navigation.
- **Effectivity**: two attachment points — `ReleaseStatus` of ItemRevision (revision effectivity) or BOM occurrence (occurrence effectivity). Date and Unit-number effectivity are first-class; unit numbers always require an End Item (Configuration Item) for namespacing. Effectivity Groups aggregate `(end_item, unit_range)` tuples OR-joined.
- **Variability**: Modular Variant Language (MVL) for boolean expressions over Option Sets. Classic Variants and Modular Variants subsystems.
- **Resolution**: Revision Rule (ordered clauses, first match wins) + Variant Rule (independent). Override/Precise clauses pin specific revisions through filters.
- **Nested effectivity**: end-item namespace switches at Configuration Item boundaries during traversal.

### Aras Innovator

- **Data model**: Part (master), Part revision, Part BOM relationship rows. Effectivity sits on the **BOM row** (not the Part revision).
- **Effectivity**: Effectivity Expression on a BOM row. Three standard variables (Date, Model, Unit) plus admin-defined variables. Expression is flat conjunction of EQ terminals; ranges live on the criterion side, not the expression.
- **Variability**: separate Configurator Services layer (Features/Options/Rules/Variability Item). Uses "Option" for what OSLC calls "OptionValue" — name collision worth flagging.
- **Resolution**: Effectivity Resolution Engine + Configurator Services. Lifecycle "Fix" locks released Parts to specific child revisions at release; no native "replace" outcome (modeled as two rows with disjoint expressions).
- **Documentation**: *Aras Innovator 31 — Effectivity Services Programmer's Guide* (D-008103), *Variant Management 33 Administrator Guide* (D-007881).

## Suggested next steps in the new session

1. **Read this handoff doc top-to-bottom.**
2. **Read the three artifacts in order** to ground the context (all already on master at commit `248dfd2`):
   - `specs/config/oslc-config-selections-extensions.md`
   - `specs/plm/plm-spec.html` (the new "Effectivity and Variability" section, plus the vocab/shapes additions in `plm-vocab.ttl` and `plm-shapes.ttl`)
   - `specs/config/oslc-config-selections-extensions-plm-mapping.md`
3. **Refinement candidates** (any subsequent commits would land on top of `248dfd2`):
   - **Worked examples.** The current ones are minimal. A fuller BMM-style example exercising both Effectivity and Variability against the same product structure would help reviewers.
   - **Conformance language alignment.** The current MUST/SHOULD/MAY wording is internally consistent but may benefit from a final pass against the existing OSLC-OP house style.
   - **HTML conversion.** The two `.md` files in `specs/config/` are Markdown working documents. The existing config-spec artifacts are HTML (`oslc-config-mgt.html`, `config-resources.html`, `versioned-resources.html`). At some point these would either fold into `oslc-config-mgt.html` as new sections, or become a new HTML spec under the same directory.
   - **Companion shapes file.** No machine-readable `.ttl` shapes file accompanies the new Markdown selections-extensions doc yet. If/when the spec firms up, adding `oslc-config-selections-shapes.ttl` to formalize `EffectivitySelections` / `VariabilitySelections` shapes would parallel the pattern of `config-shapes.ttl`.
4. **Submit for OSLC-OP review.** The proto-spec on `oslc:superShape` (`oslc4js`) has been ready for review since 2026-05-15; these Selections Extensions and the PLM spec changes are the second batch. The PLM-mapping document is intended as informative reviewer support — non-normative.

## External references

- OSLC-Shape-Extensions proto-spec (already shared): https://github.com/OSLC/oslc4js/blob/master/docs/OSLC-Shape-Extensions.md
- LQE's `merge:mergeShape` documentation: https://jazz.net/library/article/91481 ("A look inside LQE and Report Builder")
- OSLC AM Resource shape (the canonical cross-document inheritance base): https://docs.oasis-open-projects.org/oslc-op/am/v3.0/os/architecture-management-shapes.html and locally at `specs/am/architecture-management-shapes.ttl`
- Aras Innovator 31 — Effectivity Services Programmer's Guide (D-008103)
- jazz.net article 91481 — IBM ELM LQE/Report Builder type-system merging
- PTC Windchill REST EffectivityMgmt domain (PTC documentation, behind auth)

## What's deliberately out of scope here

- **Implementation in oslc4js.** No code lands until OSLC-OP feedback returns. The `oslc:superShape` implementation plan in `oslc4js/docs/superpowers/plans/2026-05-14-oslc-supershape-extension.md` is parked pending feedback.
- **AAKI ontology.** Tracked separately in `oslc4js/docs/superpowers/plans/2026-05-19-aaki-ontology.md`. Related (it'll demonstrate cross-document inheritance from `oslc_am:ResourceShape`), but its own work product.
- **Other OSLC profile updates.** No changes to RM, CM, QM, or AM specs needed for these proposals. The PLM spec is the only domain spec that needs to be touched to back the Configuration Management extensions.
