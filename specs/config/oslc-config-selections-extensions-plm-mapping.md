# PLM Product Conformance Mapping for OSLC Configuration Management Selections Extensions

*This document is informative. It is a companion to the normative `oslc-config-selections-extensions.md` and the Effectivity/Variability sections of `plm-spec.html`.*

## Scope and Purpose

This document maps the abstract resources, predicates, and resolution algorithm defined by the OSLC Configuration Management Selections Extensions and the OSLC PLM specification onto the configuration-management concepts of three widely-deployed commercial PLM systems:

- **PTC Windchill** (12.x / 13.x)
- **Siemens Teamcenter** (Active Workspace / 14.x)
- **Aras Innovator** (current releases, including Configurator Services / Variant Management)

The purpose is to demonstrate to the OSLC-OP Configuration Management TC that the proposed extensions can be implemented as an adapter or native server against each of these products without doing violence to the product's data model. Where a clean mapping exists, this document records it. Where the spec or the product would need adjustment, the gap is named explicitly so the TC can decide whether to refine the spec or accept that adapter authors handle it.

This document is *not* a Project Note in the OASIS sense and carries no normative weight; it is reviewer support material.

## The Common Adapter Pattern

All three products share a structural pattern that maps cleanly to the two-step resolution defined in the extensions:

1. **Master record** — `oslc_plm:Part` (concept resource URI per the OSLC CM base spec): the identity layer.
2. **Revisions / iterations** — `oslc_plm:Part` version resources: the versioned content layer.
3. **BOM line / occurrence** — `oslc_plm:PartUsage` version resources: the relationship layer (reified).
4. **A filter pass** that consumes (a) a configuration / change-state, (b) variant option choices, (c) effectivity parameters, and yields a single revision of each in-scope part and the surviving usage links.

The OSLC CM extensions describe this filter pass in resource-oriented terms: a configuration that contains `EffectivitySelections` and/or `VariabilitySelections` is resolved by walking the selections, computing candidates via Section 12, then evaluating predicates carried by the candidate versions against per-request contexts. The product-specific work below shows how each vendor's existing model lands on that pattern.

---

## PTC Windchill

### Data-model translation

| OSLC concept | Windchill concept | Notes |
|---|---|---|
| `oslc_plm:Part` (concept) | `WTPartMaster` | The identity layer keyed by part number. |
| `oslc_plm:Part` (version) | `WTPart` (iteration of a revision) | Windchill distinguishes revisions and iterations; OSLC versions correspond to iterations selected by a ConfigSpec. |
| `oslc_plm:PartUsage` (concept) | `WTPartUsageLink` between a parent `WTPart` and a child `WTPartMaster` | Windchill's usage links point at the master; child revision is selected by ConfigSpec at navigation time. |
| `oslc_plm:PartUsage` (version) | An effective iteration of a `WTPartUsageLink` | Usage links iterate with their parent revision; an adapter exposes each iteration as a PartUsage version. |
| `oslc_plm:Effectivity` | A `WTDatedEffectivity`, `ProductLotNumberEffectivity`, `ProductBlockEffectivity`, `ProductSerialNumberEffectivity`, or `WTSerialNumberedEffectivity` attached to a `WTPart` via `EffectivityHelper.setEffectivityTarget` | One Windchill effectivity object → one OSLC Effectivity record. |
| `oslc_plm:effectiveForEndItem` | The `EffContext` field on the Windchill effectivity record (the product instance / end item) | First-class in Windchill. |
| `oslc_plm:EffectivityContext` | The `(end item, value)` pair supplied to an Effectivity ConfigSpec at navigation time | No standing resource in Windchill — typically constructed transiently per request. An adapter exposes it as an addressable resource. |
| `oslc_plm:OptionSet` / `Option` / `OptionValue` | Windchill Option Sets, Options, Choices | The terminology aligns directly. |
| `oslc_plm:VariabilityCondition` | An assigned expression on a configurable module evaluated by the Option Filter | Windchill's expression syntax is product-specific; an adapter renders it as a `oslc_plm:VariabilityCondition` AST. |
| `oslc_plm:VariabilityContext` | An option-choice set supplied to the Option Filter | Like EffectivityContext, transient in Windchill — exposed as an addressable resource by the adapter. |

