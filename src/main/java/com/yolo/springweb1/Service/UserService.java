package com.yolo.springweb1.Service;

import com.yolo.springweb1.Reposetry.UserRepository;
import com.yolo.springweb1.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService
{
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User findByUsername(String username)
    {
        return userRepository.findByUsername(username);
    }

    public List<User> getAll()
    {
        return userRepository.findAll();
    }

    public List<User> saveAll(List<User> users)
    {
        return userRepository.saveAll(users);
    }

    public User save(User user)
    {
        if (user == null) throw new IllegalArgumentException("User cannot be null");
        // Encode password before saving so BCrypt comparison works at login
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public User saveEntry(User user)
    {
        if (user == null) throw new IllegalArgumentException("User cannot be null");
        return userRepository.save(user);
    }

    public void deleteAll()
    {
        userRepository.deleteAll();
    }

    public User deleteByusername(String username)
    {
       return userRepository.deleteByUsername(username);
    }
}
