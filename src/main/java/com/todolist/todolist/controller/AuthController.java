package com.todolist.todolist.controller;

import com.todolist.todolist.dto.LoginDto;
import com.todolist.todolist.dto.SignUpDto;
import com.todolist.todolist.dto.UserDto;
import com.todolist.todolist.models.Role;
import com.todolist.todolist.models.User;
import com.todolist.todolist.repository.RoleRepository;
import com.todolist.todolist.repository.UserRepository;
import com.todolist.todolist.security.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/signup")
    public ResponseEntity<?> requestUser(@RequestBody SignUpDto signUpDto) {

        // เช็คค่าว่ามี Username นี้ยังใน DB
        if (userRepository.existsByUsername(signUpDto.getUsername())) {
            return new ResponseEntity<>("Username is already taken", HttpStatus.BAD_REQUEST);
        }
        // เช็คค่าว่ามี Email นี้ยังใน DB
        if (userRepository.existsByEmail(signUpDto.getEmail())) {
            return new ResponseEntity<>("Email is already taken", HttpStatus.BAD_REQUEST);
        }
        //สร้าง User
        User user = new User();
        user.setName(signUpDto.getName());
        user.setUsername(signUpDto.getUsername());
        user.setEmail(signUpDto.getEmail());
        user.setPassword(passwordEncoder.encode(signUpDto.getPassword()));

        //บันทึก Role
        Role role = roleRepository.findByName(signUpDto.getRole()) //ผู้ใช้ส่ง role มาจาก request
                .orElseThrow(() -> new RuntimeException("Role not found")); //ถ้าไม่เจอ role "Role not found"

        user.setRoles(Collections.singleton(role)); // ใส่ role ให้ user
        userRepository.save(user);

        UserDto userDto = new UserDto(
                user.getUsername(),
                user.getEmail(),
                role.getName()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(userDto);
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginDto loginDto) {
        // 1) ตรวจสอบ username/password
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginDto.getUsernameOrEmail(),
                        loginDto.getPassword()
                )
        );
        // 2) ดึง username (หรือ email ตาม principal ที่คุณใช้ใน CustomUserDetailsService)
        String principal = authentication.getName();   // จะได้ username หรือ email
        // 3) สร้าง JWT token
        String token = jwtUtil.generateToken(principal);
        // 4) ส่ง Token กลับไปให้ผู้ใช้
        return ResponseEntity.ok(new JwtResponse(token));
    }
    // DTO สำหรับ response
    public record JwtResponse(String token) {}


//    @PostMapping("/login")
//    public ResponseEntity<?> authenticateUser(@RequestBody LoginDto loginDto){ //Basic auth
//        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
//                new UsernamePasswordAuthenticationToken(
//                loginDto.getUsernameOrEmail(),
//                loginDto.getPassword());
//
//        Authentication authentication = authenticationManager.authenticate(usernamePasswordAuthenticationToken); // authenticationManager ตรวจ username + password
//        SecurityContextHolder.getContext().setAuthentication(authentication); //ล็อกอินเข้าสู่ระบบ
//        return new ResponseEntity<>("OK",HttpStatus.OK);
//    }
}


