package com.yolo.springweb1;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document
public class Alien
{
    @Id
    private String id;       // String → MongoDB auto-generates ObjectId
    private String name;
    private String title;
    private Date date;

}

