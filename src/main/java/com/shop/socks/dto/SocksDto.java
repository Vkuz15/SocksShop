package com.shop.socks.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Data
public class SocksDto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Schema(description = "Цвет носов")
    private String color;

    @Schema(description = "Процент содержания хлопка")
    private int cotton;

    @Schema(description = "Количество носков")
    private int quantity;

    public SocksDto() {
    }

    public SocksDto(String color, int cotton, int quantity) {
        this.color = color;
        this.cotton = cotton;
        this.quantity = quantity;
    }
}
