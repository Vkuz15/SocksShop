package com.shop.socks.service.impl;

import com.shop.socks.dto.LotOfSocksDto;
import com.shop.socks.dto.SocksDto;
import com.shop.socks.model.Socks;
import com.shop.socks.repository.SocksRepository;
import com.shop.socks.service.SocksService;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SocksServiceImpl implements SocksService {

    private final SocksRepository socksRepository;

    @Setter
    private List<SocksDto> socksList = new ArrayList<>();

    @Autowired
    public SocksServiceImpl(SocksRepository socksRepository) {
        this.socksRepository = socksRepository;
    }

    @Override
    public LotOfSocksDto removeSocks(SocksDto socksDto) {
        log.info("Попытка удалить носки: {}", socksDto);
        Optional<Socks> existingSocksOpt = socksRepository
                .findByColorAndCotton(socksDto.getColor(), socksDto.getCotton());
        if (existingSocksOpt.isEmpty() || existingSocksOpt.get().getQuantity() < socksDto.getQuantity()) {
            log.warn("Недостаточно носков на складе для удаления: {}", socksDto);
            return null;
        }

        Socks existingSocks = existingSocksOpt.get();
        existingSocks.setQuantity(existingSocks.getQuantity() - socksDto.getQuantity());
        socksRepository.save(existingSocks);
        log.info("Носки удалены: {}", existingSocks);
        return convertToLotOfSocks(existingSocks);
    }

    @Override
    public LotOfSocksDto addSocks(SocksDto socksDto) {
        log.info("Попытка добавить носки: {}", socksDto);
        if (socksDto.getCotton() < 0 || socksDto.getCotton() > 100) { // Проверка диапазона для cotton
            throw new IllegalArgumentException("Значение хлопка должно быть в диапазоне: 0-100");
        }
        Optional<Socks> existingSocksOpt = socksRepository
                .findByColorAndCotton(socksDto.getColor(), socksDto.getCotton());
        Socks socksToReturn;
        if (existingSocksOpt.isPresent()) {
            Socks socks = existingSocksOpt.get();
            socks.setQuantity(socks.getQuantity() + socksDto.getQuantity());
            socksRepository.save(socks);
            log.info("Количество носков обновлено: {}", socks);
            socksToReturn = socks;
        } else {
            socksToReturn = new Socks();
            socksToReturn.setColor(socksDto.getColor());
            socksToReturn.setCotton(socksDto.getCotton());
            socksToReturn.setQuantity(socksDto.getQuantity());
            socksRepository.save(socksToReturn);
            log.info("Носки добавлены: {}", socksToReturn);
        }
        return convertToLotOfSocks(socksToReturn);
    }

    @Override
    public List<SocksDto> getFilteredSocks(String color, Integer minCotton, Integer maxCotton, String sortBy) {
        log.info("Получение носков с фильтрацией: color={}, minCotton={}, maxCotton={}, sortBy={}",
                color, minCotton, maxCotton, sortBy);

        if (socksList.isEmpty()) {
            log.warn("Список носков пуст!");
            return Collections.emptyList(); // Возвращаем пустой список
        }

        List<SocksDto> filteredList = new ArrayList<>(socksList);

        //Фильтрация по цвету
        if (color != null && !color.trim().isEmpty()) {
            filteredList = filteredList.stream()
                    .filter(sock -> sock.getColor().equalsIgnoreCase(color.trim()))
                    .collect(Collectors.toList());
            log.info("После фильтрации по цвету: {}", filteredList.size());
        }

        //Фильтрация по диапазону хлопка
        if (minCotton != null) {
            filteredList = filteredList.stream()
                    .filter(sock -> sock.getCotton() >= minCotton)
                    .collect(Collectors.toList());
            log.info("После фильтрации по minCotton: {}", filteredList.size());
        }
        if (maxCotton != null) {
            filteredList = filteredList.stream()
                    .filter(sock -> sock.getCotton() <= maxCotton)
                    .collect(Collectors.toList());
            log.info("После фильтрации по maxCotton: {}", filteredList.size());
        }

        if ("color".equalsIgnoreCase(sortBy)) {
            filteredList.sort(Comparator.comparing(SocksDto::getColor));
        } else if ("cotton".equalsIgnoreCase(sortBy)) {
            filteredList.sort(Comparator.comparing(SocksDto::getCotton));
        }
        log.info("Фильтрованные носки: {}", filteredList);
        return filteredList;
    }

    @Override
    public SocksDto updateSocks(Long id, SocksDto socksDto) {
        log.info("Обновление носков с ID: {} и данными: {}", id, socksDto);
        Optional<Socks> optionalSocks = socksRepository.findById(id);

        if (optionalSocks.isPresent()) {
            Socks socks = optionalSocks.get();
            socks.setColor(socksDto.getColor());
            socks.setCotton(socksDto.getCotton());
            socks.setQuantity(socksDto.getQuantity());

            socksRepository.save(socks);
            log.info("Носки обновлены: {}", socks);
            return new SocksDto(socks.getColor(), socks.getCotton(), socks.getQuantity());
        } else {
            log.error("Носки с ID: {} не найдены", id);
            throw new RuntimeException("Носки с ID: " + id + " не найдены");
        }
    }

//    @Override
//    public LotOfSocksDto processBatchOfSocks(MultipartFile file) {
//        log.info("Обработка партии носков из файла: {}", file.getOriginalFilename());
//        List<Socks> socksList = new ArrayList<>();
//
//        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
//            Sheet sheet = workbook.getSheetAt(0); //Получаем первый лист
//
//            for (Row row : sheet) {
//                if (row.getRowNum() == 0) continue; //Пропускаем заголовок
//
//                String color = row.getCell(0).getStringCellValue();
//                int cotton = (int) row.getCell(1).getNumericCellValue();
//                int quantity = (int) row.getCell(2).getNumericCellValue();
//
//                Socks socks = new Socks();
//                socks.setColor(color);
//                socks.setCotton(cotton);
//                socks.setQuantity(quantity);
//
//                socksList.add(socks);
//            }
//            socksRepository.saveAll(socksList); //Сохраняем все носки в Базе данных
//            log.info("Партия носков успешно загружена, количество: {}", socksList.size());
//        } catch (IOException e) {
//            log.error("Ошибка при чтении файла: {}", e.getMessage());
//            throw new RuntimeException("Ошибка при чтении файла", e);
//        }
//        return new LotOfSocksDto(socksList.size());
//    }

    @Override
    public LotOfSocksDto processBatchOfSocks(MultipartFile file) {
        log.info("Обработка партии носков из файла: {}", file.getOriginalFilename());
        List<Socks> socksList = new ArrayList<>();

        // Проверка на пустой файл
        if (file.isEmpty()) {
            log.error("Файл пустой: {}", file.getOriginalFilename());
            throw new IllegalArgumentException("Файл не должен быть пустым");
        }

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0); // Получаем первый лист

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // Пропускаем заголовок

                String color = row.getCell(0).getStringCellValue();
                int cotton = (int) row.getCell(1).getNumericCellValue();
                int quantity = (int) row.getCell(2).getNumericCellValue();

                Socks socks = new Socks();
                socks.setColor(color);
                socks.setCotton(cotton);
                socks.setQuantity(quantity);

                socksList.add(socks);
            }
            socksRepository.saveAll(socksList); // Сохраняем все носки в Базе данных
            log.info("Партия носков успешно загружена, количество: {}", socksList.size());
        } catch (IOException e) {
            log.error("Ошибка при чтении файла: {}", e.getMessage());
            throw new RuntimeException("Ошибка при чтении файла", e);
        } catch (Exception e) {
            log.error("Ошибка при обработке данных из файла: {}", e.getMessage());
            throw new RuntimeException("Ошибка при обработке данных из файла", e);
        }
        return new LotOfSocksDto(socksList.size());
    }

    private LotOfSocksDto convertToLotOfSocks(Socks socks) {
        return new LotOfSocksDto(socks.getColor(), socks.getCotton(), socks.getQuantity());
    }

