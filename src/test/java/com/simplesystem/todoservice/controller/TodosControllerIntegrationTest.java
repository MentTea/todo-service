package com.simplesystem.todoservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.simplesystem.todoservice.model.TodoItem;
import com.simplesystem.todoservice.model.TodoStatus;
import com.simplesystem.todoservice.repository.TodoItemRepository;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.OffsetDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "todo.scheduler.enabled=false")
@TestPropertySource(properties = "todo.scheduler.enabled=false")
class TodosControllerIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext wac;

    private ObjectMapper objectMapper;

    @Autowired
    private TodoItemRepository repository;

    @BeforeEach
    void clean() {
        repository.deleteAll();
        this.objectMapper = new ObjectMapper();
        this.mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    @Test
    void createAndGetTodo_success() throws Exception {
        val payload = """
            {
              "description": "Test todo",
              "dueAt": "2030-01-01T10:00:00Z"
            }
            """;

        val response = mockMvc.perform(post("/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.description").value("Test todo"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        long id = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(get("/todos/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));
    }

    @Test
    void listTodos_filtersNotDone() throws Exception {
        val notDone = new TodoItem();
        notDone.setDescription("Not done");
        notDone.setStatus(TodoStatus.NOT_DONE);
        notDone.setCreatedAt(OffsetDateTime.now());
        notDone.setDueAt(OffsetDateTime.now().plusDays(1));
        repository.save(notDone);

        val done = new TodoItem();
        done.setDescription("Done");
        done.setStatus(TodoStatus.DONE);
        done.setCreatedAt(OffsetDateTime.now());
        done.setDueAt(OffsetDateTime.now().plusDays(1));
        repository.save(done);

        mockMvc.perform(get("/todos").param("statuses", "not_done"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].description").value("Not done"));
    }

    @Test
    void operationsOnPastDue_returnConflict() throws Exception {
        var item = new TodoItem();
        item.setDescription("Past due");
        item.setStatus(TodoStatus.PAST_DUE);
        item.setCreatedAt(OffsetDateTime.now().minusDays(2));
        item.setDueAt(OffsetDateTime.now().minusDays(1));
        item = repository.save(item);

        mockMvc.perform(put("/todos/{id}/description", item.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"new\"}"))
                .andExpect(status().isConflict());

        mockMvc.perform(post("/todos/{id}/done", item.getId()))
                .andExpect(status().isConflict());

        mockMvc.perform(post("/todos/{id}/not-done", item.getId()))
                .andExpect(status().isConflict());
    }

    @Test
    void getMissingTodo_returnsNotFound() throws Exception {
        mockMvc.perform(get("/todos/{id}", 9999))
                .andExpect(status().isNotFound());
    }
}
