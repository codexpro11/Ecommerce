package com.yolo.springweb1.Reposetry;
import com.yolo.springweb1.User;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends MongoRepository<User, String>
{
    User findByUsername(String username);

    User deleteByUsername(String username);
}
