package com.example.photoapp.repository;

import com.example.photoapp.entity.Page;
import com.example.photoapp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface PageRepository extends JpaRepository<Page, Long> {
    List<Page> findAllByOwner(User user);

    Page findAllByPageName(String name);

    List<Page> findAllByOwnerNot(User user);

    List<Page> findAllByLikedPageUsersContains(User user);
}
