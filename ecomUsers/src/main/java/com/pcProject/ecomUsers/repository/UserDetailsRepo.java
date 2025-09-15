package com.pcProject.ecomUsers.repository;

import com.pcProject.ecomUsers.model.UserDetails;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserDetailsRepo extends JpaRepository<UserDetails,String> {
    UserDetails findByUserName(String userName);

    @Modifying
    @Transactional
    @Query(value = "Delete from UserDetails where userName =:userName")
    void deleteByUserName(String userName);
}
