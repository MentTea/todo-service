package com.simplesystem.todoservice.service;

import com.simplesystem.todoservice.model.TodoItem;
import com.simplesystem.todoservice.model.TodoStatus;

import java.time.OffsetDateTime;
import java.util.List;

public interface TodoService {

    TodoItem createTodo(String description, OffsetDateTime dueAt);

    TodoItem getTodo(Long id);

    List<TodoItem> listTodos(List<TodoStatus> statuses);

    TodoItem updateDescription(Long id, String description);

    TodoItem markDone(Long id);

    TodoItem markNotDone(Long id);

    int markPastDueItems(OffsetDateTime now);
}
