package com.yolo.springweb1.Controller;
import com.yolo.springweb1.Service.UserService;
import com.yolo.springweb1.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RequestMapping("/users")
@RestController
public class UserController
{
    @Autowired
    private UserService userService;

@GetMapping()
    public  List<User> getAll()
{
 return userService.getAll();
}

@PostMapping()
    public User post(@RequestBody User users)
{
   return userService.save(users);
}
@PutMapping("/update/{username}")
public ResponseEntity<User> updates(@RequestBody User updatedUser, @PathVariable String username)
{
    // Use path variable (current username) to find the existing user
    User existingUser = userService.findByUsername(username);
    if (existingUser != null)
    {
        existingUser.setUsername(updatedUser.getUsername());
        existingUser.setPassword(updatedUser.getPassword());
        return ResponseEntity.ok(userService.save(existingUser));
    }
    return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
}

@DeleteMapping("/delete/{username}")
public User deleteByUsername(@PathVariable String username)
{
    return userService.deleteByusername(username);
}

@DeleteMapping("/deletall")
public String deleteAll()
{
    userService.deleteAll();
  return "DELETED SUCCESSFULLY";
}
}


