package com.auth.controller;

import com.auth.dto.*;
import com.auth.entity.Role;
import com.auth.entity.User;
import com.auth.repository.RoleRepository;
import com.auth.repository.UserRepository;
import com.auth.security.JwtGenerator;
import com.auth0.jwk.Jwk;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.UrlJwkProvider;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.net.URL;
import java.security.interfaces.RSAPublicKey;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.auth0.jwt.JWT;

@RestController
@RequestMapping("/auth")
public class UserController {
    private UserRepository userRepository;
    private RoleRepository roleRepository;
    private AuthenticationManager authenticationManager;
    private PasswordEncoder passwordEncoder;
    private JwtGenerator jwtGenerator;
    @Autowired
    public UserController(UserRepository userRepository, RoleRepository roleRepository, AuthenticationManager authenticationManager, PasswordEncoder passwordEncoder, JwtGenerator jwtGenerator) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
        this.jwtGenerator = jwtGenerator;
    }
    @PostMapping("/user/register")
    public ResponseEntity<String> registerUser(@RequestBody AuthDto authDto){
        if (authDto.getUsername() == null || authDto.getUsername().trim().isEmpty()) {
            return new ResponseEntity<>("Username cannot be empty!", HttpStatus.BAD_REQUEST);
        }
        if (authDto.getPhone_number() == null || !authDto.getPhone_number().matches("\\+?\\d{10,15}")) {
            return new ResponseEntity<>("Invalid phone number format!", HttpStatus.BAD_REQUEST);
        }
        if (authDto.getEmail() == null || !authDto.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            return new ResponseEntity<>("Invalid email format!", HttpStatus.BAD_REQUEST);
        }
        if (authDto.getFirst_name() == null || authDto.getFirst_name().trim().isEmpty()) {
            return new ResponseEntity<>("First name cannot be empty!", HttpStatus.BAD_REQUEST);
        }
        if (authDto.getLast_name() == null || authDto.getLast_name().trim().isEmpty()) {
            return new ResponseEntity<>("Last name cannot be empty!", HttpStatus.BAD_REQUEST);
        }
        if (authDto.getDate_of_birth() == null || !authDto.getDate_of_birth().matches("\\d{4}-\\d{2}-\\d{2}")) {
            return new ResponseEntity<>("Invalid date format! Use YYYY-MM-DD.", HttpStatus.BAD_REQUEST);
        }

        LocalDate dob = LocalDate.parse(authDto.getDate_of_birth());
        LocalDate today = LocalDate.now();
        LocalDate minDate = today.minusYears(100);

        if (dob.isAfter(today)) {
            return new ResponseEntity<>("Date of birth cannot be in the future!", HttpStatus.BAD_REQUEST);
        }
        if (dob.isBefore(minDate)) {
            return new ResponseEntity<>("Date of birth is too far in the past!", HttpStatus.BAD_REQUEST);
        }
        if (authDto.getPreferred_language() == null || authDto.getPreferred_language().trim().isEmpty()) {
            return new ResponseEntity<>("Preferred language cannot be empty!", HttpStatus.BAD_REQUEST);
        }
        if (authDto.getPassword() == null || authDto.getPassword().length() < 6) {
            return new ResponseEntity<>("Password must be at least 6 characters long!", HttpStatus.BAD_REQUEST);
        }

        Optional<User> existingUserOpt = userRepository.findByUsername(authDto.getUsername());
        if (existingUserOpt.isPresent()) {
            User existingUser = existingUserOpt.get();
            boolean isAlreadyUser = existingUser.getRoles().stream()
                    .anyMatch(role -> role.getName().equals("USER"));

            if (isAlreadyUser) {
                return new ResponseEntity<>("User with username: " + authDto.getUsername() + " already exists as USER!", HttpStatus.BAD_REQUEST);
            }

            Role userRole = roleRepository.findByName("USER").orElseThrow();
            existingUser.getRoles().add(userRole);
            userRepository.save(existingUser);
            return new ResponseEntity<>("USER role added to existing OWNER account!", HttpStatus.OK);
        }

        User user = new User();
        user.setUsername(authDto.getUsername());
        user.setPhone_number(authDto.getPhone_number());
        user.setEmail(authDto.getEmail());
        user.setFirst_name(authDto.getFirst_name());
        user.setLast_name(authDto.getLast_name());
        user.setDate_of_birth(authDto.getDate_of_birth());
        user.setPreferred_language(authDto.getPreferred_language());
        user.setPassword(passwordEncoder.encode(authDto.getPassword()));

        Role role = roleRepository.findByName("USER").orElseThrow();
        user.setRoles(Collections.singletonList(role));
        userRepository.save(user);
        return new ResponseEntity<>("User has been registered success!!!", HttpStatus.OK);
    }

    @PostMapping("/user/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginDto loginDto) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDto.getUsername(), loginDto.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        User user = userRepository.findByUsername(loginDto.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<String> roles = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList());

        if (roles.contains("OWNER") && !roles.contains("USER")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not allowed to log in as USER");
        }

        String token = jwtGenerator.generateToken(authentication, user.getId(), roles);

        return ResponseEntity.ok(new AuthResponseDto(user.getId(), token));
    }
    @PostMapping("/owner/register")
    public ResponseEntity<String> registerOwner(@RequestBody AuthDto authDto) {
        if (authDto.getUsername() == null || authDto.getUsername().trim().isEmpty()) {
            return new ResponseEntity<>("Username cannot be empty!", HttpStatus.BAD_REQUEST);
        }
        if (authDto.getPhone_number() == null || !authDto.getPhone_number().matches("\\+?\\d{10,15}")) {
            return new ResponseEntity<>("Invalid phone number format!", HttpStatus.BAD_REQUEST);
        }
        if (authDto.getEmail() == null || !authDto.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            return new ResponseEntity<>("Invalid email format!", HttpStatus.BAD_REQUEST);
        }
        if (authDto.getFirst_name() == null || authDto.getFirst_name().trim().isEmpty()) {
            return new ResponseEntity<>("First name cannot be empty!", HttpStatus.BAD_REQUEST);
        }
        if (authDto.getLast_name() == null || authDto.getLast_name().trim().isEmpty()) {
            return new ResponseEntity<>("Last name cannot be empty!", HttpStatus.BAD_REQUEST);
        }
        if (authDto.getDate_of_birth() == null || !authDto.getDate_of_birth().matches("\\d{4}-\\d{2}-\\d{2}")) {
            return new ResponseEntity<>("Invalid date format! Use YYYY-MM-DD.", HttpStatus.BAD_REQUEST);
        }

        LocalDate dob = LocalDate.parse(authDto.getDate_of_birth());
        LocalDate today = LocalDate.now();
        LocalDate minDate = today.minusYears(100);

        if (dob.isAfter(today)) {
            return new ResponseEntity<>("Date of birth cannot be in the future!", HttpStatus.BAD_REQUEST);
        }
        if (dob.isBefore(minDate)) {
            return new ResponseEntity<>("Date of birth is too far in the past!", HttpStatus.BAD_REQUEST);
        }
        if (authDto.getPreferred_language() == null || authDto.getPreferred_language().trim().isEmpty()) {
            return new ResponseEntity<>("Preferred language cannot be empty!", HttpStatus.BAD_REQUEST);
        }
        if (authDto.getPassword() == null || authDto.getPassword().length() < 6) {
            return new ResponseEntity<>("Password must be at least 6 characters long!", HttpStatus.BAD_REQUEST);
        }

        Optional<User> existingUserOpt = userRepository.findByUsername(authDto.getUsername());
        if (existingUserOpt.isPresent()) {
            User existingUser = existingUserOpt.get();
            boolean isAlreadyOwner = existingUser.getRoles().stream()
                    .anyMatch(role -> role.getName().equals("OWNER"));

            if (isAlreadyOwner) {
                return new ResponseEntity<>("User already has OWNER role!", HttpStatus.BAD_REQUEST);
            }

            Role ownerRole = roleRepository.findByName("OWNER").orElseThrow();
            existingUser.getRoles().add(ownerRole);
            userRepository.save(existingUser);
            return new ResponseEntity<>("OWNER role added to existing USER account!", HttpStatus.OK);
        }

        User user = new User();
        user.setUsername(authDto.getUsername());
        user.setPhone_number(authDto.getPhone_number());
        user.setEmail(authDto.getEmail());
        user.setFirst_name(authDto.getFirst_name());
        user.setLast_name(authDto.getLast_name());
        user.setDate_of_birth(authDto.getDate_of_birth());
        user.setPreferred_language(authDto.getPreferred_language());
        user.setPassword(passwordEncoder.encode(authDto.getPassword()));

        Role role = roleRepository.findByName("OWNER").orElseThrow();
        user.setRoles(Collections.singletonList(role));
        userRepository.save(user);
        return new ResponseEntity<>("Owner registered successfully!", HttpStatus.OK);
    }
    @PostMapping("/owner/login")
    public ResponseEntity<?> loginOwner(@RequestBody LoginDto loginDto) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDto.getUsername(), loginDto.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        User user = userRepository.findByUsername(loginDto.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<String> roles = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList());

        if (!roles.contains("OWNER")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not allowed to log in as OWNER");
        }

        String token = jwtGenerator.generateToken(authentication, user.getId(), roles);
        return ResponseEntity.ok(new AuthResponseDto(user.getId(), token));
    }
    @GetMapping("/userProfile")
    public ResponseEntity<UserProfileDto> getUserById(@RequestHeader("X-User-Id") UUID userId) {
        return userRepository.findById(userId)
                .map(user -> new ResponseEntity<>(new UserProfileDto(user), HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
    @PatchMapping("/userProfile/update")
    public ResponseEntity<String> updateUser(@RequestHeader("X-User-Id") UUID userId, @RequestBody UpdateUserDto updateUserDto) {
        return userRepository.findById(userId)
                .map(user -> {
                    List<String> errors = new ArrayList<>();

                    // Validate Phone Number
                    if (updateUserDto.getPhone_number() != null) {
                        if (!updateUserDto.getPhone_number().matches("\\+?\\d{10,15}")) {
                            errors.add("Invalid phone number format! It should be 10-15 digits long.");
                        } else {
                            user.setPhone_number(updateUserDto.getPhone_number());
                        }
                    }

                    // Validate Email
                    if (updateUserDto.getEmail() != null) {
                        if (!updateUserDto.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                            errors.add("Invalid email format!");
                        } else {
                            user.setEmail(updateUserDto.getEmail());
                        }
                    }

                    // Validate Preferred Language
                    if (updateUserDto.getPreferred_language() != null) {
                        if (updateUserDto.getPreferred_language().trim().isEmpty()) {
                            errors.add("Preferred language cannot be empty!");
                        } else {
                            user.setPreferred_language(updateUserDto.getPreferred_language());
                        }
                    }

                    // Validate First Name
                    if (updateUserDto.getFirst_name() != null) {
                        if (updateUserDto.getFirst_name().trim().isEmpty()) {
                            errors.add("First name cannot be empty!");
                        } else {
                            user.setFirst_name(updateUserDto.getFirst_name());
                        }
                    }

                    // Validate Last Name
                    if (updateUserDto.getLast_name() != null) {
                        if (updateUserDto.getLast_name().trim().isEmpty()) {
                            errors.add("Last name cannot be empty!");
                        } else {
                            user.setLast_name(updateUserDto.getLast_name());
                        }
                    }

                    if (updateUserDto.getDate_of_birth() != null) {
                        if (!updateUserDto.getDate_of_birth().matches("\\d{4}-\\d{2}-\\d{2}")) {
                            errors.add("Invalid date format! Use YYYY-MM-DD.");
                        } else {
                            LocalDate dob = LocalDate.parse(updateUserDto.getDate_of_birth());
                            LocalDate today = LocalDate.now();
                            LocalDate minDate = today.minusYears(100); // At most 100 years old

                            if (dob.isAfter(today)) {
                                errors.add("Date of birth cannot be in the future!");
                            } else if (dob.isBefore(minDate)) {
                                errors.add("Date of birth is too far in the past!");
                            } else {
                                user.setDate_of_birth(updateUserDto.getDate_of_birth());
                            }
                        }
                    }

                    // If there are validation errors, return them instead of updating the user
                    if (!errors.isEmpty()) {
                        return new ResponseEntity<>(String.join("\n", errors), HttpStatus.BAD_REQUEST);
                    }

                    userRepository.save(user);
                    return new ResponseEntity<>("Profile updated successfully!", HttpStatus.OK);
                })
                .orElse(new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND));
    }

    @PutMapping("/userProfile/update/password")
    public ResponseEntity<String> updatePassword(@RequestHeader("X-User-Id") UUID userId, @RequestBody UpdatePasswordDto updatePasswordDto) {
        return userRepository.findById(userId)
                .map(user -> {
                    // Validate old password
                    if (!passwordEncoder.matches(updatePasswordDto.getOldPassword(), user.getPassword())) {
                        return new ResponseEntity<>("Old password is incorrect", HttpStatus.BAD_REQUEST);
                    }

                    // Validate new password length
                    if (updatePasswordDto.getNewPassword().length() < 6) {
                        return new ResponseEntity<>("New password must be at least 6 characters long", HttpStatus.BAD_REQUEST);
                    }

                    // Update password
                    user.setPassword(passwordEncoder.encode(updatePasswordDto.getNewPassword()));
                    userRepository.save(user);
                    return new ResponseEntity<>("Password updated successfully!", HttpStatus.OK);
                })
                .orElse(new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND));
    }
    @Value("${auth0.domain}")
    private String auth0Domain;

    @Value("${auth0.audience}")
    private String auth0Audience;
    @PostMapping("/user/social-login")
    public ResponseEntity<?> socialLoginUser(@RequestBody SocialLoginDto dto) {
        String idToken = dto.getId_token();
        if (idToken == null || idToken.isEmpty()) {
            return ResponseEntity.badRequest().body("id_token is required");
        }

        try {
            URL jwksUrl = new URL("https://" + auth0Domain + "/.well-known/jwks.json");
            JwkProvider provider = new UrlJwkProvider(jwksUrl);
            DecodedJWT decoded = JWT.decode(idToken);
            Jwk jwk = provider.get(decoded.getKeyId());

            Algorithm algorithm = Algorithm.RSA256((RSAPublicKey) jwk.getPublicKey(), null);
            JWTVerifier verifier = JWT.require(algorithm)
                    .withAudience(auth0Audience)
                    .withIssuer("https://" + auth0Domain + "/")
                    .build();

            DecodedJWT jwt = verifier.verify(idToken);

            String email = jwt.getClaim("email").asString();
            String givenName = jwt.getClaim("given_name").asString();
            String familyName = jwt.getClaim("family_name").asString();
            if (email == null || email.isEmpty()) {
                return ResponseEntity.badRequest().body("Email not found in token");
            }

            User user = userRepository.findByEmail(email).orElse(null);
            Role userRole = roleRepository.findByName("USER").orElseThrow();

            if (user == null) {
                user = new User();
                user.setUsername(email.split("@")[0] + UUID.randomUUID().toString().substring(0, 5));
                user.setEmail(email);
                user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
                user.setFirst_name(givenName != null ? givenName : "");
                user.setLast_name(familyName != null ? familyName : "");
                user.setPreferred_language("English");
                user.setDate_of_birth("");
                user.setPhone_number("");
                user.setRoles(Collections.singletonList(userRole));
                userRepository.save(user);
            } else {
                boolean hasUserRole = user.getRoles().stream()
                        .anyMatch(role -> role.getName().equals("USER"));
                if (!hasUserRole) {
                    user.getRoles().add(userRole);
                    userRepository.save(user);
                }
            }

            List<GrantedAuthority> authorities = user.getRoles().stream()
                    .map(role -> new SimpleGrantedAuthority(role.getName()))
                    .collect(Collectors.toList());

            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(user.getUsername(), null, authorities);
            SecurityContextHolder.getContext().setAuthentication(authToken);

            String token = jwtGenerator.generateToken(authToken, user.getId(), user.getRoles().stream().map(Role::getName).toList());
            return ResponseEntity.ok(new AuthResponseDto(user.getId(), token));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token: " + e.getMessage());
        }
    }

    @PostMapping("/owner/social-login")
    public ResponseEntity<?> socialLoginOwner(@RequestBody SocialLoginDto dto) {
        String idToken = dto.getId_token();
        if (idToken == null || idToken.isEmpty()) {
            return ResponseEntity.badRequest().body("id_token is required");
        }

        try {
            URL jwksUrl = new URL("https://" + auth0Domain + "/.well-known/jwks.json");
            JwkProvider provider = new UrlJwkProvider(jwksUrl);
            DecodedJWT decoded = JWT.decode(idToken);
            Jwk jwk = provider.get(decoded.getKeyId());

            Algorithm algorithm = Algorithm.RSA256((RSAPublicKey) jwk.getPublicKey(), null);
            JWTVerifier verifier = JWT.require(algorithm)
                    .withAudience(auth0Audience)
                    .withIssuer("https://" + auth0Domain + "/")
                    .build();

            DecodedJWT jwt = verifier.verify(idToken);

            String email = jwt.getClaim("email").asString();
            String givenName = jwt.getClaim("given_name").asString();
            String familyName = jwt.getClaim("family_name").asString();
            if (email == null || email.isEmpty()) {
                return ResponseEntity.badRequest().body("Email not found in token");
            }

            User user = userRepository.findByEmail(email).orElse(null);
            Role ownerRole = roleRepository.findByName("OWNER").orElseThrow();

            if (user == null) {
                user = new User();
                user.setUsername(email.split("@")[0] + UUID.randomUUID().toString().substring(0, 5));
                user.setEmail(email);
                user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
                user.setFirst_name(givenName != null ? givenName : "");
                user.setLast_name(familyName != null ? familyName : "");
                user.setPreferred_language("English");
                user.setDate_of_birth("");
                user.setPhone_number("");
                user.setRoles(Collections.singletonList(ownerRole));
                userRepository.save(user);
            } else {
                boolean hasOwnerRole = user.getRoles().stream()
                        .anyMatch(role -> role.getName().equals("OWNER"));
                if (!hasOwnerRole) {
                    user.getRoles().add(ownerRole);
                    userRepository.save(user);
                }
            }

            List<GrantedAuthority> authorities = user.getRoles().stream()
                    .map(role -> new SimpleGrantedAuthority(role.getName()))
                    .collect(Collectors.toList());

            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(user.getUsername(), null, authorities);
            SecurityContextHolder.getContext().setAuthentication(authToken);

            String token = jwtGenerator.generateToken(authToken, user.getId(), user.getRoles().stream().map(Role::getName).toList());
            return ResponseEntity.ok(new AuthResponseDto(user.getId(), token));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token: " + e.getMessage());
        }
    }
    @GetMapping("/username/{username}/preferred-language")
    public ResponseEntity<String> getPreferredLanguage(@PathVariable String username) {
        return userRepository.findByUsername(username)
                .map(user -> ResponseEntity.ok(user.getPreferred_language()))
                .orElse(ResponseEntity.ok("English"));
    }
    @GetMapping("/userData/{id}")
    public ResponseEntity<UserDataDto> getUserDataById(
            @PathVariable UUID id,
            @RequestHeader("Authorization") String token
    ) {
        Optional<User> user = userRepository.findById(id);
        return user.map(value -> ResponseEntity.ok(new UserDataDto(value)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

}