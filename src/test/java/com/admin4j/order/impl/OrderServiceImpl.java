package com.admin4j.order.impl;

import com.admin4j.order.OrderService;

/**
 * @author andanyang
 * @since 2023/4/20 11:29
 */
public class OrderServiceImpl implements OrderService {

    @Override
    public String createOrder(String orderNo) {
        System.out.println("createOrder = " + orderNo);
        return "OKKK";
    }
}
