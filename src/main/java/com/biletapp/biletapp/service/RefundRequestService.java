package com.biletapp.biletapp.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.biletapp.biletapp.model.Bilet;
import com.biletapp.biletapp.model.RefundRequest;
import com.biletapp.biletapp.model.RefundRequest.RefundStatus;
import com.biletapp.biletapp.model.User;
import com.biletapp.biletapp.repository.BiletRepository;
import com.biletapp.biletapp.repository.RefundRequestRepository;
import com.biletapp.biletapp.repository.UserRepository;

@Service
public class RefundRequestService {

    @Autowired
    private RefundRequestRepository refundRepo;

    @Autowired
    private BiletRepository biletRepo;

    @Autowired
    private UserRepository userRepo;

    public RefundRequest createRefundRequest(Long biletId, String reason, String username) {
        User user = userRepo.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));

        Bilet bilet = biletRepo.findById(biletId)
            .orElseThrow(() -> new RuntimeException("Bilet not found"));

        if (!bilet.getKullanici().getId().equals(user.getId())) {
            throw new RuntimeException("This ticket does not belong to you!");
        }

        RefundRequest request = new RefundRequest();
        request.setBilet(bilet);
        request.setUser(user);
        request.setReason(reason);
        request.setStatus(RefundStatus.PENDING);

        return refundRepo.save(request);
    }

    public List<RefundRequest> getRefundRequestsByUser(String username) {
        User user = userRepo.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));
        return refundRepo.findByUser(user);
    }
}
