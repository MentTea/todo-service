package com.simplesystem.todoservice.service;

import com.simplesystem.todoservice.exception.PastDueModificationNotAllowedException;
import com.simplesystem.todoservice.exception.TodoNotFoundException;
import com.simplesystem.todoservice.model.TodoItem;
import com.simplesystem.todoservice.model.TodoStatus;
import com.simplesystem.todoservice.repository.TodoItemRepository;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TodoServiceImplTest {

    private final TodoItemRepository repository = Mockito.mock(TodoItemRepository.class, RETURNS_DEEP_STUBS);

    private Clock clock;
    private TodoService service;

    @BeforeEach
    void setUp() {
        clock = Clock.fixed(Instant.parse("2025-01-01T10:00:00Z"), ZoneOffset.UTC);
        service = new TodoServiceImpl(repository, clock);
    }

    @Test
    void createTodo_persistsWithNotDoneStatus() {
        var saved = new TodoItem();
        saved.setId(1L);
        saved.setDescription("test");
        saved.setStatus(TodoStatus.NOT_DONE);
        saved.setCreatedAt(OffsetDateTime.now(clock));
        when(repository.save(any(TodoItem.class))).thenReturn(saved);

        val result = service.createTodo("test", OffsetDateTime.now(clock).plusDays(1));

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getStatus()).isEqualTo(TodoStatus.NOT_DONE);
        verify(repository).save(any(TodoItem.class));
    }

    @Test
    void getTodo_throwsWhenNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getTodo(99L))
                .isInstanceOf(TodoNotFoundException.class);
    }

    @Test
    void markDone_setsDoneAtAndStatus() {
        val existing = new TodoItem();
        existing.setId(1L);
        existing.setStatus(TodoStatus.NOT_DONE);
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(any(TodoItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = service.markDone(1L);

        assertThat(result.getStatus()).isEqualTo(TodoStatus.DONE);
        assertThat(result.getDoneAt()).isEqualTo(OffsetDateTime.now(clock));
    }

    @Test
    void markNotDone_resetsDoneAtAndStatus() {
        val existing = new TodoItem();
        existing.setId(1L);
        existing.setStatus(TodoStatus.DONE);
        existing.setDoneAt(OffsetDateTime.now(clock));
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(any(TodoItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

        val result = service.markNotDone(1L);

        assertThat(result.getStatus()).isEqualTo(TodoStatus.NOT_DONE);
        assertThat(result.getDoneAt()).isNull();
    }

    @Test
    void operationsOnPastDue_throwConflict() {
        val existing = new TodoItem();
        existing.setId(1L);
        existing.setStatus(TodoStatus.PAST_DUE);
        when(repository.findById(1L)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> service.updateDescription(1L, "x"))
                .isInstanceOf(PastDueModificationNotAllowedException.class);
        assertThatThrownBy(() -> service.markDone(1L))
                .isInstanceOf(PastDueModificationNotAllowedException.class);
        assertThatThrownBy(() -> service.markNotDone(1L))
                .isInstanceOf(PastDueModificationNotAllowedException.class);
    }

    @Test
    void markPastDueItems_updatesEligibleItems() {
        val now = OffsetDateTime.now(clock);
        val a = new TodoItem();
        a.setStatus(TodoStatus.NOT_DONE);
        a.setDueAt(now.minusDays(1));
        val b = new TodoItem();
        b.setStatus(TodoStatus.NOT_DONE);
        b.setDueAt(now.minusHours(1));
        when(repository.findByStatusAndDueAtBefore(TodoStatus.NOT_DONE, now))
                .thenReturn(List.of(a, b));

        var updated = service.markPastDueItems(now);

        assertThat(updated).isEqualTo(2);
        ArgumentCaptor<List<TodoItem>> captor = ArgumentCaptor.forClass(List.class);
        verify(repository).saveAll(captor.capture());
        assertThat(captor.getValue())
                .extracting(TodoItem::getStatus)
                .containsOnly(TodoStatus.PAST_DUE);
    }
}