### Effectivity mapping

Windchill carries effectivity directly on the `WTPart` revision, with one effectivity object per `EffContext`. The mapping is straightforward:

- `oslc_plm:effectiveValueType` is set per the effectivity object's Java class:
  `WTDatedEffectivity` → `oslc_plm:Date`,
  `ProductLotNumberEffectivity` → `oslc_plm:Lot`,
  `ProductBlockEffectivity` → `oslc_plm:Lot` (Block is treated as Lot in OSLC for v1 — see Gaps below),
  `WTSerialNumberedEffectivity` / `ProductSerialNumberEffectivity` → `oslc_plm:SerialNumber` or `oslc_plm:UnitNumber` depending on the part's trace code.
- `oslc_plm:effectiveForEndItem` = the Windchill `EffContext` (the product instance — typically a `WTProductInstance2` or the top `WTPart`).
- `oslc_plm:effectiveFrom` / `oslc_plm:effectiveTo` from the start/end fields on the Windchill effectivity record. Open-ended ranges (Windchill admits `00001–`, etc.) map to the absence-means-open semantics in the OSLC shape.
- `oslc_plm:authorizedBy` = the authorizing Change Notice on the Windchill record (`PendingEffectivity` → `ActualEffectivity` audit linkage). This is the only widely-deployed PLM system with this metadata standardized; adapters in the other two systems will commonly leave `authorizedBy` absent.

Multiple Windchill effectivity records on one part revision (different EffContexts, or different ranges) map to multiple `oslc_plm:Effectivity` resources referenced from `oslc_plm:effectivity` on the version. The OR semantics of the OSLC spec match Windchill's evaluation behavior.

#### Release effectivity / supersede

Windchill's supersedes/supersededBy relationship between two revisions records the audit trail, but the actual revision selection in a configured BOM is performed by an Effectivity ConfigSpec that, given an `(EffContext, type, value)`, returns the revision whose effectivity range includes the value. This maps to `oslc_plm:effectiveReplacement` on the older revision: when a context falls within the older record's range AND a replacement is named, the resolver substitutes the named revision.

In adapter implementations, the supersede link in Windchill is typically expressed as:
- The older revision carries an `oslc_plm:Effectivity` record covering its valid range with `effectiveReplacement` pointing at the newer revision when the context is outside that range. (Note: Windchill's own model doesn't have a "replace if context outside this record" semantics; it resolves by finding *any* covering record. The adapter has equivalent expressive power by emitting an `Effectivity` record on the *newer* revision covering its range. This is the simpler and more direct mapping.)

In other words: the OSLC `Effectivity` records form a partition of the context space across revisions of one master; resolution picks the revision whose record covers the supplied context. This is exactly how Windchill's `EffConfigSpecGroup` evaluates.

### Variability mapping

Windchill's variant-management subsystem (Product Family / Configurable Modules / Option Filter) maps to the OSLC variability model:

- A Windchill **Option Set** → `oslc_plm:OptionSet`.
- A Windchill **Option** → `oslc_plm:Option`.
- A Windchill **Choice** → `oslc_plm:OptionValue`.
- An **assigned expression** on a configurable module → `oslc_plm:VariabilityCondition` rendered as an AST of `and`/`or`/`not`/`optionEquals`.

Windchill applies the Option Filter *first* (before the ConfigSpec / Effectivity ConfigSpec runs). The OSLC CM extensions specify variability-before-effectivity resolution, which matches Windchill's order.

Windchill assigned expressions admit ranges and other constructs an adapter would need to lower to `optionEquals` terminals. Most production assignments resolve to flat conjunctions of equality terms, so the AST mapping is usually one-to-one. Rare cases involving Windchill's date-scoped option choices may require the adapter to combine variability and effectivity contexts.

### Resolution algorithm mapping

