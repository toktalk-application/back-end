package com.springboot.counselor.repository;

import com.springboot.counselor.entity.Counselor;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CounselorRepository extends JpaRepository<Counselor, Long> {
    @EntityGraph(attributePaths = {"roles"})
    Optional<Counselor> findByUserId(String userId);
}
