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

---

## Session of 2026-05-25 — Refinement progress

*Five days after the original handoff. The work products at `248dfd2` were taken as the starting point; this session refined the design substantially and shipped the changes in commit `79daac6` on `master`. The notes below record what changed, what was decided, and what's now next.*

### What changed in the spec documents

1. **Post-filter `selects` semantics — the core reframe.** `oslc_config:EffectivitySelections.selects` and `oslc_config:VariabilitySelections.selects` no longer hold pre-evaluation *candidates*; they hold *post-filter survivors* — the version URIs that have passed both the Configuration-Context candidate selection (Section 12) and the relevant predicate evaluation. The previous "candidates that will be evaluated at request time" framing was rejected as inconsistent with the base spec's `oslc_config:Selections.selects` semantics (which always names bound, chosen versions) and was the user's first substantive correction in the session. The reframe required rewriting Sections N.1 through N.9 and M.1 through M.9 of `specs/config/oslc-config-selections-extensions.md`.

2. **Context attachment via properties on Configuration.** New `oslc_config:effectivityContext` and `oslc_config:variabilityContext` properties on `oslc_config:Configuration` are now the **primary** mechanism for naming the contexts under which the configuration's selections were computed. A baseline carries both its frozen `selects` triples *and* its frozen context references — fully self-describing, no external lookup needed. A stream's context properties are mutable; modifying them re-triggers server-side computation. The request-time `Effectivity-Context` / `Variability-Context` header and `oslc_config.effectivity` / `oslc_config.variability` query parameter are retained as optional secondary mechanisms for transient per-request overrides on streams. The motivation came from the user: baselines must record *which* contexts produced the frozen `selects`, so the natural home is a property on the Configuration itself.

3. **Algorithm reframed as computation, not request-time resolution (N.5, M.5).** What was "Resolution Algorithm" is now "Computation of `EffectivitySelections.selects`" (and the parallel for variability). The procedure describes how a server *populates* the post-filter set from a candidate pool plus a context, with explicit triggers: create, context change, candidate-pool change, baseline freeze, transient request override. Baseline freeze is non-recomputable; stream materialization is refreshed on the listed triggers.

4. **Worked Example — Eco Line Washing Machine (Effectivity only).** Added to `plm-spec.html` as a new top-level informative section `#washing-machine-example` (originally placed nested under Effectivity and Variability; promoted to top-level after user feedback that it should serve the whole document). Product structure: `WashingMachine → {Motor, Housing → {UpperHousing, LowerHousing}, ControlUnit}` with `oslc_plm:Part` revisions and `oslc_plm:PartUsage` versions reifying parent→child usages and carrying date-based effectivity. A summary version in `specs/config/oslc-config-selections-extensions.md` references the PLM example and shows the resulting stream and baseline turtle. Variability example deliberately omitted at user direction — to be added in a future revision.

5. **Effectivity value-type enumeration.** `oslc_plm:Date`, `UnitNumber`, `SerialNumber`, `Lot`, `Model` were refactored from individual `rdfs:Class` declarations to **instances of a new `oslc_plm:EffectivityValueType` enumeration class**, per the OSLC Core enumeration pattern at <https://docs.oasis-open-projects.org/oslc-op/core/v3.0/os/core-vocab.html#enumerations>. The Effectivity shape's `:effectiveValueType` property now constrains via `oslc:range oslc_plm:EffectivityValueType` + `oslc:allowedValue` listing the five members — closed enumeration at the shape level while leaving the enumeration open in the vocabulary for future extensions.

6. **Shape rendering for the new effectivity/variability classes.** New `effectivityVariabilityConstraints` section in `pml-shapes.html` (file name has the existing typo — pml not plm — left unchanged to avoid breaking external references). The section includes `shapeToSpec` divs for the seven new resource shapes: `Effectivity`, `EffectivityContext`, `OptionSet`, `Option`, `OptionValue`, `VariabilityCondition`, `VariabilityContext`. Plus an `effectivityValueTypes` description of the enumeration. These existed in `plm-shapes.ttl` from the prior session but had no rendering hookup — addressed.

7. **New cross-domain common link type `oslc_am:realizes`.** Contributed to the OASIS-standard `oslc_am:` namespace by the OSLC PLM specification. The property is defined on `oslc_am:Resource` only; PLM resources (`Part`, `PartUsage`, `LogicalDesign`, `PhysicalDesign`) are *targets* of `realizes` assertions, not bearers. The canonical cross-domain use is a SysML `PartDefinition` (exposed as `oslc_am:Resource` via the OASIS OSLC SysML v2 vocabulary) realizing an `oslc_plm:Part`. New `:AmResourceShape` in `plm-shapes.ttl`; new `amResourceConstraints` peer subsection in `pml-shapes.html` under *Constraints on Other OSLC Domain Resources*, alongside the existing `changeRequestConstraints` and `testCaseConstraints` sections. Namespace deviation flagged in spec prose: existing common link types remain in legacy `jazz_am:` namespace; the OASIS-OP TC may choose to migrate them to `oslc_am:` for consistency as a separate decision.

