package com.yolo.springweb1.Service;

import com.yolo.springweb1.Reposetry.UserRepository;
import com.yolo.springweb1.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserDetailsService implements org.springframework.security.core.userdetails.UserDetailsService
{
    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println(">>> loadUserByUsername called for: " + username);
        User user = userRepository.findByUsername(username);
        if (user != null)
        {
            System.out.println(">>> User found in DB: " + user.getUsername());
            System.out.println(">>> Stored password hash: " + user.getPassword());
            System.out.println(">>> Roles in DB: " + user.getRoles());

            // Guard against null/empty roles — default to "USER" if not set
            List<String> roles = (user.getRoles() != null && !user.getRoles().isEmpty())
                    ? user.getRoles()
                    : List.of("USER");

            System.out.println(">>> Using roles: " + roles);

           UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                    .username(user.getUsername())
                    .password(user.getPassword())
                    .roles(roles.toArray(new String[0]))
                    .build();
           return userDetails;
        }
        System.out.println(">>> USER NOT FOUND for username: " + username);
        throw new UsernameNotFoundException("user not found" + username);
    }
}
