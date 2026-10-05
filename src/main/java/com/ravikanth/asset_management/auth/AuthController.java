
package com.ravikanth.asset_management.auth;

import com.ravikanth.asset_management.user.User;
import com.ravikanth.asset_management.user.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final AuthService authService;
    private final JwtService jwtService;

    public AuthController(
            UserService userService,
            AuthService authService,
            JwtService jwtService
    ) {
        this.userService = userService;
        this.authService = authService;
        this.jwtService = jwtService;
    }

    // Register a new student account
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public RegisterResponse register(
            @Valid @RequestBody RegisterRequest request
    ) {

        User user = userService.registerUser(
                request.getFullName(),
                request.getEmail(),
                request.getPassword(),
                request.getMobileNumber()
        );

        return RegisterResponse.from(user);
    }

    // Authenticate a user and return a JWT token
    @PostMapping("/login")
    public LoginResponse login(
            @Valid @RequestBody LoginRequest request
    ) {

        User user = authService.authenticate(
                request.getEmail(),
                request.getPassword()
        );

        String token = jwtService.generateToken(user);

        return new LoginResponse(
                token,
                "Bearer",
                user.getId(),
                user.getEmail(),
                user.getRole().name()
        );
    }
}

