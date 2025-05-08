package com.ecommerce.project.service;

import com.ecommerce.project.payload.CartDTO;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface CartService {

    CartDTO addProductToCart(Long productId, Integer quantity);

    List<CartDTO> getAllCarts();

    CartDTO getCart(String email, Long cartId);

    @Transactional
    CartDTO updateProductQuantityInCart(Long productId, String operation);

    @Transactional
    String deleteProductFromCart(Long cartId, Long productId);


}
