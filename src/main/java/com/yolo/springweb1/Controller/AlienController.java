package com.yolo.springweb1.Controller;
import com.yolo.springweb1.Alien;
import com.yolo.springweb1.Service.AlienService;
import com.yolo.springweb1.Service.UserService;
import com.yolo.springweb1.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/aliens")
public class AlienController
{
    @Autowired
    private AlienService service;
    @Autowired
    private UserService userService;
    
    @GetMapping("/all")
    public List<Alien> getAll()
    {
        return service.getAll();
    }
    @GetMapping("/user/{username}")

    public ResponseEntity <?> getAllAlienEntriesofUser(@PathVariable String username)
    {
        User user = userService.findByUsername(username);
        if (user==null)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        List<Alien> all = user.getAliens();

        return ResponseEntity.ok(all);
    }

    @GetMapping("id/{id}")
    public Optional<Alien> getId(@PathVariable String id)
    {
        return service.findByid(id);
    }

    @PostMapping("/user/{username}")
    public ResponseEntity<Alien> cloud(@RequestBody Alien aliens, @PathVariable String username)
    {
        try {
            service.saveEntry(aliens, username);
            return new ResponseEntity<>(aliens, HttpStatus.CREATED);
        }
        catch (IllegalArgumentException e)
        {
            // User not found
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        catch (Exception e)
        {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable String id)
    {
      service.deleteById(id);
      return "Deleted";
    }

    @DeleteMapping("/all")
    public void deleteAll()
    {
        service.deleteAll();
    }

    //update if existing or create if it doesn't exist
    @PutMapping("/update/{id}")
    public ResponseEntity<?> update(@PathVariable String id, @RequestBody Alien updatedalien)
    {
        updatedalien.setId(id);
        Alien saved = service.saveUpdates(updatedalien);
        return ResponseEntity.ok(saved);
    }
    @PostMapping("/post")
    public ResponseEntity<Alien> postUser(@RequestBody Alien newalien)
    {
        Alien saved = service.saveUser(newalien);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

}