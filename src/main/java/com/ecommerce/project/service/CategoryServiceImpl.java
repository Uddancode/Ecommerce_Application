package com.ecommerce.project.service;

import com.ecommerce.project.exceptions.APIException;
import com.ecommerce.project.exceptions.ResourceNotFoundException;
import com.ecommerce.project.model.Category;
import com.ecommerce.project.payload.CategoryDTO;
import com.ecommerce.project.payload.CategoryResponse;
import com.ecommerce.project.repositories.CategoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public CategoryResponse getAllCategories(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {


        log.debug("Fetching all categories from database.");

        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);
        Page<Category> categoryPage = categoryRepository.findAll(pageDetails);

        List<Category> categories = categoryPage.getContent();
        if (categories.isEmpty()) {

            log.warn("No category available into database ");
            throw new APIException("No category created till now.");

        }


        List<CategoryDTO> categoryDTOS = categories.stream()
                .map(category -> modelMapper.map(category, CategoryDTO.class))
                .toList();

        log.info("Total categories retrieved from database : {}", categoryDTOS.size());

        CategoryResponse categoryResponse = new CategoryResponse();
        categoryResponse.setContent(categoryDTOS);
        categoryResponse.setPageNumber(categoryPage.getNumber());
        categoryResponse.setPageSize(categoryPage.getSize());
        categoryResponse.setTotalElements(categoryPage.getTotalElements());
        categoryResponse.setTotalPages(categoryPage.getTotalPages());
        categoryResponse.setLastPage(categoryPage.isLast());


        return categoryResponse;
    }

    @Override
    public CategoryDTO createCategory(CategoryDTO categoryDTO) {

        log.info("User trying to create category with these details , {}", categoryDTO);

        Category category = modelMapper.map(categoryDTO, Category.class);
        Category categoryFromDb = categoryRepository.findByCategoryName(category.getCategoryName());
        if (categoryFromDb != null) {

            log.warn("Category with the name " + category.getCategoryName() + " already exists !!!");
            throw new APIException("Category with the name " + category.getCategoryName() + " already exists !!!");

        }
        Category savedCategory = categoryRepository.save(category);

        log.info("Category created successfully  {}", savedCategory);
        return modelMapper.map(savedCategory, CategoryDTO.class);
    }

    @Override
    public CategoryDTO deleteCategory(Long categoryId) {


        log.info("User trying to delete category with id :{}", categoryId);
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> {


                    log.warn("Category not found with this category id {}", categoryId);
                    return new ResourceNotFoundException("Category", "categoryId", categoryId);
                });


        categoryRepository.delete(category);

        log.info("Category is delete successfully with category Id :{}", categoryId);
        return modelMapper.map(category, CategoryDTO.class);
    }

    @Override
    public CategoryDTO updateCategory(CategoryDTO categoryDTO, Long categoryId) {

        log.info("User trying to update category with this category id :{}, new details : {}", categoryId, categoryDTO);
        Category savedCategory = categoryRepository.findById(categoryId)
                .orElseThrow(() -> {

                    log.warn("Failed to update the category , Category was not found  with this category id : {}", categoryId);

                    return new ResourceNotFoundException("Category", "categoryId", categoryId);

                });

        Category category = modelMapper.map(categoryDTO, Category.class);
        category.setCategoryId(categoryId);
        savedCategory = categoryRepository.save(category);

        log.info("Category updated successfully :{}", savedCategory);
        return modelMapper.map(savedCategory, CategoryDTO.class);
    }
}
