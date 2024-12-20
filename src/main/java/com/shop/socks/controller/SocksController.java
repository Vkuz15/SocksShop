package com.shop.socks.controller;

import com.shop.socks.dto.LotOfSocksDto;
import com.shop.socks.dto.SocksDto;
import com.shop.socks.service.impl.SocksServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/socks")
public class SocksController {

    private final SocksServiceImpl socksServiceImpl;
    private static final Logger logger = LoggerFactory.getLogger(SocksController.class);

    @Operation(summary = "Регистрация прихода носков")
    @PostMapping(path = "/income")
    public ResponseEntity<LotOfSocksDto> arrivalOfSocks(@RequestBody SocksDto socksDto) {
        logger.info("Регистрация прихода носков: {}", socksDto);
        // Проверка диапазона для cotton
        if (socksDto.getCotton() < 0 || socksDto.getCotton() > 100) {
            return ResponseEntity.badRequest().body(null); // Возвращаем ошибку, если cotton вне диапазона
        }
        LotOfSocksDto updateSocks = socksServiceImpl.addSocks(socksDto); //Обработка прихода носков
        logger.info("Приход носков зарегистрирован: {}", updateSocks);
        return new ResponseEntity<>(updateSocks, HttpStatus.CREATED); //Возвращаем обновленный список носков
    }

    @Operation(summary = "Регистрация отпуска носков")
    @PostMapping(path = "/outcome")
    public ResponseEntity<LotOfSocksDto> departureOfSocks(@RequestBody SocksDto socksDto) {
        logger.info("Регистрация отпуска носков: {}", socksDto);
        LotOfSocksDto updateSocks = socksServiceImpl.removeSocks(socksDto);
        if (updateSocks == null) {
            logger.warn("Не удалось зарегистрировать отпуск носков: {}", socksDto);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        logger.info("Отпуск носков зарегистрирован: {}", updateSocks);
        return new ResponseEntity<>(updateSocks, HttpStatus.OK);
    }

    @Operation(summary = "Получение общего количества носков с фильтрацией")
    @GetMapping
    public ResponseEntity<List<SocksDto>> allFilteredSocks(
            @RequestParam String color,
            @RequestParam(required = false) Integer minCotton,
            @RequestParam(required = false) Integer maxCotton,
            @RequestParam(required = false) String sortBy
    ) {
        logger.info("Получение носков с фильтрацией: color={}, minCotton={}, maxCotton={}, sortBy={}",
                color, minCotton, maxCotton, sortBy);
        if (minCotton != null) {
            logger.debug("minCotton параметр передан: {}", minCotton);
        } else {
            logger.debug("minCotton параметр не передан.");
        }

        if (maxCotton != null) {
            logger.debug("maxCotton параметр передан: {}", maxCotton);
        } else {
            logger.debug("maxCotton параметр не передан.");
        }

        logger.info("Вызов сервиса для получения отфильтрованных носков...");
        List<SocksDto> filteredSocks = socksServiceImpl.getFilteredSocks(color, minCotton, maxCotton, sortBy);

        logger.info("Носки получены: {}", filteredSocks);
        return ResponseEntity.ok(filteredSocks);
    }

    @Operation(summary = "Обновление данных носков")
    @PutMapping(path = "/{id}")
    public ResponseEntity<SocksDto> updateSocks(@PathVariable Long id, @RequestBody SocksDto socksDto) {
        logger.info("Обновление носков с id={} и данными: {}", id, socksDto);
        SocksDto updatedSocks = socksServiceImpl.updateSocks(id, socksDto);
        logger.info("Данные носков обновлены: {}", updatedSocks);
        return ResponseEntity.ok(updatedSocks);
    }

    @Operation(summary = "Загрузка партий носков из Excel файла")
    @PostMapping(path = "/batch")
    public ResponseEntity<LotOfSocksDto> batchOfSocksFromExcel(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            logger.warn("Попытка загрузки пустого файла");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        logger.info("Загрузка партий носков из файла: {}", file.getOriginalFilename());
        LotOfSocksDto lotOfSocksDto = socksServiceImpl.processBatchOfSocks(file);
        logger.info("Партия носков загружена: {}", lotOfSocksDto);
        return ResponseEntity.ok(lotOfSocksDto);
    }
}
