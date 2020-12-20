package com.Apiweb.polling.controller;
import com.Apiweb.polling.model.Role;
import com.Apiweb.polling.model.RoleName;
import com.Apiweb.polling.model.User;
import com.Apiweb.polling.payload.ApiResponse;
import com.Apiweb.polling.payload.JwtAuthenticationResponse;
import com.Apiweb.polling.payload.LoginRequest;
import com.Apiweb.polling.payload.InscriptionRequest;
import com.Apiweb.polling.depot.*;
import com.Apiweb.polling.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import java.net.URI;
import java.util.Collections;
import java.util.Optional;

public class AuthController {
    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UtiDepot utiDepot;

    @Autowired
    RoleDepot roleDepot;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    JwtTokenProvider tokenProvider;

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsernameOrEmail(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = tokenProvider.generateToken(authentication);
        return ResponseEntity.ok(new JwtAuthenticationResponse(jwt));
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody InscriptionRequest inscriptionRequest) {
        if(utiDepot.existsByUsername(inscriptionRequest.getUsername())) {
            return new ResponseEntity(new ApiResponse(false, "Username is already taken!"),
                    HttpStatus.BAD_REQUEST);
        }

        if(utiDepot.existsByEmail(inscriptionRequest.getEmail())) {
            return new ResponseEntity(new ApiResponse(false, "Email Address already in use!"),
                    HttpStatus.BAD_REQUEST);
        }

        // Creating user's account
        User user = new User(inscriptionRequest.getNom(), inscriptionRequest.getPrenom(), inscriptionRequest.getUsername(),
                inscriptionRequest.getEmail(), inscriptionRequest.getPassword());

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        Role userRole = roleDepot.findByName(RoleName.ROLE_USER)
                .orElseThrow(/*() -> new AppException("User Role not set.")*/);

        user.setRoles(Collections.singleton(userRole));

        User result = utiDepot.save(user);

        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath().path("/api/users/{username}")
                .buildAndExpand(result.getUsername()).toUri();

        return ResponseEntity.created(location).body(new ApiResponse(true, "User registered successfully"));
    }
}

