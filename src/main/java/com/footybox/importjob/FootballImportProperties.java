package com.footybox.importjob;

import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "footybox.import")
public class FootballImportProperties {

    private boolean enabled;
    private List<String> competitionCodes = new ArrayList<>(List.of(
            "WC", "CL", "BL1", "DED", "BSA", "PD",
            "FL1", "ELC", "PPL", "EC", "SA", "PL"
    ));
    private int fromYear = 2010;
    private int toYear = 2025;
    private boolean exitAfterRun;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public List<String> getCompetitionCodes() {
        return competitionCodes;
    }

    public void setCompetitionCodes(List<String> competitionCodes) {
        this.competitionCodes = competitionCodes;
    }

    public int getFromYear() {
        return fromYear;
    }

    public void setFromYear(int fromYear) {
        this.fromYear = fromYear;
    }

    public int getToYear() {
        return toYear;
    }

    public void setToYear(int toYear) {
        this.toYear = toYear;
    }

    public boolean isExitAfterRun() {
        return exitAfterRun;
    }

    public void setExitAfterRun(boolean exitAfterRun) {
        this.exitAfterRun = exitAfterRun;
    }
}