```
OSLC CM extensions algorithm:        Windchill equivalent:
─────────────────────────────────    ──────────────────────────────────────
Step 1 — Candidate version           ConfigSpec resolves the candidate
   resolution (Section 12)           revision for each Master reached by
                                     navigation. For the BOM case, the
                                     adapter walks WTPartUsageLink iterations
                                     and returns the relevant PartUsage
                                     versions.

Step 2a — Variability binding        Windchill Option Filter, evaluating
                                     assigned expressions on configurable
                                     modules against the supplied
                                     option-choice set.

Step 2b — Effectivity binding        Windchill Effectivity ConfigSpec
                                     (EffConfigSpecGroup), evaluating
                                     (EffContext, type, value) against
                                     the revisions' effectivity records.

Step 3 — Final resolution            Returns the configured BOM (or 404 for
                                     concept URIs with no surviving binding).
```

### Conformance checklist for a Windchill adapter

To claim conformance to the Configuration Management Selections Extensions and the PLM Effectivity/Variability sections, a Windchill OSLC adapter would:

1. Expose `WTPart` revisions as `oslc_plm:Part` version resources with `oslc_plm:effectivity` populated from the part's Windchill effectivity records.
2. Expose `WTPartUsageLink` iterations as `oslc_plm:PartUsage` version resources, with their own `effectivity` and `variabilityCondition` properties as authored in the configurable structure.
3. Accept `oslc_plm:EffectivityContext` resources at URIs and translate them to Windchill `EffContext`-keyed effectivity criteria.
4. Accept `oslc_plm:VariabilityContext` resources at URIs and translate them to option-choice sets for the Option Filter.
5. Honor `?oslc_config.effectivity=` and `?oslc_config.variability=` query parameters (and the corresponding headers) on GET requests against resources in a configured context.
6. Apply variability-before-effectivity ordering, returning the resolved BOM as a set of bound `Part` and `PartUsage` versions.
7. Populate `oslc_plm:authorizedBy` from the authorizing Change Notice on each effectivity record — Windchill is the natural source for this metadata and adapters SHOULD emit it.

### Known gaps and recommendations

1. **Block effectivity.** Windchill distinguishes Block from Lot; this proposal currently has only `oslc_plm:Lot`. An adapter folds Block into Lot, which loses fidelity. A future revision should add `oslc_plm:Block` to the value-type identifiers.
2. **MSN effectivity.** Windchill's Model Serial Number (model-qualified serial) doesn't have a direct equivalent. An adapter can carry the model via `oslc_plm:effectivityModel` and the serial via `oslc_plm:effectivitySerialNumber`, but the cross-product semantics (MSN as a *single* keyed identifier) is implicit.
3. **Trace code on the master.** Windchill restricts which effectivity types a part can carry. The OSLC PLM spec doesn't express this constraint; adapters may surface attempted combinations that Windchill would reject. This is a feature the OSLC-OP TC could consider as a future addition (`oslc_plm:effectivityTraceCode` on a Part master).
4. **Custom ConfigSpec extensions.** Windchill admits custom Java ConfigSpec classes that go beyond range matching. Predicates expressible only via custom ConfigSpecs may not have a clean OSLC representation. An adapter SHOULD report a 501 Not Implemented on attempted requests against such structures and document the limitation.

---

## Siemens Teamcenter

### Data-model translation

