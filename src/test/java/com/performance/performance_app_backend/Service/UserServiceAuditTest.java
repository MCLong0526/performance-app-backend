package com.performance.performance_app_backend.Service;

import com.performance.performance_app_backend.Entity.User;
import com.performance.performance_app_backend.Enum.Role;
import com.performance.performance_app_backend.Repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

class UserServiceAuditTest {

    @Mock
    private UserRepository repo;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private AutoCloseable mocks;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        org.springframework.test.util.ReflectionTestUtils.setField(userService, "passwordEncoder", passwordEncoder);
        when(passwordEncoder.encode(any())).thenReturn("encodedPassword");
    }

    @AfterEach
    void tearDown() throws Exception {
        mocks.close();
        SecurityContextHolder.clearContext();
    }

    @Test
    void create_shouldSetCreatedBy_fromSecurityContext() {
        // Arrange
        String currentUserEmail = "admin@example.com";
        String currentUserName = "Admin User";
        User adminUser = new User();
        adminUser.setEmail(currentUserEmail);
        adminUser.setName(currentUserName);
        adminUser.setRole(Role.ADMIN);

        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(adminUser);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);

        User newUser = new User();
        newUser.setName("New User");
        newUser.setEmail("new@example.com");
        newUser.setPassword("password");

        when(repo.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        User createdUser = userService.create(newUser);

        // Assert
        assertEquals(currentUserName, createdUser.getCreatedBy());
    }

    @Test
    void update_shouldSetUpdatedBy_fromSecurityContext() {
        // Arrange
        String currentUserEmail = "updater@example.com";
        String currentUserName = "Updater User";
        User updaterUser = new User();
        updaterUser.setEmail(currentUserEmail);
        updaterUser.setName(currentUserName);
        updaterUser.setRole(Role.PROGRAMMER);

        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(updaterUser);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);

        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setName("Existing");
        existingUser.setEmail("existing@example.com");
        existingUser.setDeleted(false);

        when(repo.findById(1L)).thenReturn(Optional.of(existingUser));
        when(repo.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User updateData = new User();
        updateData.setName("Updated Name");

        // Act
        User updatedUser = userService.update(1L, updateData);

        // Assert
        assertEquals(currentUserName, updatedUser.getUpdatedBy());
    }

    @Test
    void create_shouldKeepSelfRegister_whenAnonymous() {
        // Arrange
        // Simulate Anonymous User (or no authentication)
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(null);
        SecurityContextHolder.setContext(securityContext);

        User newUser = new User();
        newUser.setName("Self Register User");
        newUser.setEmail("self@example.com");
        newUser.setPassword("password");
        newUser.setCreatedBy("SELF_REGISTER");

        when(repo.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        User createdUser = userService.create(newUser);

        // Assert
        assertEquals("SELF_REGISTER", createdUser.getCreatedBy());
    }
}
