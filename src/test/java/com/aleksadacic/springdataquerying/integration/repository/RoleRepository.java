package com.aleksadacic.springdataquerying.integration.repository;

import com.aleksadacic.springdataquerying.integration.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

@SuppressWarnings("unused")
public interface RoleRepository extends JpaRepository<Role, Long> {
    // Additional query methods can be defined here if needed
}
