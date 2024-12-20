package com.shop.socks.repository;

import com.shop.socks.model.Socks;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SocksRepository extends JpaRepository<Socks, Long> {

    Optional<Socks> findByColorAndCotton(String color, int cotton);

    List<Socks> findByColor(String color);
}
