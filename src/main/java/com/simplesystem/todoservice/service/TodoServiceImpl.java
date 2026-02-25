package com.simplesystem.todoservice.service;

import com.simplesystem.todoservice.exception.DueDateInThePastException;
import com.simplesystem.todoservice.exception.PastDueModificationNotAllowedException;
import com.simplesystem.todoservice.exception.PastDueToUpdateStatusException;
import com.simplesystem.todoservice.exception.TodoNotFoundException;
import com.simplesystem.todoservice.model.TodoItem;
import com.simplesystem.todoservice.model.TodoStatus;
import com.simplesystem.todoservice.repository.TodoItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class TodoServiceImpl implements TodoService {

    private final TodoItemRepository repository;
    private final Clock clock;

    @Override
    public TodoItem createTodo(String description, OffsetDateTime dueAt) {
        if (dueAt.isBefore(OffsetDateTime.now())) {
            throw new DueDateInThePastException();
        }
        val item = new TodoItem();
        item.setDescription(description);
        item.setDueAt(dueAt);
        item.setStatus(TodoStatus.NOT_DONE);

        val savedItem = repository.save(item);

        log.info("Todo item with '{}' id successfully created at {}", savedItem.getId(), savedItem.getCreatedAt());
        return savedItem;
    }

    @Override
    @Transactional(readOnly = true)
    public TodoItem getTodo(Long id) {
        return repository.findById(id).orElseThrow(() -> new TodoNotFoundException(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TodoItem> listTodos(List<TodoStatus> statuses) {
        if (statuses.contains(TodoStatus.NOT_DONE)) {
            return repository.findByStatus(TodoStatus.NOT_DONE);
        }
        return repository.findAll();
    }

    @Override
    public TodoItem updateStatus(Long id, TodoStatus statusToUpdate) {
        if (statusToUpdate == TodoStatus.PAST_DUE) {
            throw new PastDueToUpdateStatusException(id);
        }
        val item = repository.findById(id).orElseThrow(() -> new TodoNotFoundException(id));

        if (item.getStatus() == TodoStatus.PAST_DUE) {
            throw new PastDueModificationNotAllowedException(id);
        }
        item.setStatus(statusToUpdate);

        if (statusToUpdate == TodoStatus.DONE) {
            item.setDoneAt(OffsetDateTime.now(clock));
        } else {
            item.setDoneAt(null);
        }
        return repository.save(item);
    }

    @Override
    public TodoItem updateDescription(Long id, String description) {
        val item = repository.findById(id).orElseThrow(() -> new TodoNotFoundException(id));

        if (item.getStatus() == TodoStatus.PAST_DUE) {
            throw new PastDueModificationNotAllowedException(id);
        }
        item.setDescription(description);
        return repository.save(item);
    }

    @Override
    public int markPastDueItems(OffsetDateTime now) {
        val candidates = repository.findByStatusAndDueAtBefore(TodoStatus.NOT_DONE, now);

        for (var item : candidates) {
            item.setStatus(TodoStatus.PAST_DUE);
        }
        repository.saveAll(candidates);
        return candidates.size();
    }
}
