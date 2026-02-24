package com.simplesystem.todoservice.config;

import com.simplesystem.todoservice.mapper.TodoMapper;
import org.mapstruct.factory.Mappers;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MapStructConfig {

    @Bean
    public TodoMapper entityRestMapper() {
        return Mappers.getMapper(TodoMapper.class);
    }
}
