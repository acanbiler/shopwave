package com.shopwave.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReviewDto extends BaseDto {
    private Long userId;
    private String userFirstName;
    private String userLastName;
    private Long productId;
    private String productName;
    private Integer rating;
    private String comment;
    private String title;
    private Boolean isVerifiedPurchase;
    private Boolean isHelpful;
} 