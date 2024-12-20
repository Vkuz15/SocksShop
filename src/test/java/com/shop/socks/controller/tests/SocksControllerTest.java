package com.shop.socks.controller.tests;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shop.socks.controller.SocksController;
import com.shop.socks.dto.LotOfSocksDto;
import com.shop.socks.dto.SocksDto;
import com.shop.socks.service.impl.SocksServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

public class SocksControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private SocksServiceImpl socksServiceImpl;

    @InjectMocks
    private SocksController socksController;


    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(socksController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    public void testArrivalOfSocks() throws Exception {
        SocksDto socksDto = new SocksDto("Red", 50, 100);
        String jsonContent = objectMapper.writeValueAsString(socksDto);

        when(socksServiceImpl.addSocks(any(SocksDto.class))) //Настройка мок
                .thenReturn(new LotOfSocksDto("Red", 50, 100));

        mockMvc.perform(post("/api/socks/income")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonContent))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.cotton").value(50)) //Ожидаемое значение для cotton
                .andExpect(jsonPath("$.quantity").value(100));
    }

    @Test
    public void testDepartureOfSocks_Success() throws Exception {
        //Подготовка данных для теста
        SocksDto socksDto = new SocksDto("Red", 50, 100);
        LotOfSocksDto lotOfSocksDto = new LotOfSocksDto("Red", 50, 50); //Ожидаемый результат

        //Настройка поведения мок сервиса
        when(socksServiceImpl.removeSocks(any(SocksDto.class))).thenReturn(lotOfSocksDto);

        //Выполнение запроса и проверка результата
        mockMvc.perform(post("/api/socks/outcome")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(socksDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.color").value("Red"))
                .andExpect(jsonPath("$.cotton").value(50))
                .andExpect(jsonPath("$.quantity").value(50));
    }

    @Test
    public void testDepartureOfSocks_Failure() throws Exception {
        //Подготовка данных для теста
        SocksDto socksDto = new SocksDto("Red", 50, 100);

        //Настройка поведения мок сервиса для возврата null
        when(socksServiceImpl.removeSocks(any(SocksDto.class))).thenReturn(null);

        //Выполнение запроса и проверка результата
        mockMvc.perform(post("/api/socks/outcome")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(socksDto)))
                .andExpect(status().isBadRequest());
    }

    //Проверяет успешное получение отфильтрованных носков по цвету
    @Test
    public void testAllFilteredSocks_Success() throws Exception {
        //Подготовка данных для теста
        SocksDto socks1 = new SocksDto("Red", 100, 50);
        SocksDto socks2 = new SocksDto("Red", 80, 30);
        List<SocksDto> filteredSocks = Arrays.asList(socks1, socks2);

        //Настройка поведения мок сервиса
        when(socksServiceImpl.getFilteredSocks("Red", null, null, null))
                .thenReturn(filteredSocks);

        //Выполнение запроса и проверка результата
        mockMvc.perform(get("/api/socks")
                        .param("color", "Red")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].color").value("Red"))
                .andExpect(jsonPath("$[0].cotton").value(100))
                .andExpect(jsonPath("$[1].color").value("Red"))
                .andExpect(jsonPath("$[1].cotton").value(80));
    }

    //Проверяет ситуацию, когда носки не найдены по указанному цвету
    @Test
    public void testAllFilteredSocks_NoSocksFound() throws Exception {
        //Настройка поведения мок сервиса для возврата пустого списка
        when(socksServiceImpl.getFilteredSocks("Blue", null, null, null))
                .thenReturn(Arrays.asList());

        //Выполнение запроса и проверка результата
        mockMvc.perform(get("/api/socks")
                        .param("color", "Blue")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    //Проверяет ситуацию, когда обязательный параметр color не передан
    @Test
    public void testAllFilteredSocks_InvalidColor() throws Exception {
        //Проверка на отсутствие обязательного параметра color
        mockMvc.perform(get("/api/socks")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    //Проверяет фильтрацию носков по цвету и диапазону содержания хлопка
    @Test
    public void testAllFilteredSocks_WithCottonFilter() throws Exception {
        //Подготовка данных для теста
        SocksDto socks1 = new SocksDto("Green", 100, 50);
        List<SocksDto> filteredSocks = Arrays.asList(socks1);

        //Настройка поведения мок сервиса
        when(socksServiceImpl.getFilteredSocks("Green", 50, 100, null))
                .thenReturn(filteredSocks);

        //Выполнение запроса и проверка результата
        mockMvc.perform(get("/api/socks")
                        .param("color", "Green")
                        .param("minCotton", "50")
                        .param("maxCotton", "100")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].color").value("Green"))
                .andExpect(jsonPath("$[0].cotton").value(100));
    }

    @Test
    public void testUpdateSocks_Success() throws Exception {
        // Подготовка данных для теста
        Long sockId = 1L;
        SocksDto socksDto = new SocksDto("Blue", 60, 50);
        SocksDto updatedSocks = new SocksDto("Blue", 60, 150);

        // Настройка поведения мок-сервиса
        when(socksServiceImpl.updateSocks(sockId, socksDto)).thenReturn(updatedSocks);

        // Выполнение запроса и проверка результата
        mockMvc.perform(put("/api/socks/{id}", sockId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(socksDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.color").value("Blue"))
                .andExpect(jsonPath("$.cotton").value(60))
                .andExpect(jsonPath("$.quantity").value(150)); // Замените на реальные свойства
    }

}
