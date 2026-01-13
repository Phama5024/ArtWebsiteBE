package com.example.be.dto.user;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public interface AdminUserListRowDTO {
    Long getId();
    String getEmail();
    String getFullName();
    String getPhone();
    String getAddress();

    String getGender();
    LocalDate getDateOfBirth();
    String getAvatarUrl();

    Boolean getIsVerified();
    String getStatus();
    LocalDateTime getCreatedAt();

    Long getOrdersCount();
    BigDecimal getTotalSpent();
}
