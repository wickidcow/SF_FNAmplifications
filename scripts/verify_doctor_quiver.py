#!/usr/bin/env python3
"""Verify FN Amplifications Doctor quiver reconciliation stays conservative."""

from pathlib import Path
import sys

ROOT = Path(sys.argv[1] if len(sys.argv) > 1 else ".").resolve()
ERRORS: list[str] = []


def read(path: str) -> str:
    file = ROOT / path
    if not file.is_file():
        ERRORS.append(f"missing required file: {path}")
        return ""
    return file.read_text(encoding="utf-8")


def require(value: bool, message: str) -> None:
    if not value:
        ERRORS.append(message)


def reject(value: bool, message: str) -> None:
    if value:
        ERRORS.append(message)


doctor = read("src/main/java/ne/fnfal113/fnamplifications/diagnostics/FNDoctor.java")
bridge = read("src/main/java/ne/fnfal113/fnamplifications/diagnostics/LegacyDoctorBridge.java")
plugin = read("src/main/java/ne/fnfal113/fnamplifications/FNAmplifications.java")
quiver_task = read("src/main/java/ne/fnfal113/fnamplifications/quivers/implementations/QuiverTask.java")

require("instanceof AbstractQuiver quiver" in doctor,
        "Doctor must scope reconciliation to registered FN quivers")
require("getLoadedChunks()" in doctor,
        "Doctor must remain loaded-chunk-only")
reject("loadChunk(" in doctor or "getChunkAt(" in doctor or "setForceLoaded(" in doctor,
       "Doctor must never load or force-load chunks")
require("MAX_NESTED_DEPTH = 4" in doctor,
        "nested container traversal must remain bounded")
require("!hasCount && !hasState && !hasUniqueId" in doctor,
        "fresh never-used quivers must remain valid and untouched")
require("if (!hasCount)" in doctor and "left untouched" in doctor,
        "missing authoritative arrow counts must fail closed")
require("count < 0 || count > quiver.getQuiverSize()" in doctor,
        "out-of-range arrow counts must be detected")
require("count was not changed" in doctor,
        "invalid arrow count diagnostics must state that the count is preserved")
reject("pdc.set(quiver.getStoredArrowsKey()" in doctor,
       "Doctor must never rewrite the authoritative stored-arrow count")
require("pdc.set(quiver.getStateKey(), PersistentDataType.STRING, expectedState)" in doctor,
        "Doctor may repair only derivable open/closed state")
require("pdc.set(quiver.getRandomIdKey(), PersistentDataType.INTEGER" in doctor,
        "Doctor may restore the non-empty quiver uniqueness marker")
require("ThreadLocalRandom.current().nextInt(1, 1_000_000)" in doctor,
        "Doctor uniqueness marker range must match current quiver behavior")
require("ThreadLocalRandom.current().nextInt(1, 1000000)" in quiver_task,
        "live quiver uniqueness behavior changed; review Doctor reconciliation")
reject("setItemData" in doctor or "registerLegacySlimefunItemId" in doctor,
       "quiver reconciliation must not rewrite Slimefun IDs")
require("AddonDoctor" in bridge and "AddonDoctorReport" in bridge,
        "Doctor bridge must remain optional/reflection-based")
require("Class.forName(DOCTOR_API" in bridge,
        "Doctor bridge must not hard-link the optional Legacy API")
require('case "getAddonName" -> "FN Amplifications"' in bridge,
        "Doctor provider name changed unexpectedly")
require("LegacyDoctorBridge.register(this);" in plugin,
        "Doctor bridge must register during plugin enable")
require("LegacyDoctorBridge.unregister(this);" in plugin,
        "Doctor bridge must unregister during plugin disable")

if ERRORS:
    print("FN Doctor quiver verification failed:")
    for error in ERRORS:
        print(" -", error)
    raise SystemExit(1)

print("FN Doctor quiver verification passed.")
