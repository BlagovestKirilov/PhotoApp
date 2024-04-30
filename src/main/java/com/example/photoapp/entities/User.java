package com.example.photoapp.entities;

import com.example.photoapp.enums.RegistrationStatusEnum;
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

    @OneToOne
    private Photo profilePhoto;

    @ManyToMany
    @JoinTable(
            name = "user_friends",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "friend_id")
    )
    private List<User> friendList;

    @Enumerated(EnumType.STRING)
    private RegistrationStatusEnum registrationStatus;

    public User(String name, String email, String password, List<Role> roles, List<Photo> photos, List<User> friendList, RegistrationStatusEnum registrationStatus, Photo profilePhoto) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.roles = roles;
        this.photos = photos;
        this.friendList = friendList;
        this.registrationStatus = registrationStatus;
        this.profilePhoto = profilePhoto;
    }
}
