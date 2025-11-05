package com.todolist.todolist.dto;

import lombok.Data;

@Data
public class TaskDto {
    private Long owner_id; //id
    private String name;
    private String description;
    private Boolean completed;
}
