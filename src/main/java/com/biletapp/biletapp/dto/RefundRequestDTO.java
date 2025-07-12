package com.biletapp.biletapp.dto;

import lombok.Data;

@Data
public class RefundRequestDTO {
    private Long biletId;
    private String reason;
}
