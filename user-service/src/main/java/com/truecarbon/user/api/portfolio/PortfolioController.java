package com.truecarbon.user.api.portfolio;

import com.truecarbon.user.api.portfolio.PortfolioDtos.*;
import com.truecarbon.user.security.UserPrincipal;
import com.truecarbon.user.service.PortfolioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PortfolioController {

    private final PortfolioService portfolioService;

    @GetMapping("/portfolio")
    public ResponseEntity<List<HoldingResponse>> myHoldings(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(portfolioService.getHoldings(principal.getId()));
    }
}
