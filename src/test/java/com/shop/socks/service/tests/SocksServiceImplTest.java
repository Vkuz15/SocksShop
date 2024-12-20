package com.shop.socks.service.tests;

import com.shop.socks.dto.LotOfSocksDto;
import com.shop.socks.dto.SocksDto;
import com.shop.socks.model.Socks;
import com.shop.socks.repository.SocksRepository;
import com.shop.socks.service.impl.SocksServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.stream.Collectors;

public class SocksServiceImplTest {

    @InjectMocks
    private SocksServiceImpl socksService;

    @Mock
    private SocksRepository socksRepository;

    @Mock
    private List<SocksDto> socksList;

    @Mock
    private MultipartFile file;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        socksList = new ArrayList<>();
        socksList.add(new SocksDto("Red", 40, 100));
        socksList.add(new SocksDto("Blue", 70, 10));
        socksList.add(new SocksDto("Red", 30, 150));
        socksList.add(new SocksDto("Green", 60, 50));
        socksService.setSocksList(socksList);
    }

    //Проверить, что метод removeSocks корректно уменьшает кол-во носков на складе
    @Test
    public void testRemoveSocks() {
        SocksDto socksDto = new SocksDto("Red", 40, 50); //Носки для удаления
        Socks existingSocks = new Socks("Red", 40, 100); //Существующие носки на складе

        when(socksRepository.findByColorAndCotton(socksDto.getColor(), socksDto.getCotton()))
                .thenReturn(Optional.of(existingSocks)); //Настройка поведения мока

        LotOfSocksDto result = socksService.removeSocks(socksDto); //Вызов метода

        assertNotNull(result); //Проверка результата
        assertEquals(50, existingSocks.getQuantity()); //Проверяем что количество уменьшилось
        verify(socksRepository, times(1)).save(existingSocks); //Проверяем, что save был вызван
    }

    //Проверить, что метод removeSocks не позволяет удалить большее количество носков, чем есть на складе
    @Test
    public void testRemoveSocks_InsufficientQuantity() {
        SocksDto socksDto = new SocksDto("Red", 70, 150); //Носки для удаления
        Socks existingSocks = new Socks("Red", 70, 100); //Существующие носки на складе

        when(socksRepository.findByColorAndCotton(socksDto.getColor(), socksDto.getCotton()))
                .thenReturn(Optional.of(existingSocks)); //Настройка поведения мока

        LotOfSocksDto result = socksService.removeSocks(socksDto); //Вызов метода

        assertNull(result); //Ожидаем, что результат будет null
        verify(socksRepository, never()).save(any(Socks.class)); //Проверяем, что save не был вызван
    }

    //Проверить, что метод removeSocks корректно обрабатывает ситуацию, когда нужно удалить носки, которых нет на складе
    @Test
    public void testRemoveSocks_NonExistingSocks() {
        SocksDto socksDto = new SocksDto("Red", 60, 40); //Носки для удаления

        when(socksRepository.findByColorAndCotton(socksDto.getColor(), socksDto.getCotton()))
                .thenReturn(Optional.empty()); //Настройки поведения мока

        LotOfSocksDto result = socksService.removeSocks(socksDto); //Вызов метода

        assertNull(result); //Ожидаем, что результат будет null
        verify(socksRepository, never()).save(any(Socks.class)); //Проверяем, что save не был вызван
    }

    //Проверяет, что метод addSocks корректно обновляет кол-во носков, если они уже есть в БД
    @Test
    public void testAddSocks_ExistingSocks() {
        SocksDto socksDto = new SocksDto("Red", 50, 50); //Носки для добавления
        Socks existingSocks = new Socks("Red", 50, 30); //Существующие носки на складе

        when(socksRepository.findByColorAndCotton(socksDto.getColor(), socksDto.getCotton()))
                .thenReturn(Optional.of(existingSocks)); //Настройка поведения мока

        LotOfSocksDto result = socksService.addSocks(socksDto); //Вызов метода

        assertNotNull(result); //Проверка результата
        assertEquals("Red", result.getColor());
        assertEquals(50, result.getCotton());
        assertEquals(80, result.getQuantity()); //Проверяем, что количество обновлено
        verify(socksRepository, times(1))
                .save(existingSocks); //Проверяем, что save был вызван один раз
    }

    //Проверяет, что метод addSocks корректно обновляет кол-во носков, если их нет в БД
    @Test
    public void testAddSocks_NewSocks() {
        SocksDto socksDto = new SocksDto("Blue", 80, 20); //Носки для добавления
        Socks newSocks = new Socks("Blue", 80, 20); //Существующие носки на складе

        when(socksRepository.findByColorAndCotton(socksDto.getColor(), socksDto.getCotton()))
                .thenReturn(Optional.empty()); //Настройка поведения мока

        LotOfSocksDto result = socksService.addSocks(socksDto); //Вызов метода

        assertNotNull(result); //Проверка результата
        assertEquals(newSocks.getColor(), result.getColor());
        assertEquals(newSocks.getCotton(), result.getCotton());
        assertEquals(newSocks.getQuantity(), result.getQuantity());
        verify(socksRepository, times(1))
                .save(newSocks); //Проверяем, что save был вызван один раз
    }

    //Проверить, что метод возвращает пустой список, если в базе данных нет носков
    @Test
    public void testGetFilteredSocks_EmptyList() {
        socksList.clear(); // Очищаем список для теста
        List<SocksDto> result = socksService.getFilteredSocks("Red", null, null, null);
        assertTrue(result.isEmpty(), "Список должен быть пустым");
    }

    //Проверить, что метод фильтрует носки по цвету
    @Test
    public void testGetFilteredSocks_FilterByColor() {
        List<SocksDto> result = socksService.getFilteredSocks("Red", null, null, null);
        assertEquals(2, result.size(), "Должно быть 2 носка красного цвета");
    }

    //Проверить, что метод фильтрует носки по минимальному содержанию хлопка
    @Test
    public void testGetFilteredSocks_FilterByMinCotton() {
        List<SocksDto> result = socksService.getFilteredSocks(null, 50, null, null);
        assertEquals(2, result.size(), "Должно быть 2 носка с хлопком >= 50");
    }

    //Проверить, что метод фильтрует носки по максимальному содержанию хлопка
    @Test
    public void testGetFilteredSocks_FilterByMaxCotton() {
        List<SocksDto> result = socksService.getFilteredSocks(null, null, 50, null);
        assertEquals(2, result.size(), "Должно быть 2 носка с хлопком <= 50");
    }

    //Проверить, что метод фильтрует носки по диапазону содержания хлопка
    @Test
    public void testGetFilteredSocks_FilterByMinAndMaxCotton() {
        List<SocksDto> result = socksService.getFilteredSocks(null, 30, 60, null);
        assertEquals(3, result.size(), "Должно быть 3 носка с хлопком в диапазоне [30, 60]");
    }

    //Проверить, что метод сортирует носки по цвету
    @Test
    public void testGetFilteredSocks_SortingByColor() {
        List<SocksDto> result = socksService.getFilteredSocks(null, null, null, "color");

        // Проверяем, что список отсортирован правильно
        List<String> expectedColors = Arrays.asList("Blue", "Green", "Red", "Red");
        List<String> actualColors = result.stream().map(SocksDto::getColor).collect(Collectors.toList());

        assertEquals(expectedColors, actualColors, "Список носков должен быть отсортирован по цвету");
    }

    //Проверить, что метод сортирует носки по содержанию хлопка
    @Test
    public void testGetFilteredSocks_SortingByCotton() {
        List<SocksDto> result = socksService.getFilteredSocks(null, null, null, "cotton");
        assertEquals(30, result.get(0).getCotton(), "Первый носок должен иметь хлопок 30");
    }

    //Тест на проверку носков с определенным ID, которые существуют в БД
    @Test
    public void testUpdateSocks_Success() {
        Long id = 1L;
        SocksDto updatedSocksDto = new SocksDto("Blue", 60, 150);
        Socks existingSocks = new Socks("Red", 40, 100);

        when(socksRepository.findById(id)).thenReturn(Optional.of(existingSocks)); //Настройка моков

        SocksDto result = socksService.updateSocks(id, updatedSocksDto); //Вызов метода

        assertEquals(updatedSocksDto.getColor(), result.getColor()); //Проверка результатов
        assertEquals(updatedSocksDto.getCotton(), result.getCotton());
        assertEquals(updatedSocksDto.getQuantity(), result.getQuantity());

        verify(socksRepository).save(existingSocks); //Проверка, что метод save был вызван
        assertEquals("Blue", existingSocks.getColor());
        assertEquals(60, existingSocks.getCotton());
        assertEquals(150, existingSocks.getQuantity());
    }

    //Тест на проверку выброса исключения, если носки с определенным ID отсутствуют в БД
    @Test
    public void testUpdateSocks_NotFound() {
        Long id = 2L;
        SocksDto updatedSocksDto = new SocksDto("Green", 50, 200);

        when(socksRepository.findById(id)).thenReturn(Optional.empty()); //Настройка моков

        //Проверка, что исключение выбрасывается
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            socksService.updateSocks(id, updatedSocksDto);
        });

        assertEquals("Носки с ID: " + id + " не найдены", exception.getMessage());

        verify(socksRepository, never()).save(any(Socks.class)); //Проверка, что save не был вызван
    }

    //Проверка на пустой файл
    @Test
    void testProcessBatchOfSocks_EmptyFile() {
        when(file.isEmpty()).thenReturn(true); //Файл пустой
        when(file.getOriginalFilename()).thenReturn("empty.xlsx"); //Указываем имя файла

        // Проверка на исключение
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            socksService.processBatchOfSocks(file);
        });
        assertEquals("Файл не должен быть пустым", exception.getMessage());
    }

    //Проверка на ошибку чтения файла
    @Test
    void testProcessBatchOfSocks_ReadError() throws Exception {
        when(file.isEmpty()).thenReturn(false); //Файл не пустой
        when(file.getInputStream()).thenThrow(new IOException("Ошибка чтения")); //Файл поврежден или отсутствует

        // Проверка на исключение
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            socksService.processBatchOfSocks(file);
        });
        assertEquals("Ошибка при чтении файла", exception.getMessage());
    }
}
