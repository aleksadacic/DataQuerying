package io.github.aleksadacic.dataquerying.integration.repository;

import io.github.aleksadacic.dataquerying.integration.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

@SuppressWarnings("unused")
public interface RoleRepository extends JpaRepository<Role, Long> {
    // Additional query methods can be defined here if needed
}
