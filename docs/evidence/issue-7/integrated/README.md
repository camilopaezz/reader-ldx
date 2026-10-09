# Integrated slider verification

Orchestrator installed the combined APK built from source integration `5447cf9`; evidence integration `788d010` changes documentation only. Build passed in23s. APK SHA256 `7b20f861c7bea04619db1d61aee52a46e59f6c4d6e39c4e54fc62bacdc6148b0`. Same pinned device/dependencies and authored CC0 Spanish fixture as the [slice report](../README.md). Existing database v2 and all four current books were preserved. No data clear or new import occurred.

Starting saved passage was chapter1 `#p-1-09`, progression .33343624575486264, Return chapter2 `#chapter-2`, progression0. The saved block was within the visible page, not necessarily its first line. [Start](start.png).

Center tap exposed controls. Return reached the chapter2 heading and swapped chapter1 `#p-1-09` into its target. [Visible result](return.png), [complete durable locators](integrated-return.json). Force-stop/relaunch retained both JSON locators byte-for-byte. [Stopped/relaunched state](integrated-after-restart.json), [settled screen](return-restart-settled.png), [runtime log](runtime.log). The earlier `return-restart.png` capture was taken during rendering and is not the settled outcome.

Return again reached chapter1 `#p-1-09` and restored chapter2 heading as target. A slider tap at the observed left-side track entered an uncommitted preview; Cancel restored the committed passage. Force-stop/relaunch preserved chapter1 `#p-1-09` and the chapter2 target. [Durable state](integrated-cancel.json), [restored page](cancel-restart.png). PASS for these integration samples at paragraph/block precision. This run does not independently repeat every slice scenario.

The required #7 slice checks have runtime evidence. Combined lookup/annotation/note/reflow checks remain for #8, and provisional policy still awaits owner review. The overall adoption gate remains failed.
