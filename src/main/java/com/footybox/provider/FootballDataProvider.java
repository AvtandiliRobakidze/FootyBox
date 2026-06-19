package com.footybox.provider;

import java.util.List;

public interface FootballDataProvider {
    String providerName();

    List<ProviderMatch> fetchMatches(String competitionCode, int seasonYear);
}
