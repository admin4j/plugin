package com.admin4j.order;

import com.admin4j.api.anno.SPI;

/**
 * @author andanyang
 * @since 2023/4/20 11:28
 */
@SPI("def")
public interface OrderService {

    String createOrder(String orderNo);
}
