package com.simplesystem.todoservice.service;

import com.simplesystem.todoservice.model.TodoItem;
import com.simplesystem.todoservice.model.TodoStatus;

import java.time.OffsetDateTime;
import java.util.List;

public interface TodoService {

    TodoItem createTodo(String description, OffsetDateTime dueAt);

    TodoItem getTodo(Long id);

    List<TodoItem> listTodos(List<TodoStatus> statuses);

    TodoItem updateStatus(Long id, TodoStatus statusToUpdate);

    TodoItem updateDescription(Long id, String description);

    int markPastDueItems(OffsetDateTime now);
}
