package com.assesment.backpayments.infrastructure.storage;

import com.assesment.backpayments.domain.model.Role;
import com.assesment.backpayments.domain.model.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio de roles para control de acceso.
 */
public interface RoleRepository extends JpaRepository<Role, RoleName> {
}
