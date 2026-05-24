package com.dotory.domain.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @UuidGenerator
    private UUID id;

    @Enumerated(EnumType.STRING)
    private UserRole role;

    @Column(unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    private String nickname;

    private String name;

    private String phone;

    private String profileImageUrl;

    @Enumerated(EnumType.STRING)
    private SocialProvider provider;

    private String providerId;

    @Builder
    private User(String email, String password, String name, SocialProvider provider, String providerId) {
        this.role = UserRole.USER;
        this.email = email;
        this.password = password;
        this.name = name;
        this.provider = provider;
        this.providerId = providerId;
    }
}
