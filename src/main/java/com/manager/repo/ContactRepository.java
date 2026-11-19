package com.manager.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.manager.model.Contact;
import com.manager.model.User;

public interface ContactRepository extends JpaRepository<Contact, Integer> {
   // Pagination is implemented

   @Query("from Contact c where c.user.id = :userId")
   // This Pageable object contains two things: current page number {page} and
   // number of contacts per page
   public Page<Contact> findByUserId(@Param("userId") int userId, Pageable pageable);

   @Query("from Contact c where c.user.id = :userId")
   public List<Contact> findByUserId(@Param("userId") int userId);

   Contact findByPhoneAndUser(String phone, User user);

   @Query("""
             SELECT c
             FROM Contact c
             WHERE c.cId = :cId
               AND c.user.id = :userId
         """)
   Optional<Contact> findByCIdAndUser_Id(
         @Param("cId") int cId,
         @Param("userId") int userId);

   long countByUser_Id(int userId);

   @Query("""
             SELECT c
             FROM Contact c
             WHERE c.user.id = :userId
               AND (
                     LOWER(c.name) LIKE CONCAT('%', :keyword, '%')
                  OR LOWER(c.email) LIKE CONCAT('%', :keyword, '%')
                  OR LOWER(c.phone) LIKE CONCAT('%', :keyword, '%')
               )
         """)
   List<Contact> searchContacts(int userId, String keyword);
}
