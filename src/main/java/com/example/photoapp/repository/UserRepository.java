package com.example.photoapp.repository;

import com.example.photoapp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findByEmail(String email);
    User findByName(String name);
   @Query("SELECT u FROM User u WHERE u.email <> :userEmail " +
           "AND u.role.name <> 'ROLE_ADMIN' "+
           "AND u.registrationStatus = 'CONFIRMED' "+
           "AND NOT EXISTS (SELECT f FROM u.friendList f WHERE f.email = :userEmail)")
    List<User> findNonFriendUsersByUser(@Param("userEmail") String userEmail);

    @Query("SELECT u FROM User u WHERE u.email <> :userEmail " +
            "AND u.role.name <> 'ROLE_ADMIN' "+
            "AND u.registrationStatus = 'CONFIRMED' "+
            "AND u.country = (SELECT c.country FROM User c WHERE c.email = :userEmail) "+
            "AND NOT EXISTS (SELECT f FROM u.friendList f WHERE f.email = :userEmail)")
    List<User> findNonFriendUsersByUserAndCountry(@Param("userEmail") String userEmail);

    @Query("SELECT u.friendList FROM User u WHERE u.email = :email")
    List<User> findFriendsByEmail(@Param("email") String email);
}
