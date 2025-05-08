package com.ecommerce.project.controller;

import com.ecommerce.project.exceptions.APIException;
import com.ecommerce.project.model.Cart;
import com.ecommerce.project.payload.CartDTO;
import com.ecommerce.project.repositories.CartRepository;
import com.ecommerce.project.service.CartService;
import com.ecommerce.project.utils.AuthUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api")
public class CartController {

    // logged in users
    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartService cartService;

    @Autowired
    AuthUtil authUtil;
    // Add product to cart

    @PostMapping("/carts/products/{productId}/quantity/{quantity}")
    public ResponseEntity<CartDTO> addProductToCart(@PathVariable Long productId, @PathVariable Integer quantity) {

        log.info("Request received : Add Product to cart  , product Id {} with quantity {}", productId, quantity);
        CartDTO cartDTO = cartService.addProductToCart(productId, quantity);

        log.info("Product added successfully {}", cartDTO);

        return new ResponseEntity<>(cartDTO, HttpStatus.CREATED);
    }

    //get all carts

    // should only for admin

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/carts")
    public ResponseEntity<List<CartDTO>> getAllCarts() {
        log.info("Request received : Fetch all carts");

        List<CartDTO> list = cartService.getAllCarts();

        log.info("fetched all the carts successfully {} ", list.toString());
        return new ResponseEntity<>(list, HttpStatus.OK);

    }

    //get user's cart
    @GetMapping("/carts/users/cart")
    public ResponseEntity<CartDTO> getCartById() {
        String email = authUtil.loggedInEmail();


        log.info("Request received : get cart by ID {}", email);

        Cart cart = cartRepository.findCartByEmail(email);
        if (cart == null) {

            log.warn("No cart exists for this  : " + email);
            throw new APIException("No cart exists for this  : " + email);

        }
        Long cartId = cart.getCartId();
        CartDTO cartDTO = cartService.getCart(email, cartId);

        log.info("Cart fetched successfully {}", cartDTO);

        return new ResponseEntity<>(cartDTO, HttpStatus.OK);
    }
    //update product quantity

    @PutMapping("/cart/products/{productId}/quantity/{operation}")
    public ResponseEntity<CartDTO> updateCartProduct(
            @PathVariable Long productId,
            @PathVariable String operation
    ) {
        log.info("Request received : Update the product into cart {} with operation {} ", productId, operation);

        CartDTO cartDTO = cartService.updateProductQuantityInCart(productId, operation);

        log.info("Product successfully updated into the cart {}", cartDTO);
        return new ResponseEntity<>(cartDTO, HttpStatus.OK);
    }


    //Delete the product from cart
    @DeleteMapping("/carts/{cartId}/product/{productId}")
    public ResponseEntity<String> deleteProduct(@PathVariable Long cartId, @PathVariable Long productId) {

        log.info("Request received to delete product from cart with cart Id  {} and product Id  {}", cartId, productId);

        String status = cartService.deleteProductFromCart(cartId, productId);

        log.info("Product successfully removed from cart {}", status);
        return new ResponseEntity<>(status, HttpStatus.OK);
    }
}