| OSLC concept | Teamcenter concept | Notes |
|---|---|---|
| `oslc_plm:Part` (concept) | `Item` (with `item_id`) | The identity layer. |
| `oslc_plm:Part` (version) | `ItemRevision` | Direct mapping. |
| `oslc_plm:PartUsage` (concept) | `ChildElement` / occurrence within a `BOMViewRevision` | Teamcenter has explicit occurrence objects. |
| `oslc_plm:PartUsage` (version) | An occurrence in a specific `BOMViewRevision` | One BOMViewRevision iteration = one usage version. |
| `oslc_plm:Effectivity` | Effectivity object on a `ReleaseStatus` of an ItemRevision (*revision effectivity*) OR on an occurrence (*occurrence effectivity*) | Teamcenter has two attachment points; PLM spec covers both naturally through Part and PartUsage versions. |
| `oslc_plm:effectiveForEndItem` | The End Item / Configuration Item bound to the Effectivity object | First-class in Teamcenter. |
| `oslc_plm:EffectivityContext` | The Effectivity Cursor on a Revision Rule plus a Variant Rule | Compound in Teamcenter; the adapter exposes the cursor as a single context resource. |
| `oslc_plm:OptionSet` | An Option Set in Modular Variants | Direct mapping. |
| `oslc_plm:Option` / `OptionValue` | Option family / value in Modular Variants | Direct mapping. |
| `oslc_plm:VariabilityCondition` | A Variant Condition expressed in MVL (Modular Variant Language) | The MVL boolean expression maps to the AST of `and`/`or`/`not`/`optionEquals`. |
| `oslc_plm:VariabilityContext` | A Variant Rule (a complete assignment over the Option Set) | Direct mapping. |

### Effectivity mapping

Teamcenter's two effectivity attachment points map naturally to the OSLC PLM model:

- **Revision effectivity** (Effectivity on `ReleaseStatus` of an `ItemRevision`) → `oslc_plm:Effectivity` carried by the OSLC `Part` version. Use `effectiveReplacement` when an effective range hands off to a sibling revision.
- **Occurrence effectivity** (Effectivity on the BOM child element) → `oslc_plm:Effectivity` carried by the OSLC `PartUsage` version. This handles the "is this child in this BOM under this date / unit?" question without needing a separate placement.

Teamcenter Effectivity Groups (named, reusable, multi-tuple effectivity objects) map to multiple `oslc_plm:Effectivity` resources on a single version, where each resource captures one `(end_item, range)` tuple from the group. The OR semantics of multiple OSLC records reproduce the OR semantics of the Effectivity Group's tuples. Where the Effectivity Group is shared across many occurrences in Teamcenter, the OSLC adapter MAY expose it as a single `oslc_plm:Effectivity` resource and reference it from each version's `oslc_plm:effectivity` link (the resource is the same; only the references differ).

#### Lifecycle-state-bound effectivity

Teamcenter's effectivity sits on a `ReleaseStatus`, and a single ItemRevision may have multiple ReleaseStatuses (Prototype, Pre-production, Production) each with its own effectivity. The OSLC PLM spec doesn't currently distinguish these — all records OR together on the version. Teamcenter adapters MAY emit one OSLC `Effectivity` per ReleaseStatus, with the lifecycle state captured in `dcterms:title` (e.g., `"Production effectivity"`) for diagnosis. A future spec revision could add `oslc_plm:lifecycleState` on `Effectivity` to make this distinction first-class.

### Variability mapping

Teamcenter Modular Variant Language (MVL) is a direct expression-tree representation that lowers cleanly to OSLC `VariabilityCondition`:

```mvl
Engine = V8 & Market = EU
```

becomes

```turtle
[ a oslc_plm:VariabilityCondition ;
  oslc_plm:and (
    [ a oslc_plm:VariabilityCondition ;
      oslc_plm:optionEquals ( tc:Option/Engine tc:OV/Engine-V8 ) ]
    [ a oslc_plm:VariabilityCondition ;
      oslc_plm:optionEquals ( tc:Option/Market tc:OV/Market-EU ) ]
  ) ]
```

MVL admits `|` (OR), `!` (NOT), parentheses, and multi-value equality (`Engine = V6,V8`). All four constructs lower to the OSLC AST cleanly. Classic Variants (top-level option families) are a special case of the same model and need no separate treatment.

### Resolution algorithm mapping

Teamcenter resolution combines a **Revision Rule** (which selects a single ItemRevision per Item from an ordered clause list) and a **Variant Rule** (which selects which occurrences participate). The OSLC two-step resolution maps as:

- Step 1 (Candidate version resolution) corresponds to Teamcenter's Revision Rule selecting an ItemRevision per Item. For a BOM, this includes occurrence selection from the BOMViewRevision.
- Step 2a (Variability binding) is performed by the Teamcenter Variant Rule application.
- Step 2b (Effectivity binding) is performed by the Teamcenter Effectivity Cursor on the Revision Rule.

