package com.shop.socks.dto;

import lombok.Data;

@Data
public class LotOfSocksDto {

    private String color;
    private int cotton;
    private int quantity;

    public LotOfSocksDto(int size) {
    }

    public LotOfSocksDto(String color, int cotton, int quantity) {
        this.color = color;
        this.cotton = cotton;
        this.quantity = quantity;
    }
}
