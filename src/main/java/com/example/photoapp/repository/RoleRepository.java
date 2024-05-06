package com.example.photoapp.repository;


import com.example.photoapp.entity.Role;
import com.example.photoapp.enums.RoleEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Role findByName(RoleEnum name);
}