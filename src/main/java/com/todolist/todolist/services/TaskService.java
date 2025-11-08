package com.todolist.todolist.services;

import com.todolist.todolist.dto.TaskDto;
import com.todolist.todolist.models.Task;
import com.todolist.todolist.models.User;
import com.todolist.todolist.repository.TaskRepository;
import com.todolist.todolist.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class TaskService {
    private TaskRepository taskRepository;
    private UserRepository userRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public TaskService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<Task> getAllTask(String usernameOrEmail){
        return taskRepository.findByOwner_UsernameOrOwner_Email(usernameOrEmail, usernameOrEmail);
    }

    public List<Task> findTaskName(String name, String usernameOrEmail){
        return taskRepository.findByOwner_UsernameOrOwner_EmailAndNameContaining(usernameOrEmail, usernameOrEmail, name);
    }
    @Transactional
    public TaskDto createTask(Task task, String usernameOrEmail) {
        User user = userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        task.setOwner(user);

        Task taskSaved = taskRepository.save(task);

        TaskDto taskDto = new TaskDto();
        taskDto.setOwner_id(taskSaved.getOwner().getId());
        taskDto.setName(taskSaved.getName());
        taskDto.setDescription(taskSaved.getDescription());
        taskDto.setCompleted(taskSaved.getCompleted());

        return taskDto;
    }

    public Optional<Task> findTaskById(Long id, String usernameOrEmail){
        return taskRepository.findById(id).filter(task ->
                Objects.equals(task.getOwner().getName(), usernameOrEmail)||
                Objects.equals(task.getOwner().getEmail(), usernameOrEmail));
    }

    public List<Task> findAllCompletedTask(String usernameOrEmail) {
        return taskRepository.findByOwner_UsernameOrOwner_EmailAndCompletedTrue(usernameOrEmail, usernameOrEmail);
    }

    public List<Task> findAllUnCompletedTask(String usernameOrEmail) {
        return taskRepository.findByOwner_UsernameOrOwner_EmailAndCompletedFalse(usernameOrEmail, usernameOrEmail);
    }

    @Transactional
    public Optional<Task> updateTask(Long id, Task task, String usernameOrEmail){
        Optional<Task> getTask = taskRepository.findById(id).filter(findTask ->
                // ดึง Task ตาม ID ตรวจสอบว่าผู้ที่ส่งคำขอ (usernameOrEmail) เป็นเจ้าของ Task นี้หรือเปล่า
                Objects.equals(findTask.getOwner().getName(), usernameOrEmail) ||
                Objects.equals(findTask.getOwner().getEmail(), usernameOrEmail)
        );

        if (getTask.isEmpty()) { //ถ้าไม่ใช่
            return getTask;
        } //ถ้าใช่
        if (task.getName() != null ) {
            getTask.get().setName(task.getName());
        }
        if (task.getDescription() != null ) {
            getTask.get().setDescription(task.getDescription());
        }
        if (task.getCompleted() != null ) {
            getTask.get().setCompleted(task.getCompleted());
        }

        return Optional.of(taskRepository.save(getTask.get()));
    }

    @Transactional
    public boolean deleteTaskById(Long id,String usernameOrEmail ){
        Optional<Task> getTask = taskRepository.findById(id).filter(findTask ->
                // ดึง Task ตาม ID ตรวจสอบว่าผู้ที่ส่งคำขอ (usernameOrEmail) เป็นเจ้าของ Task นี้หรือเปล่า
                Objects.equals(findTask.getOwner().getName(), usernameOrEmail) ||
                Objects.equals(findTask.getOwner().getEmail(), usernameOrEmail));
        if(getTask.isEmpty())
        {
            return false;
        }
        getTask.get().setOwner(null);
        taskRepository.save(getTask.get()); // Owner = null เพื่อลบข้อมูลได้

        taskRepository.deleteById(id);
        return true;
    }
}
