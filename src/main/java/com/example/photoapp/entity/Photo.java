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
public class Photo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fileName;

    @OneToOne
    private User user;

    @ManyToMany
    @JoinTable(
            name = "user_likes_photo",
            joinColumns = @JoinColumn(name = "photo_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    List<User> likedPhotoUsers;

    @OneToMany(mappedBy = "photo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PhotoComment> comments;

    private String status;

    @PreRemove
    public void preRemove() {
        // Perform auditing tasks here, such as logging the deletion
        // You can access entity fields and perform any necessary actions
        // For example, you can log who deleted the entity and when
        // You can also archive the entity instead of permanently deleting it
        System.out.println("Entity deleted: " + this.toString());
    }

}
