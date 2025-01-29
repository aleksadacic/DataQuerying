package com.aleksadacic.dataquerying.integration.repository;

import com.aleksadacic.dataquerying.integration.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

@SuppressWarnings("unused")
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    // Additional query methods can be defined here if needed
}
