package com.biletapp.biletapp.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.biletapp.biletapp.model.RefundRequest;
import com.biletapp.biletapp.model.User;

public interface RefundRequestRepository extends JpaRepository<RefundRequest, Long> {
    List<RefundRequest> findByUser(User user);
    int countByRequestDateBetween(LocalDateTime start, LocalDateTime end);

}
