package com.simplesystem.todoservice.api.impl;

import com.simplesystem.todoservice.api.TodosApi;
import com.simplesystem.todoservice.api.model.CreateTodoDto;
import com.simplesystem.todoservice.api.model.TodoItemDto;
import com.simplesystem.todoservice.api.model.UpdateDescriptionDto;
import com.simplesystem.todoservice.api.model.UpdateStatusDto;
import com.simplesystem.todoservice.mapper.TodoMapper;
import com.simplesystem.todoservice.service.TodoService;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class TodosController implements TodosApi {

    private final TodoService todoService;
    private final TodoMapper todoMapper;

    @Override
    public ResponseEntity<TodoItemDto> createTodo(CreateTodoDto createTodoDto) {
        val created = todoService.createTodo(createTodoDto.getDescription(), createTodoDto.getDueAt());
        val body = todoMapper.mapToDto(created);
        val location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();
        return ResponseEntity.created(location).body(body);
    }

    @Override
    public ResponseEntity<TodoItemDto> getTodoById(Long id) {
        val item = todoService.getTodo(id);
        return ResponseEntity.ok(todoMapper.mapToDto(item));
    }

    @Override
    public ResponseEntity<List<TodoItemDto>> listTodos(List<String> statuses) {
        val items = todoService.listTodos(todoMapper.mapToStatuses(statuses))
                .stream()
                .map(todoMapper::mapToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(items);
    }

    @Override
    public ResponseEntity<TodoItemDto> updateStatus(Long id, UpdateStatusDto updateStatusDto) {
        val statusToUpdate = todoMapper.mapToStatus(updateStatusDto.getStatus());
        val updated = todoService.updateStatus(id, statusToUpdate);
        return ResponseEntity.ok(todoMapper.mapToDto(updated));
    }

    @Override
    public ResponseEntity<TodoItemDto> updateDescription(Long id, UpdateDescriptionDto updateDescriptionDto) {
        val updated = todoService.updateDescription(id, updateDescriptionDto.getDescription());
        return ResponseEntity.ok(todoMapper.mapToDto(updated));
    }
}

