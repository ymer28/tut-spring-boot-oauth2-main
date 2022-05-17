package com.example;
import javax.persistence.*;



//@Entity: This annotation allows our class to be serialized and deserialized into and from JSON.
// It also allows us to create a table in the database we created earlier.
@Entity
//This annotation tells the program to call the table “users”.
@Table(name="tableeeeeee")
public class User {
    @Id
    //@GeneratedValue(strategy = GenerationType.IDENTITY)
    //IDENTITY: In this case database is responsible for determining and assigning the next primary key.
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer uid;

    @Column
    private String email;

    @Column(name = "name")
    private String name;

    public User() {}
    public User(String email, String name) {
        super();
        this.email = email;
        this.name = name;
    }

    public Integer getId() {
        return uid;
    }

    public void setId(Integer uid) {
        this.uid = uid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
