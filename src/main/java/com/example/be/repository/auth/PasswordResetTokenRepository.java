package com.example.be.repository.auth;

import com.example.be.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByToken(String token);

    @Modifying
    @Query("""
        UPDATE PasswordResetToken t
        SET t.used = true, t.usedAt = CURRENT_TIMESTAMP
        WHERE t.user.id = :userId AND t.used = false
    """)
    int invalidateAllActiveTokens(@Param("userId") Long userId);
}
