package com.truecarbon.trading.api.trade;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.truecarbon.trading.api.Dtos.PricePointResponse;
import com.truecarbon.trading.service.PriceHistoryService;

import lombok.RequiredArgsConstructor;

@CrossOrigin
@RestController
@RequestMapping("/api/prices")
@RequiredArgsConstructor
class PriceController {

    private final PriceHistoryService priceHistoryService;

    @GetMapping("/history")
    public ResponseEntity<List<PricePointResponse>> history(
            @RequestParam(defaultValue = "60") int days) {
        if (days > 365) days = 365;
        List<PricePointResponse> result = priceHistoryService.getHistory(days)
            .stream()
            .map(p -> new PricePointResponse(
                p.getRecordedOn().toString(),
                p.getOpenPrice(), p.getHighPrice(),
                p.getLowPrice(), p.getClosePrice(),
                p.getVolume(), p.getTradeCount()
            ))
            .toList();
        return ResponseEntity.ok(result);
    }
}