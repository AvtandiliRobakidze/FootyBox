package com.footybox.football;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class FootballArchiveServiceTest {

    @Mock
    private FootballMatchRepository matches;

    @InjectMocks
    private FootballArchiveService service;

    @Test
    void emptyQueryUsesServerSideNewestFilter() {
        PageRequest page = PageRequest.of(0, 50);
        Instant from = Instant.parse("1900-01-01T00:00:00Z");
        Instant to = Instant.parse("2101-01-01T00:00:00Z");
        when(matches.filteredNewest("", "", from, to, page)).thenReturn(List.of());

        service.listMatches("  ", false);

        verify(matches).filteredNewest("", "", from, to, page);
        verifyNoMoreInteractions(matches);
    }

    @Test
    void combinedFiltersAreAppliedBeforeResultLimit() {
        PageRequest page = PageRequest.of(0, 50);
        Instant from = Instant.parse("2020-01-01T00:00:00Z");
        Instant to = Instant.parse("2030-01-01T00:00:00Z");
        when(matches.filteredOldest("Argentina", "WC", from, to, page)).thenReturn(List.of());

        service.listMatches(" Argentina ", true, " WC ", 2020, "oldest");

        verify(matches).filteredOldest("Argentina", "WC", from, to, page);
        verifyNoMoreInteractions(matches);
    }

    @Test
    void invalidSortIsRejected() {
        assertThatThrownBy(() -> service.listMatches(null, false, null, null, "random"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Sort must be");
        verifyNoMoreInteractions(matches);
    }
}
