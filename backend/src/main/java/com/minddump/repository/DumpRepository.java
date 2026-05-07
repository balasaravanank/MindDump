package com.minddump.repository;

import com.minddump.model.Dump;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DumpRepository extends JpaRepository<Dump, Long> {
    List<Dump> findAllByOrderByCreatedAtDesc();
}
