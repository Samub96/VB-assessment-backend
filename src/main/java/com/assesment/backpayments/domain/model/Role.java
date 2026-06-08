package com.assesment.backpayments.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Representa un rol asignable a usuarios para controlar permisos.
 */
@Entity
@Table(name = "roles")
public class Role {

    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = "name", length = 32, nullable = false)
    private RoleName name;

    public Role() {
    }

    public Role(RoleName name) {
        this.name = name;
    }

    public RoleName getName() {
        return name;
    }

    public void setName(RoleName name) {
        this.name = name;
    }
}
