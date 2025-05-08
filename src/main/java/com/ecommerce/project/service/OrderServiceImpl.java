package com.ecommerce.project.service;

import com.ecommerce.project.exceptions.APIException;
import com.ecommerce.project.exceptions.ResourceNotFoundException;
import com.ecommerce.project.model.*;
import com.ecommerce.project.payload.OrderDTO;
import com.ecommerce.project.payload.OrderItemDTO;
import com.ecommerce.project.repositories.*;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class OrderServiceImpl implements OrderService {


    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CartService cartService;

    @Autowired
    private ModelMapper mapper;
    @Autowired
    private CartItemRepository cartItemRepository;


//    @Transactional
//    @Override
//    public OrderDTO placeOrder(String emailId, Long addressId, String paymentMethod, String pgName, String pgPaymentId, String pgStatus, String pgResponseMessage) {
//
//        // Cart
//        Cart cart = cartRepository.findCartByEmail(emailId);
//        if (cart == null) {
//            throw new ResourceNotFoundException("Cart", "email", emailId);
//        }
//        // Address
//        Address address = addressRepository.findById(addressId)
//                .orElseThrow(() -> new ResourceNotFoundException("Address", "addressId", addressId));
//
//
//        Order order = new Order();
//        order.setEmail(emailId);
//        order.setOrderDate(LocalDate.now());
//        order.setTotalAmount(cart.getTotalPrice());
//        order.setOrderStatus("Order Accepted!");
//        order.setAddress(address);
//
//        // Payment
//
//        Payment payment = new Payment(paymentMethod, pgPaymentId, pgStatus, pgResponseMessage, pgName);
//
//        payment.setOrder(order);
//
//
////        Save the payment
//
//        Payment savedPayment = paymentRepository.save(payment);
//        // set payment into order
//        order.setPayment(savedPayment);
//
//        // save order into db
//        Order savedOrder = orderRepository.save(order);
//
//        //
//        List<CartItem> cartItems = cart.getCartItems();
//
//        if (cartItems.isEmpty()) {
//            throw new APIException("Cart is empty");
//        }
//
//        // cart items -> OrderItems
//
//        List<OrderItem> orderItems = new ArrayList<>();
//        for (CartItem cartItem : cartItems
//        ) {
//            OrderItem orderItem = new OrderItem();
//            orderItem.setProduct(cartItem.getProduct());
//            orderItem.setQuantity(cartItem.getQuantity());
//            orderItem.setDiscount(cartItem.getDiscount());
//            orderItem.setOrderedProductPrice(cartItem.getProductPrice());
//            orderItem.setOrder(savedOrder);
//            orderItems.add(orderItem);
//
//        }
//
//        orderItems = orderItemRepository.saveAll(orderItems);
//        cart.getCartItems().forEach(item -> {
//            int quantity = item.getQuantity();
//            Product product = item.getProduct();
//
//            // Reduce stock
//            product.setQuantity(product.getQuantity() - quantity);
//            // save the product back to db
//
//            productRepository.save(product);
//
//            // Remove these items cart
//            cartService.deleteProductFromCart(cart.getCartId(), item.getProduct().getProductId());
//        });
//        OrderDTO orderDTO = mapper.map(savedOrder, OrderDTO.class);
//
//        orderItems.forEach(item -> orderDTO.getOrderItems().add(mapper.map(item, OrderItemDTO.class)));
//
//
//        // add the address orderDTO
//        orderDTO.setAddressId(addressId);
//
//        return orderDTO;
//    }

    @Transactional
    @Override
    public OrderDTO placeOrder(String emailId, Long addressId, String paymentMethod, String pgName, String pgPaymentId, String pgStatus, String pgResponseMessage) {


        log.info("Attempting to place new Order");
        // Fetch cart
        Cart cart = cartRepository.findCartByEmail(emailId);
        if (cart == null) {


            log.warn("Cart not found for this email {}", emailId);
            throw new ResourceNotFoundException("Cart", "email", emailId);
        }

        // Ensure cart is not empty
        List<CartItem> cartItems = cart.getCartItems();
        if (cartItems == null || cartItems.isEmpty()) {

            log.warn("Cart is empty ");
            throw new APIException("Cart is empty");
        }

        // Fetch Address
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> {

                    log.warn("Address not found with this address Id {}", addressId);
                    return new ResourceNotFoundException("Address", "addressId", addressId);
                });

        // Create Order
        Order order = new Order();
        order.setEmail(emailId);
        order.setOrderDate(LocalDate.now());
        order.setTotalAmount(cart.getTotalPrice());
        order.setOrderStatus("Order Accepted!");
        order.setAddress(address);

        // Create and link Payment
        Payment payment = new Payment(paymentMethod, pgPaymentId, pgStatus, pgResponseMessage, pgName);

        paymentRepository.save(payment);
        order.setPayment(payment);

        // Save Order (cascade saves payment)
        Order savedOrder = orderRepository.save(order);


        // Convert Cart Items -> Order Items
        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem cartItem : cartItems) {
            Product product = cartItem.getProduct();

            // Check stock
            if (product.getQuantity() < cartItem.getQuantity()) {

                log.warn("Not enough stock for product: " + product.getProductName());
                throw new APIException("Not enough stock for product: " + product.getProductName());
            }

            // Create Order Item
            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(product);
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setDiscount(cartItem.getDiscount());
            orderItem.setOrderedProductPrice(cartItem.getProductPrice());
            orderItem.setOrder(savedOrder);
            orderItems.add(orderItem);

            // Reduce stock
            product.setQuantity(product.getQuantity() - cartItem.getQuantity());
        }

        // Save Order Items in batch
        orderItemRepository.saveAll(orderItems);

        // Save updated products in batch
        productRepository.saveAll(cartItems.stream().map(CartItem::getProduct).collect(Collectors.toList()));


        // once the order is placed the cat should get deleted automatically

        cartItemRepository.deleteAllByCartId(cart.getCartId());

        cartRepository.deleteByCartId(cart.getCartId());


        // Convert to DTO
        OrderDTO orderDTO = mapper.map(savedOrder, OrderDTO.class);
        orderDTO.setOrderItems(orderItems.stream()
                .map(item -> mapper.map(item, OrderItemDTO.class))
                .collect(Collectors.toList()));
        orderDTO.setAddressId(addressId);


        log.info("Order placed successfully {}", orderDTO);
        return orderDTO;
    }


}
