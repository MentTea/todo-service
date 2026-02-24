package com.simplesystem.todoservice.scheduler;

import com.simplesystem.todoservice.service.TodoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.OffsetDateTime;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "todo.scheduler", name = "enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class TodoPastDueScheduler {

    private final TodoService todoService;
    private final Clock clock;

    @Scheduled(fixedDelayString = "${todo.scheduler.fixedDelay:60000}")
    public void markPastDue() {
        val updated = todoService.markPastDueItems(OffsetDateTime.now(clock));

        if (updated > 0) {
            log.info("Marked {} todo items as PAST_DUE", updated);
        }
    }
}
