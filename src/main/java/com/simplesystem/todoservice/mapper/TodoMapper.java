package com.simplesystem.todoservice.mapper;

import com.simplesystem.todoservice.api.model.TodoItemDto;
import com.simplesystem.todoservice.api.model.TodoStatusDto;
import com.simplesystem.todoservice.model.TodoItem;
import com.simplesystem.todoservice.model.TodoStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.openapitools.jackson.nullable.JsonNullable;

import java.time.OffsetDateTime;
import java.util.List;

@Mapper
public interface TodoMapper {

    @Mapping(target = "status", qualifiedByName = "mapApiStatus")
    @Mapping(target = "doneAt", qualifiedByName = "mapJsonNullable")
    TodoItemDto mapToDto(TodoItem entity);

    @Named("mapApiStatus")
    default TodoStatusDto mapApiStatus(TodoStatus status) {
        return status != null ? TodoStatusDto.valueOf(status.name()) : null;
    }

    @Named("mapJsonNullable")
    default JsonNullable<OffsetDateTime> mapJsonNullable(OffsetDateTime value) {
        return value != null ? JsonNullable.of(value) : JsonNullable.undefined();
    }

    List<TodoStatus> mapToStatuses(List<String> receivedStatuses);

    default TodoStatus mapToStatus(String status) {
        return status != null ? TodoStatus.valueOf(status.toUpperCase()) : null;
    }
}

