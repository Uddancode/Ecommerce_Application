package com.ecommerce.project.repositories;

import com.ecommerce.project.model.Cart;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    @Query("SELECT c FROM Cart c WHERE c.user.email=?1")
    Cart findCartByEmail(String loggedInEmail);

    @Query("SELECT c FROM Cart c WHERE c.user.email=?1 AND c.id=?2")
    Cart findCartByEmailAndCartId(String email, Long cartId);

    @Transactional
    @Modifying
    @Query("DELETE FROM Cart c WHERE c.id=?1")
    void deleteByCartId(Long cartId);


}
