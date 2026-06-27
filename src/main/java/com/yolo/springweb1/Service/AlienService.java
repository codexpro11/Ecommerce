package com.yolo.springweb1.Service;

import com.yolo.springweb1.Alien;
import com.yolo.springweb1.Reposetry.Repository;
import com.yolo.springweb1.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class AlienService
{
  @Autowired
  private Repository repository;

  @Autowired
  PasswordEncoder passwordEncoder;

  @Autowired
  private UserService userService;

  public Alien saveUpdates(Alien alien)
  {
    return repository.save(alien);
  }

  public List<Alien> getAll()
  {
    return repository.findAll();
  }

  public Optional<Alien> findByid(String id)
  {
    if (id == null) return Optional.empty();
    return repository.findById(id);
  }

  public void deleteById(String id)
  {
    if (id == null) return;
    repository.deleteById(id);
  }

  public void deleteAll()
  {
    repository.deleteAll();
  }

  @Transactional
  public void saveEntry(Alien alien, String username)
  {
    User user = userService.findByUsername(username);
    if (user == null) {
      throw new IllegalArgumentException("User not found with username: " + username);
    }
    user.setPassword(passwordEncoder.encode(user.getPassword()));
    Alien saved = repository.save(alien);
    user.getAliens().add(saved);
    userService.saveEntry(user);
  }

  public Alien saveUser(Alien alien)
  {
    return repository.save(alien);
  }
}
