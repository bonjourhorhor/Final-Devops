package com.example.demo.repository;

import com.example.demo.model.Template;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface TemplateRepository extends JpaRepository<Template, Long> {
    
    Optional<Template> findByName(String name);
    
    Optional<Template> findByCode(String code);
    
    boolean existsByName(String name);
    
    boolean existsByCode(String code);
    
    List<Template> findByActive(Boolean active);
    
    @Query("SELECT t FROM Template t WHERE t.name LIKE %:keyword% OR t.code LIKE %:keyword% OR t.organizationName LIKE %:keyword%")
    List<Template> searchTemplates(@Param("keyword") String keyword);
}