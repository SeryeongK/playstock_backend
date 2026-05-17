package com.playstock.user;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String nickname;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @Column(name = "point_balance", nullable = false)
    private Long pointBalance;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    private void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (this.pointBalance == null) {
            this.pointBalance = 0L;
        }
    }

    public static User create(String email, String nickname, String passwordHash, UserRole role) {
        User user = new User();
        user.email = email;
        user.nickname = nickname;
        user.passwordHash = passwordHash;
        user.role = role;
        return user;
    }
}
