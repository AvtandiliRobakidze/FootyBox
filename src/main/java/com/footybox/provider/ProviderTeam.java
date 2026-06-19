package com.footybox.provider;

public record ProviderTeam(
        String externalId,
        String name,
        String shortName,
        String tla,
        String crestUrl
) {
}
