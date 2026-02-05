package com.fitness.userservice.controller;

import com.fitness.userservice.dto.RegisterRequest;
import com.fitness.userservice.dto.UserResponse;
import com.fitness.userservice.services.UserServices;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@AllArgsConstructor
public class UserController {

    private  UserServices userServices;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request){
         return ResponseEntity.ok(userServices.register(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getProfileById(@PathVariable String id){
        return ResponseEntity.ok(userServices.getUserById(id));
    }

    @GetMapping("/{id}/validate")
    public ResponseEntity<Boolean> validateUser(@PathVariable String id){
        return ResponseEntity.ok(userServices.checkUserExist(id));
    }
}
