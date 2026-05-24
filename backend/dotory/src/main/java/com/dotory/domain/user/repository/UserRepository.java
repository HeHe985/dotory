package com.dotory.domain.user.repository;

import com.dotory.domain.user.entity.SocialProvider;
import com.dotory.domain.user.entity.User;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);


    Optional<User> findByProviderAndProviderId(
            SocialProvider provider,
            String providerId
    );
}
