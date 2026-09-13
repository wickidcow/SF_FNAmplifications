package ne.fnfal113.fnamplifications.diagnostics;

import java.util.List;

/** Immutable result from FN Amplifications' addon-owned Doctor reconciliation pass. */
public record FNDoctorReport(
        long scannedEntries,
        long issuesFound,
        long repairedEntries,
        long failures,
        List<String> details) {

    public FNDoctorReport {
        details = List.copyOf(details);
    }
}
