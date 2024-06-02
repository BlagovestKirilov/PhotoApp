package com.example.photoapp.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Page {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String pageName;

    @OneToOne
    private User owner;

    @OneToOne
    private Photo profilePhoto;

    @ManyToMany
    @JoinTable(
            name = "user_likes_page",
            joinColumns = @JoinColumn(name = "page_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    List<User> likedPageUsers;
}
