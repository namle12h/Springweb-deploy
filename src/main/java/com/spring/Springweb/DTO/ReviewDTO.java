/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.spring.Springweb.DTO;

import lombok.Data;

/**
 *
 * @author ADMIN
 */
@Data

public class ReviewDTO {

    private Integer id;
    private Integer rating;
    private String comment;
    private String imageUrl;
    private String reviewType;
    private Integer refId;
    private Integer customerId;
}
