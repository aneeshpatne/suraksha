package com.aneesh.suraksha;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
public class UserController {
    @Autowired
    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostMapping("/login")
    public String createUser(@RequestBody UserEntity userEntity) {
        UserEntity existingUser = userRepository.findByMailId(userEntity.getMailId());
        if (existingUser != null) {
            return "User Exists";
        }
        userRepository.save(userEntity);
        return "User Created Successfully";
    }

    @GetMapping("/users")
    public List<UserEntity> getAllUsers() {
        return userRepository.findAll();
    }
}
