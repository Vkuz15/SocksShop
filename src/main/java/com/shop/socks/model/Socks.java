package com.shop.socks.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Objects;

@Entity
@Data
@Table(name = "socks")
public class Socks {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "color", nullable = false)
    private String color;

    @Column(name = "cotton", nullable = false)
    private int cotton;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    public Socks() {
    }

    public Socks(String color, int cotton, int quantity) {
        this.color = color;
        this.cotton = cotton;
        this.quantity = quantity;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public int getCotton() {
        return cotton;
    }

    public void setCotton(int cotton) {
        this.cotton = cotton;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof Socks)) return false;
        Socks socks = (Socks) object;
        return cotton == socks.cotton &&
                quantity == socks.quantity &&
                color.equals(socks.color);
    }

    @Override
    public int hashCode() {
        return Objects.hash(color, cotton, quantity);
    }

    @Override
    public String toString() {
        return "Socks{" +
                "id=" + id +
                ", color='" + color + '\'' +
                ", cotton=" + cotton +
                ", quantity=" + quantity +
                '}';
    }
}
