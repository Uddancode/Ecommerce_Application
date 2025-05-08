package com.ecommerce.project.service;

import com.ecommerce.project.exceptions.APIException;
import com.ecommerce.project.exceptions.ResourceNotFoundException;
import com.ecommerce.project.model.Cart;
import com.ecommerce.project.model.CartItem;
import com.ecommerce.project.model.Product;
import com.ecommerce.project.model.User;
import com.ecommerce.project.payload.CartDTO;
import com.ecommerce.project.payload.ProductDTO;
import com.ecommerce.project.repositories.CartItemRepository;
import com.ecommerce.project.repositories.CartRepository;
import com.ecommerce.project.repositories.ProductRepository;
import com.ecommerce.project.utils.AuthUtil;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
public class CartServiceImpl implements CartService {


    @Autowired
    private AuthUtil authUtil;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    ModelMapper modelMapper;

    @Override
    @Transactional

    public CartDTO addProductToCart(Long productId, Integer quantity) {
        log.info("Attempting to add new product with ID  {} to Cart with quantity {}", productId, quantity);

        // Get or create cart
        Cart cart = createCart();


        Product product = productRepository.findById(productId)
                .orElseThrow(() ->

                {

                    log.warn("Product not found with this product Id : {}", productId);
                    return new ResourceNotFoundException("Product", "productId", productId);
                });

        CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId(cart.getCartId(), productId);

        if (cartItem != null) {
            log.warn("Product " + product.getProductName() + " already exists in the cart");
            throw new APIException("Product " + product.getProductName() + " already exists in the cart");
        }

        if (product.getQuantity() == 0) {

            log.warn(product.getProductName() + " is not available");
            throw new APIException(product.getProductName() + " is not available");
        }

        if (product.getQuantity() < quantity) {

            log.warn("Please order " + product.getProductName()
                    + " in a quantity less than or equal to " + product.getQuantity());
            throw new APIException("Please order " + product.getProductName()
                    + " in a quantity less than or equal to " + product.getQuantity());
        }

        CartItem newCartItem = new CartItem();
        newCartItem.setProduct(product);
        newCartItem.setCart(cart);
        newCartItem.setQuantity(quantity);
        newCartItem.setDiscount(product.getDiscount());
        newCartItem.setProductPrice(product.getPrice());


        cart.getCartItems().add(newCartItem); // Ensure cart references cart items


        cartItemRepository.save(newCartItem);

        // Deduct product quantity and save
        product.setQuantity(product.getQuantity() - quantity);
        productRepository.save(product);

        // Ensure total price is not null
        cart.setTotalPrice((cart.getTotalPrice() != null ? cart.getTotalPrice() : 0)
                + (product.getSpecialPrice() * quantity));

        cartRepository.save(cart);

        CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);

        // Ensure cart items are initialized
        List<CartItem> cartItems = cart.getCartItems() != null ? cart.getCartItems() : new ArrayList<>();

        List<ProductDTO> productDTOList = cartItems.stream().map(item -> {
            ProductDTO map = modelMapper.map(item.getProduct(), ProductDTO.class);
            map.setQuantity(item.getQuantity());
            return map;
        }).collect(Collectors.toList());

        cartDTO.setProducts(productDTOList);

