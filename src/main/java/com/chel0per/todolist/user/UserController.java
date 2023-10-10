package com.chel0per.todolist.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private IUserRepository userRepository;

    @PostMapping("/")
    public ResponseEntity create(@RequestBody UserModel userModel){
        UserModel foundUser = userRepository.findByUsername(userModel.getUsername());
        if(foundUser == null){
            UserModel userCreated = this.userRepository.save(userModel);
            return ResponseEntity.status(200).body(userCreated);
        }
        else{
            System.out.println("Usuário Já Existe");
            return ResponseEntity.status(400).body("Usuário já existe");
        }
    }
    
}
