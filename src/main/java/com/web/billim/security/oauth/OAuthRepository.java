package com.web.billim.security.oauth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OAuthRepository extends JpaRepository<SocialMember,Long> {
}
