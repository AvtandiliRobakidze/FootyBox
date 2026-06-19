package com.footybox.search;

public record SearchResultResponse(
        String type,
        Long id,
        String title,
        String subtitle
) {
}
