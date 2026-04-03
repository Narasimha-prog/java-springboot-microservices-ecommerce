package com.eswar.paymentservice.repository;

import com.eswar.paymentservice.entity.EventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface IEventRepository extends JpaRepository<EventEntity, UUID> {
}