        log.info("Product address successfully  to cart {}", cartDTO);
        return cartDTO;
    }

    @Override
    public List<CartDTO> getAllCarts() {

        log.info("Attempting to fetch all the carts from Database ");

        List<Cart> carts = cartRepository.findAll();
        if (carts.size() == 0) {
            log.warn("No cart exists");
            throw new APIException("No cart exists");
        }

        List<CartDTO> cartDTOS = carts.stream().map(eachCart -> {

            CartDTO cartDTO = modelMapper.map(eachCart, CartDTO.class);

            List<ProductDTO> productsDTO = eachCart.getCartItems().stream().map(eachCartItem -> {
                ProductDTO productDTO = modelMapper.map(eachCartItem.getProduct(), ProductDTO.class);
                productDTO.setQuantity(eachCartItem.getQuantity());// set the quantity from cart item
                return productDTO;
            }).toList();

            cartDTO.setProducts(productsDTO);
            return cartDTO;
        }).toList();

        log.info("All carts fetched successfully  with Cart size {}", cartDTOS.size());

        return cartDTOS;
    }

    @Override
    public CartDTO getCart(String email, Long cartId) {

        log.info("Attempting to Fetch cart for this user , {} ,  {}", email, cartId);

        Cart cart = cartRepository.findCartByEmailAndCartId(email, cartId);

        if (cart == null) {
            log.warn("Cart not found for this cart Id {}", cartId);
            throw new ResourceNotFoundException("Cart", "CartId", cartId);
        }
        CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);

        cart.getCartItems().forEach(c -> c.getProduct().setQuantity(c.getQuantity()));
        List<ProductDTO> productDTOS =
                cart.getCartItems()
                        .stream()
                        .map(cartItem ->
                                modelMapper.map(cartItem.getProduct(), ProductDTO.class))
                        .toList();
        cartDTO.setProducts(productDTOS);


        log.info("Cart fetched successfully for this user {}, cart {}", email, cartDTO);
        return cartDTO;
    }

    @Transactional
    @Override
    public CartDTO updateProductQuantityInCart(Long productId, String operation) {

        log.info("Attempting to update the products in the cart  product Id {}, operation {}", productId, operation);

        Integer quantity = operation.equalsIgnoreCase("delete") ? -1 : 1;
        log.info("quantity {}", quantity);

        // perform operation to update
        String email = authUtil.loggedInEmail();

        Cart userCart = cartRepository.findCartByEmail(email);

        Long cartId = userCart.getCartId();
        // Existing cart
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> {

                    log.warn("Cart not found with this cart Id {}", cartId);
                    return new ResourceNotFoundException("Cart", "cartId", cartId);
                });

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> {

                    log.warn("Product not found with this product Id {}", productId);
                    return new ResourceNotFoundException("Product", "productId", productId);
                });

        if (product.getQuantity() == 0) {
            log.warn(product.getProductName() + " is not available");
            throw new APIException(product.getProductName() + " is not available");
        }
        if (product.getQuantity() < quantity) {

            log.warn("please make an order the " + product.getProductName()
                    + " less than or equal to the quantity " + product.getQuantity());
            throw new APIException(
                    "please make an order the " + product.getProductName()
                            + " less than or equal to the quantity " + product.getQuantity());
        }
        // Add new checks or validations

        CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId(cartId, productId);

        if (cartItem == null) {

            log.warn("Product " + product.getProductName() + " not available in the cart");
            throw new APIException("Product " + product.getProductName() + " not available in the cart");
        }


        int newQuantity = cartItem.getQuantity() + quantity;

        log.info("New quantity : " + newQuantity + " products in stock " + product.getQuantity());

        if (newQuantity < 0) {

            log.warn("The resulting quantity cannot be negative");
            throw new APIException("The resulting quantity cannot be negative");
        }

        if (newQuantity > product.getQuantity()) {

            log.warn("Only " + product.getQuantity() + " units are available in stock. Please select the same quantity or fewer.");
            throw new APIException("Only " + product.getQuantity() + " units are available in stock. Please select the same quantity or fewer.");

        }

        if (newQuantity == 0) {

            log.info("removing product from cart completely ");
            // remove completely
            deleteProductFromCart(cartId, productId);

        } else {

//        Update the cart item with new Data
            cartItem.setProductPrice(product.getPrice());
            cartItem.setQuantity(cartItem.getQuantity() + quantity);
            cartItem.setDiscount(product.getDiscount());
// Update the cart with new cart item
            cart.setTotalPrice(cart.getTotalPrice() + (cartItem.getProductPrice() * quantity));
            cartRepository.save(cart);
        }

        CartItem updatedCartItem = cartItemRepository.save(cartItem);
        if (updatedCartItem.getQuantity() == 0) {


            cartItemRepository.deleteById(updatedCartItem.getCartItemId());
        }

        CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);
        List<CartItem> cartItems = cart.getCartItems();
        Stream<ProductDTO> productDTOStream = cartItems.stream().map(item -> {
            ProductDTO productDTO = modelMapper.map(item.getProduct(), ProductDTO.class);
            productDTO.setQuantity(item.getQuantity());
            return productDTO;
        });
        cartDTO.setProducts(productDTOStream.toList());

        log.info("Cart is updated successfully {}", cartDTO);
        return cartDTO;
    }

    @Transactional
    @Override
    public String deleteProductFromCart(Long cartId, Long productId) {

        log.info("Attempting to delete remove product from cart {}, Product Id {}", cartId, productId);

        Cart cart = cartRepository.findById(cartId).orElseThrow(() ->

        {
            log.warn("Cart not found with this cart Id {}", cartId);
            return new ResourceNotFoundException("Cart", "cartId", cartId);
        });

        CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId(cartId, productId);

        if (cartItem == null) {
            log.warn("Cart Item not found for this cart Id  {} and product Id {}", cartId, productId);


            throw new ResourceNotFoundException("cartItem", "cartItem", productId);
        }

        cart.setTotalPrice(cart.getTotalPrice() - (cartItem.getProductPrice() * cartItem.getQuantity()));
        cartItemRepository.deleteCartItemByProductIdAndCartId(cartId, productId);

        log.info("Product  " + cartItem.getProduct().getProductName() + "removed from the cart");

        return "Product " + cartItem.getProduct().getProductName() + " removed from the cart";
    }


    private Cart createCart() {

        log.info("Trying to get the email Address of logged in user ");
        String email = authUtil.loggedInEmail();


        if (email == null || email.isEmpty()) {
            log.warn("User email not found. Please log in.");
            throw new APIException("User email not found. Please log in. ");
        }


        log.info("Trying to fetch user cart by email : {}", email);
        Cart userCart = cartRepository.findCartByEmail(email);

        if (userCart != null) {

            log.info("Found the cart for this user {} and the cart is {} ", email, userCart);
            return userCart;
        }

        User user = authUtil.loggedInUser();
        if (user == null) {
            log.warn("User details not found. Please log in.");
            throw new APIException("User details not found. Please log in.");
        }

        Cart newCart = new Cart();

        newCart.setTotalPrice(0.0);  // Use BigDecimal for precision
        newCart.setUser(user);


        Cart savedCart = cartRepository.save(newCart);

        log.info("New cart is created and saved into database successfully {}", savedCart);

        return savedCart;
    }

}
