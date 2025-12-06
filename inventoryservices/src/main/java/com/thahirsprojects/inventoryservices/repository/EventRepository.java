package com.thahirsprojects.inventoryservices.repository;

import com.thahirsprojects.inventoryservices.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRepository extends JpaRepository<Event, Long> {
}
