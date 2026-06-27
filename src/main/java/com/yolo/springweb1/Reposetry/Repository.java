package com.yolo.springweb1.Reposetry;

import com.yolo.springweb1.Alien;
import com.yolo.springweb1.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

@org.springframework.stereotype.Repository
public interface Repository extends MongoRepository <Alien, String>
{
}
