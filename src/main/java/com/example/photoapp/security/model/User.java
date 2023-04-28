package com.example.photoapp.security.model;

import com.example.photoapp.entities.Photo;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false, unique = true)
    private String email;
    @Column(nullable = false)
    private String password;
    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
    @JoinTable(
            name = "users_roles",
            joinColumns = {@JoinColumn(name = "user_id", referencedColumnName = "id")},
            inverseJoinColumns = {@JoinColumn(name = "role_id", referencedColumnName = "id")}
    )
    private List<Role> roles = new ArrayList<>();

    @OneToMany
    private List<Photo> photos = new ArrayList<>();

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
    @JoinTable(
            name = "users_friends",
            joinColumns = {@JoinColumn(name = "user_id", referencedColumnName = "id")},
            inverseJoinColumns = {@JoinColumn(name = "friends_id", referencedColumnName = "id")}
    )
    private List<User> friends;

    public User(String name, String email, String password, List<Role> roles, List<Photo> photos, List<User> friends) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.roles = roles;
        this.photos = photos;
        this.friends = friends;
    }

    //    public User(String name, String email, String password, List<Role> roles) {
//        this.name = name;
//        this.email = email;
//        this.password = password;
//        this.roles = roles;
//        this.friends = new ArrayList<>();
//    }
}