8. **SysML v2 prior-art section.** Added to `specs/config/oslc-config-selections-extensions-plm-mapping.md` (the informative companion doc) covering the meaningful differences between SysML v2 `PartDefinition`/`PartUsage` (KerML Classifier/Feature) and OSLC PLM `Part`/`PartUsage`: definition/usage separation, typing/multiplicity/composition gaps, intensional (SysML v2) vs extensional (OSLC PLM) variation models, versioning layers. **Explicitly framed as terminology-overlap clarification, not as an alignment roadmap** — at the user's direction, the section disavows any current business case for SysML v2 ↔ OSLC PLM data-model convergence and notes the absence of interest from either community. Cross-domain *linking* (via the existing AM common link types plus the new `realizes`) is presented as the integration surface, distinct from data-model alignment.

### Decisions captured in this session

- `selects` = post-filter survivors. Not candidates.
- Context binding is a property on Configuration, not on each `*Selections` resource.
- A baseline freezes both `selects` and its context references; a stream is server-maintained.
- Request-time header/query mechanism is optional and only meaningful for streams (transient overrides).
- Variability-before-effectivity computation order remains normative (M.7).
- `LogicalDesign` and `PhysicalDesign` should be removed (filed as Option A in issue #638; see below).
- SysML v2 alignment is **not** an active project. OSLC AM common link types (and the new `realizes`) suffice as the cross-domain assertion surface.
- `oslc_am:realizes` is on AM resources only, not on PLM resources. PLM resources are targets, not bearers.

### Open questions and follow-ups raised

**Filed as GitHub issues:**
- **#638 — Review LogicalDesign / PhysicalDesign — origin, framing, and whether to remove.** Research found the names are not Teamcenter-canonical; the closest Teamcenter concept (ARCADIA Logical/Physical Architecture via SMW) is an architecture viewpoint, not a Part subtype, and the `rdfs:subClassOf oslc_plm:Part` framing is not supported by Siemens source documentation. The user's clarification — that multi-view BOMs are already expressible via named Configurations with `EffectivitySelections`, no `viewType` needed — strengthens the case for removal. Recommendation in the issue is Option A (Remove). Label: `Domain: PLM`. URL: <https://github.com/oslc-op/oslc-specs/issues/638>.

**In the CM Selections Extensions doc's Open Questions section:**
- Resolution-order normativity (variability-before-effectivity) — unchanged from prior session.
- **New Q2 (replacing the old "context as URI vs inline" question):** *Is the request-time header/query mechanism needed at all, given the property approach covers baselines and streams?* — left for TC discussion.
- Inline vs resource-by-reference contexts (if the request-time mechanism is retained).
- Predicate validation hooks for authoring-time checks.
- `oslc_config:authorizedBy` placement.
- Override / pinning semantics (Teamcenter's `Override` / `Precise`).

### Files touched in this session

```
specs/config/oslc-config-selections-extensions.md          # post-filter rewrite, N.1–N.9, M.1–M.9, worked example, open questions
specs/config/oslc-config-selections-extensions-plm-mapping.md  # SysML v2 prior-art section, AM linking subsection
specs/plm/plm-spec.html                                    # washing-machine top-level worked example
specs/plm/plm-vocab.ttl                                    # EffectivityValueType enumeration refactor
specs/plm/plm-shapes.ttl                                   # :realizes property shape, :AmResourceShape, effectiveValueType constraint
specs/plm/pml-shapes.html                                  # effectivityVariabilityConstraints section, amResourceConstraints peer
notes/2026-05-20-config-and-plm-session-handoff.md         # this file (originally untracked; now committed)
```

### Suggested next steps

1. **Act on issue #638** — remove `LogicalDesign`, `PhysicalDesign`, `realizesLogicalDesign` from vocab, shapes, and the spec overview (and update `PLM.svg`), if the TC agrees with Option A.
2. **Variability worked example** — parallel to the effectivity-only washing-machine example, exercise `VariabilitySelections` and `oslc_config:variabilityContext` against the same product structure with an option dimension.
3. **HTML conversion of the CM Selections Extensions doc** — the two `.md` files in `specs/config/` are still working-doc Markdown. At some point they fold into `oslc-config-mgt.html` as new sections or become a new HTML spec under the same directory.
4. **Companion `oslc-config-selections-shapes.ttl`** — no machine-readable shapes file exists yet for `EffectivitySelections` / `VariabilitySelections`. Adding one would parallel `config-shapes.ttl`.
5. **Conformance language pass** — MUST/SHOULD/MAY audit against existing OSLC-OP house style now that N/M sections have been substantially rewritten.
6. **SysML v2 prior-art section refinement** — the doc currently reads well as background, but a TC reviewer may push back on parts of the variation-model claims; the section deliberately stays general to avoid that, but a second pass with someone close to SysML v2 would be worthwhile if convenient.

### Confirming external references not changed in this session

The OSLC-Shape-Extensions proto-spec in `oslc4js` and its implementation plan (commits `3db85108` and `b22589d5`) were not touched this session — still parked pending OSLC-OP feedback on the conjunction-semantics design. The AAKI ontology plan is similarly unchanged. The work in this session is independent of `oslc:superShape` adoption; both can land separately.

---

## Session of 2026-05-25 (continued) — Structural refactor: Variability extracted, Effectivity moved into PLM

*Late in the same 2026-05-25 session, the user proposed and approved a substantial restructure motivated by the observation that variability is a general OSLC capability applicable to any resource — not a PLM-specific concept. The restructure also resolves the open Q2 from the prior section's Open Questions list (whether the request-time header/query mechanism is needed): it's been dropped entirely.*

### The architectural shape after the restructure

| Concept | Where it lives now | Namespace |
|---|---|---|
| `VariabilitySelections` (subclass of `oslc_config:Selections`) | `specs/core/oslc-variability-spec.html` (new) | `oslc:` |
| `OptionSet`, `Option`, `OptionValue`, `VariabilityCondition`, `VariabilityContext`, plus all their properties | same spec | `oslc:` |
| `oslc:variabilityContext` property on `oslc_config:Configuration` | same spec, as a Configuration extension | `oslc:` |
| `EffectivitySelections` (subclass of `oslc_config:Selections`) | `specs/plm/plm-spec.html` (moved from old extensions doc) | `oslc_plm:` |
| `oslc_plm:effectivityContext` property on `oslc_config:Configuration` | `plm-spec.html`, as a PLM-domain Configuration extension | `oslc_plm:` |
| `Effectivity`, `EffectivityContext`, `EffectivityValueType` enumeration | `plm-spec.html` (unchanged) | `oslc_plm:` |
| Request-time `Effectivity-Context` / `Variability-Context` headers and `?oslc_config.effectivity=` / `?oslc_config.variability=` query parameters | **removed** | — |

**Critical property of this restructure:** there are no changes to the OSLC Configuration Management base specification. Both new mechanisms are pure extensions — `*Selections` are subclasses of `oslc_config:Selections`, and the context properties are attached to `oslc_config:Configuration` as extension properties defined by the extending specs in their own namespaces.

### Files created

```
specs/core/oslc-variability-vocab.ttl        # new — variability vocabulary in oslc: namespace
specs/core/oslc-variability-shapes.ttl       # new — variability shape constraints
specs/core/oslc-variability-spec.html        # new — full OSLC-style spec, ReSpec-driven, with shapeToSpec includes for all the variability shapes
```

The variability spec follows the OSLC Core conventions (config.js, respec-oasis-common, etc.) but is its own document — not folded into the existing core-vocab/core-shapes — so the variability content is discoverable as a standalone capability.

### Files deleted

```
specs/config/oslc-config-selections-extensions.md   # deleted; effectivity content went to plm-spec.html, variability went to the new core/oslc-variability-spec.html
```

### Files renamed/moved

```
specs/config/oslc-config-selections-extensions-plm-mapping.md  →  specs/plm/existing-plm-mapping.md
```

The mapping doc is renamed to reflect its new role: a PLM-side mapping of three commercial systems to the OSLC PLM Effectivity model + OSLC Variability model, retitled "Existing PLM Product Conformance Mapping." The doc's intro and "Common Adapter Pattern" section were updated to reference the new spec homes; the SysML v2 prior-art section and the AM-linking subsection were retained as informative background. (Note: the body sections that map each vendor's effectivity / variability machinery to the OSLC concepts still reference the old class names — these are still substantively correct because the conceptual mapping is unchanged, only the namespace prefixes need a future cleanup pass.)

### Files heavily edited

- **`specs/plm/plm-vocab.ttl`** — variability classes/properties removed (OptionSet, Option, OptionValue, VariabilityCondition, VariabilityContext, hasOption, hasOptionValue, variabilityCondition, and, or, not, optionEquals, choice, appliesToOptionSet). New entries added: `oslc_plm:EffectivitySelections` (subclass of `oslc_config:Selections`) and `oslc_plm:effectivityContext` (property on Configuration). Added `oslc_config:` prefix declaration.
- **`specs/plm/plm-shapes.ttl`** — variability property shapes and resource shapes removed (`:variabilityCondition`, `:hasOption`, `:hasOptionValue`, `:vc-and`, `:vc-or`, `:vc-not`, `:optionEquals`, `:choice`, `:appliesToOptionSet`, `:OptionSetShape`, `:OptionShape`, `:OptionValueShape`, `:VariabilityConditionShape`, `:VariabilityContextShape`). New property shapes added: `:effectivityContext` and `:selects`. New resource shapes added: `:EffectivitySelectionsShape` and `:ConfigurationShape` (describing `oslc_config:Configuration` with the PLM `:effectivityContext` extension). `:variabilityCondition` reference removed from `:PartShape` and `:PartUsageShape` (and the LogicalDesign/PhysicalDesign shapes pending the #638 outcome). `:EffectivityContextShape` description updated to drop the request-time mechanism wording.
- **`specs/plm/plm-spec.html`** — the "Effectivity and Variability" parent section (line ~647) was renamed to just "Effectivity" with id `effectivity-resolution`. Variability subsection and Combined Resolution subsection deleted. New peer subsections added: `effectivity-selections` (covering `oslc_plm:EffectivitySelections` and `oslc_plm:effectivityContext`) and `effectivity-computation` (the computation algorithm including cross-Contribution boundaries). `effectivity-context` subsection updated to drop the request-time conveyance description and now references the new `effectivityContext` property. Conformance section updated to remove variability-related items and add new items for `EffectivitySelections`, `effectivityContext`, computation, and baseline-freeze. Overview text near the top updated to remove variability class names and point at the new variability spec under `specs/core/`. Worked example resolution text updated to use `oslc_plm:` namespace and shows a turtle snippet of the resulting `EffectivitySelections`.
- **`specs/plm/pml-shapes.html`** — `effectivityVariabilityConstraints` parent section renamed to `effectivityConstraintsParent` ("Effectivity Resources"). Variability sub-sections (`optionSetConstraints`, `optionConstraints`, `optionValueConstraints`, `variabilityConditionConstraints`, `variabilityContextConstraints`) removed. New sub-sections added: `effectivitySelectionsConstraints` rendering `:EffectivitySelectionsShape`, and `configurationConstraints` rendering `:ConfigurationShape` (the PLM extension). Intro paragraph rewritten to reflect the new scope. `effectivityValueTypes` enumeration section retained unchanged. The `amResourceConstraints` section under *Constraints on Other OSLC Domain Resources* is unchanged.

### Decisions captured in this restructure

- Variability is a general OSLC capability; not PLM-specific.
- Effectivity (with its date/unit/serial/lot/model semantics) is PLM-specific.
- `oslc_config:` namespace is not touched. Both new mechanisms are pure extensions.
- Namespaces used:
  - `oslc:` for variability (per the convention used by other OSLC Core specifications; namespaces are extensible)
  - `oslc_plm:` for effectivity
- Request-time header/query mechanism is removed — the Configuration-Context URL is the single entry point that carries all the information needed (selections, effectivity context, variability context) via the configuration's properties.
- Open Q2 from the earlier 2026-05-25 entry is resolved by this restructure: the header/query mechanism is dropped.

### Remaining open / cleanup items

- The handoff note's earlier `## Session of 2026-05-25 — Refinement progress` entry still describes the prior model (request-time mechanism, `oslc_config:EffectivitySelections`, etc.). It records what was true at the time of writing and is left intact as a history. This new section reflects the final state.
- The handoff note's "Files touched in this session" list in the prior entry is now partially superseded — the substantive locations are now those listed above.
- `existing-plm-mapping.md` body sections still use the old `oslc_config:EffectivitySelections` / `oslc_config:VariabilitySelections` class names in places. The conceptual mapping is unchanged; a future cleanup pass should re-namespace these mentions to `oslc_plm:EffectivitySelections` and `oslc:VariabilitySelections`.
- The Open Questions section of the prior `oslc-config-selections-extensions.md` is gone with the file. Two open questions migrated to the new variability spec (composition order, predicate validation, optional VariationPoint layer). PLM-side open questions did not have an obvious new home — worth deciding whether to surface them as TC issues or include in the PLM spec.
- Issue #638 on `LogicalDesign` / `PhysicalDesign` removal remains open and unaffected by this restructure.

### Suggested next steps after this restructure

1. Review the new file structure and the namespace choices, particularly that variability terms live in the `oslc:` Core namespace.
2. Decide what to do with the PLM-side open questions (authorization metadata, override semantics, etc.) that were in the deleted doc — they can be filed as TC issues, or moved into the PLM spec as informative open questions, or dropped.
3. Re-namespace the body of `specs/plm/existing-plm-mapping.md` from `oslc_config:EffectivitySelections` / `oslc_config:VariabilitySelections` to the new home namespaces, and update the document's section references accordingly.
4. Update or remove the prior `## Session of 2026-05-25 — Refinement progress` content if you'd rather the handoff note read as a single coherent state-of-play rather than a history of two iterations within one day.