Note that Teamcenter doesn't have a literal "substitute the candidate with a sibling revision" step — substitution emerges because the Revision Rule retries the next-older revision when the current one's effectivity range doesn't cover the cursor. Functionally identical to the OSLC `effectiveReplacement` outcome.

#### Nested effectivity at Configuration Item boundaries

Teamcenter swaps the end-item / unit-namespace at Configuration Item boundaries when traversing a multi-supplier structure. The OSLC `EffectivityContext` is flat. An adapter has two options:

1. Compute the full traversal server-side and emit only the resolved BOM (the recommended approach — the adapter implements the namespace switching internally).
2. Surface multiple `EffectivitySelections` resources in a nested-context configuration, with each Selection scoped to a different end-item namespace.

Option 1 is cleaner for client interoperability. Option 2 may be useful for tooling that needs to introspect the nesting; the OSLC-OP TC could consider whether to standardize a nested-context mechanism in a future revision.

### Conformance checklist for a Teamcenter adapter

1. Expose `ItemRevision` as `oslc_plm:Part` versions, with `oslc_plm:effectivity` populated from the revision's `ReleaseStatus` effectivities.
2. Expose BOM occurrences as `oslc_plm:PartUsage` versions, with `oslc_plm:effectivity` populated from occurrence effectivities and `oslc_plm:variabilityCondition` populated from MVL Variant Conditions.
3. Accept `oslc_plm:EffectivityContext` resources and translate to a Revision Rule's Effectivity Cursor plus End Item.
4. Accept `oslc_plm:VariabilityContext` resources and translate to a Variant Rule application.
5. Honor `?oslc_config.effectivity=` and `?oslc_config.variability=` (and headers).
6. Apply variability-before-effectivity ordering.
7. Compute nested-effectivity namespace switching server-side; do not require clients to assemble per-CI contexts unless the adapter explicitly supports the nested-selections pattern.

### Known gaps and recommendations

1. **Override / Precise rule clauses.** Teamcenter Revision Rules admit `Override` and `Precise` clauses that pin a specific revision regardless of effectivity. The OSLC predicate model doesn't have an explicit "pinned" outcome; a substitution-only Effectivity record (open-ended `effectiveFrom`/`effectiveTo`, with `effectiveReplacement` pointing at the pinned version) is the workaround. The OSLC-OP TC might consider an `oslc_plm:pinned` outcome explicitly.
2. **Variant Rule check-out / authoring.** Teamcenter Variant Rules can be authored, version-controlled, and approved. The OSLC `VariabilityContext` is a passive request-time parameter. Authoring workflows on `VariabilityContext` are not addressed in this proposal; an adapter might expose Variant Rules as versioned resources separate from their use as contexts.
3. **MVL `?` (unknown) values.** MVL admits a three-valued logic for incomplete configurations. The OSLC spec specifies three-valued logic for partial contexts, which matches; an adapter SHOULD ensure the lowering preserves the Kleene-style semantics Teamcenter uses.

---

## Aras Innovator

### Data-model translation

