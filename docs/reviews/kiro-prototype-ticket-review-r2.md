# Kiro Opus 5.5 review, round 2

Reviewer: Kiro, Claude Opus 5.5, medium effort. Document review only; APIs and runtime behavior were not independently verified. The findings below describe the seven-ticket draft before the round-2 wording fixes.

**Verdict: revise, but only with small wording changes.** The structure, dependency graph and ticket sizes are sound. Make the three fixes below and the draft can be published without another review round. All six prior blockers are resolved or nearly resolved. What remains is wording that would let an agent claim the gate passed, plus one ownership seam left over from blocker 4.

I read the revised ticket file, the spec (its only uncommitted change is the status line), my round-1 review, GLOSSARY.md, ADRs 0003 and 0005, and the tracker and triage-label docs. I did not check any Readium or StarDict API.

## Status of prior blockers

| # | Blocker | Status | Evidence |
|---|---|---|---|
| 1 | No owner for combined validation or the gate decision | Resolved | 07 owns the rerun mapped to evidence, the warm vs force-stop matrix, one Back policy, real bookmark/list no-history checks, owner review and the recommendation. It is blocked by 02, 03, 05 and 06, and by 04 through 05. |
| 2 | 01 oversized and speculative | Resolved | 01 exposes only selection, location, typography and commit. The manifest has source, licence and anchors. Notes say "Tickets 03 and 04 add decoration and reference interception." It is still the largest ticket, but that is normal for a walking skeleton and it fits one context. |
| 3 | Committed position coupled to the engine location | Resolved | 01: "The app owns commitment of reading position", plus the observable diagnostic non-committing move followed by relaunch. 04 and 05 both re-check the committed position. |
| 4 | Selection-menu ownership | Partial | 01 owns a minimal menu, which fixes the parallel rewrite. But 02 says "Extend the existing selection menu with lookup **and passage actions**", while highlight and annotation note belong to 03. 02 also changes the hold gesture itself ("Holding a word immediately opens… lookup"). Nothing says the 03 actions must stay reachable after hold opens lookup. |
| 5 | Morphology criteria | Resolved | Declared base-form source, `afecten`→`afectar` plus a plural, each either matched or recorded as a gap. No-neighbor rule, a known-absent token for the chosen package, language identity labelled as a hypothesis, contractions only in 01, app vs engine note. |
| 6 | Notes split and classification | Resolved | 04 and 05 are split. 05 has a classification matrix with an expected outcome per case, and the backlink is explicitly a hypothesis reviewed in 07. |

## Remaining blocking objections

**A. The gate result can still be blurred with the recommendation.** This is the main false-pass risk. 02 says app limitations "do not alone establish a Readium failure", and 07 says "Separate app-owned dictionary/extraction gaps from engine blockers". An agent could read these together as permission to file an app-owned gap as "not a blocker" and report "retain Readium" with the gate effectively passed. Two cases show this is realistic:
- Both base-form cases end as "unsupported gap". The spec check "a supported base-form entry" is then never demonstrated.
- Generic endnote extraction fails.

The spec treats "retain with explicit limitations" as a recommendation, not as a gate status. Fixes:
- 07, replace the last criterion with: "Report gate status (passed/failed) separately from the recommendation. Any spec check that is failed or not exercised, whether app-owned or engine-owned, keeps the gate failed. 'Retain with explicit limitations' must list those checks. Ticket completion does not pass the gate."
- 02, change the implementation note to: "Their limitations do not alone establish a Readium failure, but they remain failed or not-exercised spec checks in 07's report."

**B. Finish the menu seam (blocker 4).**
- 02: replace "with lookup and passage actions" with "with the lookup entry. Hold opens lookup, and lookup must keep the 01 selection menu reachable for passage actions owned by other tickets."
- 03: "Extend the existing selection menu with highlight, annotation-note and bookmark actions. These remain reachable from the lookup presentation when 02 is present."

That second sentence creates a soft link between 02 and 03. It does not need a blocker edge, because 07 already checks "menu ownership" conflicts.

**C. 07 needs the human, so the label and completion rule must say so.** An agent cannot complete "Obtain the project owner's review…" alone, and under the triage labels a `ready-for-agent` 07 would stall or get faked. Either label 07 `ready-for-human`, or reword the criterion as: "Present the provisional choices to the project owner with evidence. Record each decision as accepted, changed or deferred. The ticket stays open until the owner responds." Also bound the open-ended "Resolve … conflicts" with: "Fix narrow conflicts. File larger ones as follow-up tickets and mark the affected checks failed."

## Prior recommendations I would withdraw or soften

- In round 1 I asked to "Define what tapping a backlink should do." That would have invented a requirement. Your version, an explicit hypothesis reviewed in 07, is the right answer.
- Listing 04 explicitly as a blocker of 07 is unnecessary, because 05 can't complete before 04. Adding it is cosmetic, if it makes the tracker easier to read.

## Optional improvements

- **04:** add one non-regression check: "An ordinary internal link still navigates normally after reference interception." This way 04 doesn't depend on 05's matrix to catch broken links.
- **02:** "malformed packages" has no expected outcome. Add: "is rejected with a visible error, without a crash or partial import."
- **02:** "Exact matches take precedence" is only testable with a token that is both a headword and an inflection of another word. Add one if the package has such a token; otherwise mark the check not exercised.
- **06:** say whether a committed slider jump becomes the committed reading position immediately. It is implied but not stated.

## Unverified API assumptions

These come from documents only, and I have not verified any of them:
- Readium Kotlin's selection, decoration and hyperlink-interception surface. ADR 0003 calls the hyperlink API experimental.
- Fragment-based navigator hosting inside Compose.
- Whether a selection across a page boundary is possible in paginated mode. This is plausibly limited, and 03 correctly allows reporting it as a limitation.
- Whether StarDict `.ifo` metadata carries any language identity. I'm not aware of a standard field, which is one more reason to keep the user-assigned language hypothesis.

## Dependencies

No changes are needed: 01 → {02, 03, 04, 06}, 04 → 05, and {02, 03, 05, 06} → 07. The graph is accurate and has no hidden blocking edges, apart from the 02/03 menu overlap that fix B handles in wording.

