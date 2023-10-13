package com.chel0per.todolist.task;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    @Autowired
    private ITaskRepository taskRepository;

    @PostMapping("/")
    public ResponseEntity create(@RequestBody TaskModel taskModel, HttpServletRequest request){
        
        taskModel.setUserId((UUID) request.getAttribute("userId"));

        LocalDateTime currentDate = LocalDateTime.now();

        if(currentDate.isAfter(taskModel.getStartAt()) && currentDate.isAfter(taskModel.getEndAt())){
            return ResponseEntity.status(400).body("A data de início ou de término deve ser depois da data atual");
        }

        if(taskModel.getStartAt().isAfter(taskModel.getEndAt())){
           return ResponseEntity.status(400).body("A data de início deve ser antes da data de término"); 
        } 
        
        TaskModel taskCreated = this.taskRepository.save(taskModel);
        return ResponseEntity.status(200).body(taskCreated);
           
    }

    @GetMapping("/")
    public List<TaskModel> list(HttpServletRequest request){

        UUID userId = (UUID) request.getAttribute("userId");

        List<TaskModel> userTasks = this.taskRepository.findByUserId(userId);
        return userTasks;
    }

    @PutMapping("/{taskId}")
    public ResponseEntity update(@RequestBody TaskModel taskModel,@PathVariable UUID taskId,HttpServletRequest request){
        Optional<TaskModel> optionalTask = this.taskRepository.findById(taskId);
        if(optionalTask.isEmpty()){
            return ResponseEntity.status(401).body("Tarefa não encontrada");
        }
        TaskModel task = optionalTask.get();

        UUID userId = (UUID) request.getAttribute("userId");

        if(!task.getUserId().equals(userId)){
            return ResponseEntity.status(401).body("O usuário não tem permissão de alterar essa tarefa");
        }

        Class<?> classObject = taskModel.getClass();
        Field[] fields = classObject.getDeclaredFields();
        
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                Object value = field.get(taskModel);
                if(value != null){
                    field.set(task, value);
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        TaskModel taskUpdated = this.taskRepository.save(task);
        return ResponseEntity.status(200).body(taskUpdated);
    }
    
}
