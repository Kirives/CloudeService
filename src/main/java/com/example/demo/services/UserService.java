package com.example.demo.services;

import com.example.demo.models.User;
import com.example.demo.models.security.UsersDetails;
import com.example.demo.repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepo userRepo;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepo.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException(username);
        }
        return new UsersDetails(user);
    }

    public String addUser(User user) throws Exception{
        user.setPassword(new BCryptPasswordEncoder().encode(user.getPassword()));
        try {
            userRepo.save(user);
        }catch (Exception e){
            if(e instanceof DataIntegrityViolationException){
                throw new Exception("Такой логин уже используется",e);
            }
        }
        return user.getUsername();
    }
}
