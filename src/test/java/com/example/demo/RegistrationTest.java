package com.example.demo;

import com.example.demo.models.User;
import com.example.demo.repository.UserRepo;
import com.example.demo.services.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
public class RegistrationTest extends TestContainers{

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepo userRepo;

    private User registrationUser(String username, String password) throws Exception {
        User user = User.builder().username(username).password(password).build();
        userService.addUser(user);
        return user;
    }

    @Test
    public void registrationTest() throws Exception {
        String username = "RegistrationName";
        String password = "RegistrationPassword";
        User user = registrationUser(username, password);
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        assertNotNull(user.getId());
        assertFalse(password.equals(user.getPassword()));
        assertTrue(encoder.matches(password, user.getPassword()));
    }

    @Test
    public void authorizationTest() throws Exception {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String username = "AuthorizationName";
        String password = "AuthorizationPassword";
        User user = registrationUser(username, password);
        User userFromDB = userRepo.findByUsername(username);
        assertNotNull(userFromDB);
        assertEquals(user.getId(), userFromDB.getId());
        assertEquals(user.getUsername(), userFromDB.getUsername());
        assertEquals(user.getPassword(), userFromDB.getPassword());
        assertTrue(encoder.matches(password, user.getPassword()));
    }

}
