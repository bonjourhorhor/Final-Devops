package com.example.demo.repository;

import com.example.demo.model.Profile;
import com.example.demo.model.ProfileType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProfileRepository extends JpaRepository<Profile, Long> {
    
    Optional<Profile> findByRegistrationNumber(String registrationNumber);

    Optional<Profile> findByUuid(String uuid);
    
    boolean existsByRegistrationNumber(String registrationNumber);

    List<Profile> findByType(ProfileType type);

    @Query("SELECT p FROM Profile p WHERE p.fullName LIKE %:keyword% OR p.registrationNumber LIKE %:keyword%")
    List<Profile> searchProfiles(@Param("keyword") String keyword);
}