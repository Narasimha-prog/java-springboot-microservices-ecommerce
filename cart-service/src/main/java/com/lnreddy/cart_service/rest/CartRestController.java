package com.lnreddy.cart_service.rest;


import com.lnreddy.cart_service.service.ICartService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/cart")
public class CartRestController {


    private final ICartService cartService;



}
