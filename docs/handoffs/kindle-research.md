# Handoff: Kindle Android interaction research

Session completed on 2026-10-07 to the evidence available in the requested Sobre Palestina and Soccernomics editions. Read [the findings and explicit remaining limits](../kindle-interaction-research.md) before repeating research. Continue engine validation from [the Android prototype handoff](android-reader-prototype.md). The session accounted for every K question; unsupported fixtures and unmeasured transitions remain labelled, rather than asserted as observations.

## Task

Operate the Kindle Android app and document its reading interactions so reader-ldx can implement a familiar experience. This is an observation and specification task; app implementation comes afterward.

Read [the product brief](../product-brief.md), [the glossary](../../GLOSSARY.md), and [the Kindle interaction research queue](../kindle-interaction-research.md) before testing. The queue is the source of truth for all questions and user-selected baselines, including previously answered questions. Read [the engine findings](../engine-findings.md) when translating observations into prototype checks.

## Access and fixtures

Use a physical Android phone or an Android emulator with the actual Kindle Android app and an automation or computer-use connection. Record the available device-control method. Desktop, web, and e-reader editions do not establish Android behavior.

Establish app access and suitable test books before recording findings. Use dedicated test copies and test annotations. Prefer public-domain Spanish and English texts with note references so evidence can be shared in this public repository. Keep credentials, account screens, and commercial book files outside the repository.

If app access or a required fixture is unavailable, record the exact blocker and what is needed to continue. Leave those observations unobserved; documentation review is not a substitute for operating the app.

## Execution

1. Record the date, app/Android versions, device, screen dimensions, language, and reading settings in the research queue. List the fixture books and which scenarios each supports. This step is complete when the environment and test coverage are reproducible.
2. Investigate K04 first. Resolve slider activation, drag preview, jump grouping, return/forward controls, history depth, dismissal, and Android Back. Test at least two consecutive slider moves, intervening page turns, and background/relaunch. Compare chapter, search, and bookmark jumps separately. This step is complete when every K04 question has evidence or an explicit blocker.
3. Work through K01, K02, K03, K05, K06, and K07. Exercise Spanish/English selection, lookup failure/offline cases, short/long/nested notes, annotations, typography, and interruption scenarios. Every question needs an observation or a specific reason it could not be observed.
4. Append findings under their corresponding K section in the research queue. Separate observed Kindle behavior, the user's baseline, and proposed reader-ldx behavior. Add reproducible actions and relevant evidence. Preserve explicit user choices when Kindle differs; flag those differences for review.
5. Update the product brief with the formerly open interaction details that observations resolve, linking each to its K section. Record proposed changes to accepted behavior separately. Define new domain terms in the glossary only when their meaning is settled.
6. Return a concise handoff for the Android prototype: resolved controls and transitions, differences from the chosen baseline, blocked scenarios, and the Readium integration checks that remain. The task is complete when every K question is accounted for and document changes link to their evidence.

## Evidence

Store shareable screenshots or short recordings under `docs/research/kindle/<session-date>/`, with names such as `k04-two-slider-jumps.png`. Link them from the findings using relative paths. A written reproduction is required even when a recording exists.

For each finding, include the starting state, actions, observed result, and proposed behavior. Repeat ambiguous results and state uncertainty when fixture markup or app settings could explain a difference. Distinguish background/resume from process termination/relaunch.

## Deliverables

- Updated `docs/kindle-interaction-research.md` covering every question in K01 through K07.
- Shareable evidence with reproducible steps.
- Updates to `docs/product-brief.md` for resolved details, with observation links.
- A summary of remaining blockers and prototype acceptance checks.

Keep server login, hosting, sync conflict rules, and dictionary-format choices as already decided. Reading positions remain local to each device. A successful observation session does not establish that Readium supports the observed interactions.
