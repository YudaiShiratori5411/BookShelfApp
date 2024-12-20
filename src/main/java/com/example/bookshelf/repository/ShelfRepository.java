package com.example.bookshelf.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.bookshelf.entity.Shelf;

public interface ShelfRepository extends JpaRepository<Shelf, Long> {
    Optional<Shelf> findByName(String name);
    List<Shelf> findAllByOrderByDisplayOrderAsc();
}