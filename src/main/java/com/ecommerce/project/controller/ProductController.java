package com.ecommerce.project.controller;

import com.ecommerce.project.config.AppConstants;
import com.ecommerce.project.payload.ProductDTO;
import com.ecommerce.project.payload.ProductResponse;
import com.ecommerce.project.service.ProductService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Slf4j
@RestController
@RequestMapping("/api")
public class ProductController {

    @Autowired
    ProductService productService;


    @PreAuthorize("hasAnyRole('ADMIN','SELLER')")
    @PostMapping("/admin/categories/{categoryId}/product")
    public ResponseEntity<ProductDTO> addProduct(
            @Valid @RequestBody ProductDTO productDTO,
            @PathVariable Long categoryId) {

        log.info("Request received: Creating new product : {}", productDTO);


        ProductDTO savedProductDTO = productService.addProduct(categoryId, productDTO);

        log.info("Product created successfully : {}", savedProductDTO);
        return new ResponseEntity<>(savedProductDTO, HttpStatus.CREATED);
    }


    @GetMapping("/public/products")
    public ResponseEntity<ProductResponse> getAllProducts(
            @RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,

            @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,

            @RequestParam(name = "sortBy", defaultValue = AppConstants.SORT_PRODUCTS_BY, required = false) String sortBy,

            @RequestParam(name = "sortOrder", defaultValue = AppConstants.SORT_DIR, required = false) String sortOrder) {

        log.info("Request received : Fetching all the products ");
        ProductResponse productResponse = productService.getAllProducts(pageNumber, pageSize, sortBy, sortOrder);

        log.info(" Returning products - {}", productResponse);
        return new ResponseEntity<>(productResponse, HttpStatus.OK);
    }


    @GetMapping("/public/categories/{categoryId}/products")
    public ResponseEntity<ProductResponse> getProductsByCategory(
            @PathVariable Long categoryId,

            @RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,

            @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,

            @RequestParam(name = "sortBy", defaultValue = AppConstants.SORT_PRODUCTS_BY, required = false) String sortBy,

            @RequestParam(name = "sortOrder", defaultValue = AppConstants.SORT_DIR, required = false) String sortOrder) {


        log.info("Request received : Fetching products by category Id : {}", categoryId);


        ProductResponse productResponse = productService.searchByCategory(categoryId, pageNumber, pageSize, sortBy, sortOrder);

        log.info("Returning  the products based on category id : {}, products :{}", categoryId, productResponse);
        return new ResponseEntity<>(productResponse, HttpStatus.OK);
    }


    @GetMapping("/public/products/keyword/{keyword}")
    public ResponseEntity<ProductResponse> getProductsByKeyword(
            @PathVariable String keyword,
            @RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
            @RequestParam(name = "sortBy", defaultValue = AppConstants.SORT_PRODUCTS_BY, required = false) String sortBy,
            @RequestParam(name = "sortOrder", defaultValue = AppConstants.SORT_DIR, required = false) String sortOrder) {
        log.info("Request received : Fetching products by Keyword : {}", keyword);


        ProductResponse productResponse = productService.searchProductByKeyword(keyword, pageNumber, pageSize, sortBy, sortOrder);
        log.info("Returning  the products based on Keyword : {}, products :{}", keyword, productResponse);

        return new ResponseEntity<>(productResponse, HttpStatus.FOUND);
    }


    @PreAuthorize("hasRole('ADMIN','SELLER')")
    @PutMapping("/admin/products/{productId}")
    public ResponseEntity<ProductDTO> updateProduct(
            @Valid @RequestBody ProductDTO productDTO,
            @PathVariable Long productId) {
        log.info("Request received : Update  product by product Id : {} ", productId);


        ProductDTO updatedProductDTO = productService.updateProduct(productId, productDTO);

        log.info("Product updated successfully : {}", updatedProductDTO);

        return new ResponseEntity<>(updatedProductDTO, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN','SELLER')")
    @DeleteMapping("/admin/products/{productId}")
    public ResponseEntity<ProductDTO> deleteProduct(
            @PathVariable Long productId) {
        log.info("Request received : Delete product by product Id : {} ", productId);


        ProductDTO deletedProduct = productService.deleteProduct(productId);

        log.info("Product deleted successfully by product Id : {} ", productId);

        return new ResponseEntity<>(deletedProduct, HttpStatus.OK);
    }


    @PreAuthorize("hasRole('ADMIN','SELLER')")
    @PutMapping("/admin/products/{productId}/image")
    public ResponseEntity<ProductDTO> updateProductImage(
            @PathVariable Long productId,
            @RequestParam("image") MultipartFile image) throws IOException {

        log.info("Request received : Update product image  by product Id : {} ", productId);

        ProductDTO updatedProduct = productService.updateProductImage(productId, image);
        log.info("Product Updated successfully product Id : {} ", productId);

        return new ResponseEntity<>(updatedProduct, HttpStatus.OK);
    }
}