| OSLC concept | Aras concept | Notes |
|---|---|---|
| `oslc_plm:Part` (concept) | `Part` (master record) | Aras's `Part` is the master; versions are revisions. |
| `oslc_plm:Part` (version) | `Part` revision | Aras versions parts via the Lifecycle / Fix mechanism. |
| `oslc_plm:PartUsage` (concept) | `Part BOM` relationship (parent-Part to child-Part) | Aras BOM rows are relationship resources; the OSLC PartUsage reification matches this directly. |
| `oslc_plm:PartUsage` (version) | A specific Part BOM row in a specific Part revision | Each parent revision has its own copy of the BOM rows. |
| `oslc_plm:Effectivity` | An Aras Effectivity Expression on a Part BOM row | Note the placement is on the *relationship row*, not on the Part revision. PartUsage reification covers this naturally. |
| `oslc_plm:effectiveForEndItem` | The Aras `Model` variable in the Effectivity Expression | Configurable, but conventionally named Model. |
| `oslc_plm:EffectivityContext` | An Aras Effectivity Criteria | An assignment to the Effectivity Variables passed to the Effectivity Resolution Engine. |
| `oslc_plm:OptionSet` | Aras Variant Management Feature Set (with Configurator Context) | Strong fit conceptually. |
| `oslc_plm:Option` | Feature | |
| `oslc_plm:OptionValue` | Option (Aras's terminology — confusing relative to OSLC) | The Aras name "Option" corresponds to an OSLC OptionValue. |
| `oslc_plm:VariabilityCondition` | A Configurator Rule expression over Features and Options | The Aras rule language is product-specific; an adapter renders it as an OSLC AST. |
| `oslc_plm:VariabilityContext` | A Configurator selection (the resolved Variability Item context) | |

### Effectivity mapping

The clean PartUsage-as-version mapping makes Aras's relationship-row effectivity placement essentially free in OSLC: each row exposed as a `PartUsage` version carries its own `oslc_plm:effectivity` link. The adapter walks the Aras Effectivity Expression and emits one `Effectivity` record per term:

```
Aras expression:
<EQ><variable id="MODEL"/><named-constant id="ITEM:M-100"/></EQ>
<EQ><variable id="UNIT"/><constant type="int">11</constant></EQ>
```

becomes:

```turtle
[ a oslc_plm:Effectivity ;
  oslc_plm:effectiveValueType oslc_plm:UnitNumber ;
  oslc_plm:effectiveForEndItem aras:Item/M-100 ;
  oslc_plm:effectiveFrom 11 ;
  oslc_plm:effectiveTo 11 ]
```

Range semantics in Aras are carried on the *criterion* side (the supplied context), not the expression. The OSLC model carries ranges on the predicate side (`effectiveFrom`/`effectiveTo`). The adapter is responsible for converting Aras's pointwise expressions plus client-side range criteria into the equivalent OSLC ranged predicate. In practice, expressions like `UNIT ≤ 10` are authored client-side in Aras and become explicit ranges in the OSLC view, which is cleaner.

#### No native "replace" outcome

Aras does not natively replace one BOM row with another via the Effectivity Resolution Engine — replacement is modeled as two separate rows with disjoint date/unit expressions, each evaluated independently. This maps trivially to the OSLC model: two distinct `PartUsage` versions, each with its own `Effectivity`, and the configuration's `EffectivitySelections` selects whichever survives. The `effectiveReplacement` outcome from the OSLC spec is not required for Aras conformance, though an adapter MAY use it as a convenience to collapse adjacent ranges in the surfaced data.

### Variability mapping

Aras's Variant Management is layered above Effectivity Services and is conceptually parallel. The Variant Management Feature/Option/Rule model lowers to OSLC's OptionSet/Option/OptionValue/VariabilityCondition with one caveat:

- **Naming collision.** Aras uses "Option" for what OSLC calls "OptionValue", and uses "Feature" for what OSLC calls "Option". The adapter performs a consistent translation. This is a vocabulary impedance mismatch only; the data model fits cleanly.

Aras's Configurator Rules can express more complex constraints than the OSLC `and`/`or`/`not`/`optionEquals` AST — including "if A then B" implications, cross-feature exclusions, and assertions over counts. The most common subset (boolean over equality terminals) lowers directly. Out-of-subset rules can be approximated by their boolean equivalents where possible (e.g., `A → B` ≡ `¬A ∨ B`) or surfaced as a 501 Not Implemented on configurations that require them.

### Resolution algorithm mapping

```
OSLC CM extensions:                  Aras equivalent:
─────────────────────────────────    ──────────────────────────────────────
Step 1 — Candidate version           Lifecycle / Fix mechanism + Aras
   resolution                        Query Definition execution to enumerate
                                     candidate BOM rows.

Step 2a — Variability binding        Configurator Services rule evaluation
                                     against the Variability Item context.

Step 2b — Effectivity binding        Effectivity Resolution Engine filtering
                                     against the supplied Effectivity
                                     Criteria.

Step 3 — Final resolution            The 100% resolved structure delivered
                                     by Aras's Query Definition Engine.
```

The variability-before-effectivity ordering matches Aras's documented Configurator-then-Effectivity layering.

### Conformance checklist for an Aras adapter

1. Expose Aras `Part` revisions as `oslc_plm:Part` versions.
2. Expose Aras Part BOM rows as `oslc_plm:PartUsage` versions, each with its own `oslc_plm:effectivity` from the row's Effectivity Expression and `oslc_plm:variabilityCondition` from the row's Variant rule reference.
3. Accept `oslc_plm:EffectivityContext` resources and translate to Aras Effectivity Criteria (one variable assignment per `effectivity*` property).
4. Accept `oslc_plm:VariabilityContext` resources and translate to a Configurator selection / Variability Item context.
5. Honor `?oslc_config.effectivity=` and `?oslc_config.variability=` (and headers).
6. Apply variability-before-effectivity ordering.
7. Translate Aras's "Option" terminology to OSLC `OptionValue` consistently in both serialization and link rendering, to avoid downstream confusion.

### Known gaps and recommendations

1. **Custom Effectivity Variables.** Aras admins can declare new variables (e.g., Plant, Region) of types Integer/Date/String/List/Item. The OSLC PLM `EffectivityContext` shape lists a fixed set of properties. An adapter SHOULD either (a) map custom variables to `oslc_plm:effectivityModel` or `effectivityLot` where the semantics fit, or (b) emit them as extension properties under a vendor namespace. A future spec revision could add an open-extensions pattern (e.g., `oslc_plm:effectivityParameter` with key/value pairs).
2. **Configurator Context (rule scoping).** Aras's recent Configurator Context scopes which rule sets apply per program era. The OSLC `VariabilityContext` doesn't currently include a rule-scope reference. An adapter MAY include it as an additional property; future spec work could add `oslc_plm:variabilityRuleScope`.
3. **Aras Supersede.** Aras's Supersede globally replaces one released Part with another and has known edge cases in BOM contexts (community-reported). The OSLC `effectiveReplacement` outcome is more disciplined; an Aras adapter MAY emit `effectiveReplacement` triples to express supersede with cleaner semantics than the native model allows.
4. **Lifecycle / Fix vs. OSLC versioning.** Aras's "Fix" lifecycle behavior locks a released Part to specific child revisions at release time; subsequent revisions of a child are not auto-pulled into the released parent. This is an important authoring-semantics difference from the OSLC base CM spec's "latest" stream model. Aras adapters SHOULD document which OSLC stream semantics map to which Aras lifecycle states.

---

## Cross-Product Summary

### Effectivity dimension coverage

| Effectivity dimension | OSLC value-type | Windchill | Teamcenter | Aras |
|---|---|---|---|---|
| Date | `oslc_plm:Date` | `WTDatedEffectivity` | Date effectivity | Date variable |
| Unit number | `oslc_plm:UnitNumber` | Serial/MSN (depending on trace code) | Unit effectivity (`(end_item, unit_range)`) | Unit variable |
| Serial number | `oslc_plm:SerialNumber` | `WTSerialNumberedEffectivity` | Effectivity Group tuple | Configurable variable |
| Lot | `oslc_plm:Lot` | `ProductLotNumberEffectivity` | Not first-class | Configurable variable |
| Model | `oslc_plm:Model` | Model code on configurable products | End Item / Configuration Item (acts as namespace, not value) | Model variable |
| Block (aerospace) | (gap — folds into Lot for v1) | `ProductBlockEffectivity` | Block via Effectivity Group | Configurable variable |
| End-item / namespace | `oslc_plm:effectiveForEndItem` | `EffContext` | End Item | Model variable (named-constant of Item type) |

### Variability dimension coverage

| Variability concept | OSLC | Windchill | Teamcenter | Aras |
|---|---|---|---|---|
| Option container | `OptionSet` | Option Set | Modular Variants Option Set | Feature Set |
| Option family | `Option` | Option | Option family | Feature |
| Option value | `OptionValue` | Choice | Option value | Option (Aras's term) |
| Boolean condition | `VariabilityCondition` (AST) | Assigned expression on configurable module | Variant Condition (MVL) | Configurator Rule |
| Context | `VariabilityContext` | Option Filter input | Variant Rule | Configurator selection |

### Resolution-order alignment

All three products support the variability-before-effectivity ordering specified in the extensions:

- Windchill: Option Filter → ConfigSpec → Effectivity ConfigSpec.
- Teamcenter: Variant Rule (independent) → Revision Rule with Effectivity Cursor.
- Aras: Configurator Services → Effectivity Services.

### Authorization metadata

Only Windchill standardizes change-authorization linkage on effectivity (Pending → Actual through Change Notices). Adapters for the other two products will commonly omit `oslc_plm:authorizedBy`. The OSLC-OP TC may wish to mark this property as MAY rather than SHOULD, with implementation-specific recommendations per product.

### Override / pinning

- Teamcenter: explicit (`Override` and `Precise` Revision Rule clauses).
- Windchill: implicit (custom ConfigSpec classes).
- Aras: implicit (Fix lifecycle locks child revisions; supersede globally replaces).

The OSLC `effectiveReplacement` covers the substitution case but not the "pin this regardless of context" case. Future spec work could add an explicit pinned outcome.

---

## Recommendations to the OSLC-OP Configuration Management TC

Based on the three-product mapping above, the TC may wish to consider:

1. **Add `oslc_plm:Block` to the effectivity value-type identifiers.** Block effectivity is standardized in aerospace PLM (and is a distinct concept from Lot in Windchill). Folding Block into Lot is a documented gap.
2. **Specify open-extension mechanism for Effectivity-Context.** Aras's customizable variables and similar features in other products suggest the fixed property list in `EffectivityContextShape` will be insufficient. An `oslc_plm:effectivityParameter` (key/value bag) with declared types would help adapters surface domain-specific variables.
3. **Standardize lifecycle-state-keyed effectivity.** Teamcenter's per-ReleaseStatus effectivity is a useful pattern that the spec currently underspecifies (records OR together regardless of state). Adding `oslc_plm:lifecycleState` on `Effectivity` would let adapters preserve the distinction.
4. **Consider an explicit pinned outcome.** Teamcenter's Precise and Override clauses are common authoring constructs without a clean OSLC equivalent.
5. **Make `authorizedBy` MAY rather than SHOULD.** Only Windchill has the metadata reliably; an aspirational SHOULD may prompt adapters to invent linkage that doesn't exist in the source system.
6. **Document the "Option vs. OptionValue" naming hazard.** Aras uses "Option" for OSLC's "OptionValue". An informative note in the PLM spec would prevent confusion.

Each of these is a small, well-scoped revision that would improve adapter ergonomics without changing the core resolution model. None is blocking for an initial OSLC-OP review.

## Source material

This document synthesizes findings from the research conducted during the design of the proposed extensions:

- **Windchill**: PTC Help Center articles on Effectivity (r12.0–r13.0), the Windchill REST EffectivityMgmt domain, the PTC eBook *Understanding Configuration Management in Windchill*, and PTC Community threads on Effectivity API, Custom ConfigSpecs, and Supersede.
- **Teamcenter**: Siemens Documentation on Revision Rules, Effectivity, Modular Variants, and Variant Rules; Saratech and Swoosh Technologies practitioner documentation; the `tcplmbasics` series on Revision/Nested Effectivity and Occurrence Effectivity.
- **Aras**: *Aras Innovator 31 — Effectivity Services Programmer's Guide* (D-008103), *Aras Innovator 29 — Configurator Services Programmer's Guide*, *Aras Variant Management 33 Administrator Guide* (D-007881), the *Demystifying Effectivity with Aras Innovator Version 12* blog, and the ArasLabs effectivity-sample-application reference repository.

Specific product-version coverage is current as of 2026 product releases; major-version changes in any of the three systems may require this mapping document to be revised.
