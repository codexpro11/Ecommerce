package com.yolo.springweb1;

import lombok.Data;
import lombok.NonNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Data
@Document(collection="users")
public class User
{
    @Id
private String id;

    @NonNull
    @Indexed(unique = true)
    private String username;

    @NonNull
    private String password;

    @DBRef
   private List<Alien> aliens = new ArrayList<>();
    private List<String> roles = new ArrayList<>();
}