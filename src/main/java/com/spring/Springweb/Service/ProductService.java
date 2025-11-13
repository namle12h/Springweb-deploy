package com.spring.Springweb.Service;

import java.io.IOException;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.spring.Springweb.DTO.ProductDTO;
import org.springframework.data.domain.Page;

public interface ProductService {

    List<ProductDTO> getAll();

    ProductDTO getById(Integer id);

    ProductDTO create(ProductDTO dto, MultipartFile image) throws IOException;

    ProductDTO update(Integer id, ProductDTO dto, MultipartFile image) throws IOException;

    void delete(Integer id);

    public Page<ProductDTO> getAllProducts(int page, int limit);
}