//    //Метод для загрузки данных из CSV-файла
//    public void loadDataFromCSV(String csvFilePath) {
//        log.info("Загрузка данных из CSV файла: {}", csvFilePath);
//        try (FileReader reader = new FileReader(csvFilePath)) {
//            Iterable<CSVRecord> records = CSVFormat.DEFAULT.withHeader("ID", "Color", "Cotton", "Quantity").parse(reader);
//            for (CSVRecord record : records) {
//                SocksDto sock = new SocksDto();
//                sock.setId(Long.parseLong(record.get("ID")));
//                sock.setColor(record.get("Color"));
//                sock.setCotton(Integer.parseInt(record.get("Cotton")));
//                sock.setQuantity(Integer.parseInt(record.get("Quantity")));
//                socksList.add(sock);
//            }
//            log.info("Данные успешно загружены из CSV файла");
//        } catch (IOException e) {
//            log.error("Ошибка при чтении CSV файла: {}", e.getMessage());
//            e.printStackTrace();
//            // Обработайте ошибку чтения файла
//        }
//    }

    public void loadDataFromCSV(String csvFilePath) {
        log.info("Загрузка данных из CSV файла: {}", csvFilePath);

        // Проверка на существование файла
        File csvFile = new File(csvFilePath);
        if (!csvFile.exists() || !csvFile.canRead()) {
            log.error("Файл не существует или не может быть прочитан: {}", csvFilePath);
            throw new IllegalArgumentException("Файл не существует или не может быть прочитан");
        }

        try (FileReader reader = new FileReader(csvFilePath)) {
            Iterable<CSVRecord> records = CSVFormat.DEFAULT.withHeader("ID", "Color", "Cotton", "Quantity").parse(reader);
            for (CSVRecord record : records) {
                try {
                    SocksDto sock = new SocksDto();
                    sock.setId(Long.parseLong(record.get("ID")));
                    sock.setColor(record.get("Color"));
                    sock.setCotton(Integer.parseInt(record.get("Cotton")));
                    sock.setQuantity(Integer.parseInt(record.get("Quantity")));
                    socksList.add(sock);
                } catch (NumberFormatException e) {
                    log.error("Ошибка при парсинге данных в строке {}: {}", record.getRecordNumber(), e.getMessage());
                    // Можно добавить логику для обработки ошибок, например, пропустить строку или сохранить в отдельный список ошибок
                }
            }
            log.info("Данные успешно загружены из CSV файла, количество: {}", socksList.size());
        } catch (IOException e) {
            log.error("Ошибка при чтении CSV файла: {}", e.getMessage());
            throw new RuntimeException("Ошибка при чтении CSV файла", e);
        }
    }
}
