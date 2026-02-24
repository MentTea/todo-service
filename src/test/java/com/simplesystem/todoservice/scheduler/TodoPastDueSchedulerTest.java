package com.simplesystem.todoservice.scheduler;

import com.simplesystem.todoservice.service.TodoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TodoPastDueSchedulerTest {

    private final TodoService todoService = Mockito.mock(TodoService.class, RETURNS_DEEP_STUBS);

    private Clock clock;
    private TodoPastDueScheduler scheduler;

    @BeforeEach
    void setUp() {
        clock = Clock.fixed(Instant.parse("2025-01-01T10:00:00Z"), ZoneOffset.UTC);
        scheduler = new TodoPastDueScheduler(todoService, clock);
    }

    @Test
    void markPastDue_invokesServiceWithCurrentTime() {
        var expectedNow = OffsetDateTime.now(clock);

        scheduler.markPastDue();

        verify(todoService).markPastDueItems(expectedNow);
    }

    @Test
    void markPastDue_logsWhenItemsUpdated() {
        when(todoService.markPastDueItems(any(OffsetDateTime.class))).thenReturn(3);

        scheduler.markPastDue();

        verify(todoService).markPastDueItems(any(OffsetDateTime.class));
    }
}

