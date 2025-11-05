package com.todolist.todolist.repository;

import com.todolist.todolist.models.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task,Long>{
    //หาทั้งหมด
    public List<Task> findAll();

    //หา Task ที่ Login จากที่ BasicAuthentication
    public List<Task> findByOwner_UsernameOrOwner_Email(String username,String email);

    //ค้นหา Task จาก Name
    public List<Task> findByOwner_UsernameOrOwner_EmailAndNameContaining(String username,String email,String name);

    List<Task> findByOwner_UsernameOrOwner_EmailAndCompletedTrue(String username,String email);

    List<Task> findByOwner_UsernameOrOwner_EmailAndCompletedFalse(String username,String email);
}
