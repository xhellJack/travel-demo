package com.example.travel.repository;

import com.example.travel.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository; // Ensure @Repository is here

import java.util.Optional;

@Repository // Add @Repository if it's missing
public interface TagRepository extends JpaRepository<Tag, Long> {
    Optional<Tag> findByName(String name);
    boolean existsByName(String name);
    boolean existsByNameAndIdNot(String name, Long id); // For update validation
}