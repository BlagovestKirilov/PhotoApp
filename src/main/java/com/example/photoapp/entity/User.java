package com.example.photoapp.entity;

import com.example.photoapp.enums.RegistrationStatusEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.List;
import java.util.Set;

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

    @ManyToOne()
    private Role role;

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

    @Column
    private String country;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserBan> userBans;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Notification> notifications;

    @OneToMany(mappedBy = "sender")
    private Set<ChatMessage> sentMessages;

    @OneToMany(mappedBy = "recipient")
    private Set<ChatMessage> receivedMessages;

    @Column
    private String education;

    @Column
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date birtdate;
    public User(String name, String email, String country, String password, Role role,List<User> friendList, RegistrationStatusEnum registrationStatus, Photo profilePhoto) {
        this.name = name;
        this.email = email;
        this.country = country;
        this.password = password;
        this.role = role;
        this.friendList = friendList;
        this.registrationStatus = registrationStatus;
        this.profilePhoto = profilePhoto;
    }
}
