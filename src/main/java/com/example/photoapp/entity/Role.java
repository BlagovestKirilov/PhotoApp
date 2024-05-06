package com.example.photoapp.entity;

import com.example.photoapp.enums.RoleEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    @Enumerated(EnumType.STRING)
    private RoleEnum name;
    @OneToMany(mappedBy = "role")
    private Set<User> users = new HashSet<>();

    public Role(RoleEnum name) {
        this.name = name;
    }
}
