package com.todolist.todolist.controller;

import com.todolist.todolist.dto.TaskDto;
import com.todolist.todolist.models.Task;
import com.todolist.todolist.services.TaskService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/task")
public class TaskController {

    @Autowired
    private TaskService taskService;

    //ใช้สำหรับ เรียกดูข้อมูล task ทั้งหมด list ของ User ที่ login อยู่
    //ค้นหา task โดยใช้ Name ของ task ่
    @GetMapping("/")
    public ResponseEntity<?> getFindAll(Authentication authentication ,@RequestParam(value = "name",defaultValue = "")String name) {
        String usernameOrEmail = authentication.getName();
        if (name.isEmpty()) {
            return ResponseEntity.status(HttpStatus.OK).body(taskService.getAllTask(usernameOrEmail));
        }
        else {
            return ResponseEntity.status(HttpStatus.OK).body(taskService.findTaskName(name, usernameOrEmail));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Optional<Task>> getTackDetail(Authentication authentication, @PathVariable Long id){
        String usernameOrEmail = authentication.getName(); //ผู้ที่ login
        Optional<Task> task = taskService.findTaskById(id,usernameOrEmail);
        return ResponseEntity.status(HttpStatus.OK).body(task);
    }

    // ใช้สำหรับสร้าง task ของ User ที่ login
    @PostMapping("/")
    public ResponseEntity<?> createTask( Authentication authentication,@Valid @RequestBody Task task){
        TaskDto newTask = taskService.createTask(task,authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(newTask);
    }

    @GetMapping("/completed")
    public ResponseEntity<List<Task>> getAllTaskCompleted(Authentication authentication){
        String usernameOrEmail = authentication.getName(); //ชื่อผู้ที่ login
        List<Task> task = taskService.findAllCompletedTask(usernameOrEmail);
        return ResponseEntity.status(HttpStatus.OK).body(task);
    }

    @GetMapping("/uncompleted")
    public ResponseEntity<List<Task>> getAllTaskUnCompleted(Authentication authentication){
        String usernameOrEmail = authentication.getName(); //ชื่อผู้ที่ login
        List<Task> task = taskService.findAllUnCompletedTask(usernameOrEmail);
        return ResponseEntity.status(HttpStatus.OK).body(task);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateTask(Authentication authentication, @PathVariable Long id,@RequestBody Task task){
        String usernameOrEmail = authentication.getName(); //ชื่อผู้ที่ login
        Optional<Task> taskUpdate = taskService.updateTask(id,task,usernameOrEmail);
        if(taskUpdate.isEmpty()){ //ถ้าค้นหา id ไม่เจอ
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Task or id not found.");
        }
        else {
            return ResponseEntity.status(HttpStatus.OK).body(taskUpdate);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTask(Authentication authentication, @PathVariable Long id){
        String usernameOrEmail = authentication.getName(); //ชื่อผู้ที่ login
        boolean isDelete = taskService.deleteTaskById(id,usernameOrEmail);
        if(isDelete){
            return ResponseEntity.status(HttpStatus.OK).body("delete succeed");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Task or id not found.");
    }
}