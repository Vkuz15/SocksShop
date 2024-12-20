package com.shop.socks.service;

import com.shop.socks.dto.LotOfSocksDto;
import com.shop.socks.dto.SocksDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface SocksService {

    LotOfSocksDto addSocks(SocksDto socksDto);

    LotOfSocksDto removeSocks(SocksDto socksDto);

    List<SocksDto> getFilteredSocks(String color, Integer minCotton, Integer maxCotton, String sortBy);

    SocksDto updateSocks(Long id, SocksDto socksDto);

    LotOfSocksDto processBatchOfSocks(MultipartFile file);
}
