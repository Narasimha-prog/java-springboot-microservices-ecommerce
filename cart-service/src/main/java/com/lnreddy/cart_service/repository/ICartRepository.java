package com.lnreddy.cart_service.repository;

import com.lnreddy.cart_service.entity.CartEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ICartRepository extends JpaRepository<CartEntity, UUID> {
}
