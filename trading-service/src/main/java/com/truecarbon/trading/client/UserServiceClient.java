package com.truecarbon.trading.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Component
@Slf4j
public class UserServiceClient {

	private final RestTemplate rest = new RestTemplate();
	private final String baseUrl;
	private final String apiKey;

	public UserServiceClient(@Value("${user-service.base-url}") String baseUrl,
			@Value("${user-service.internal-api-key}") String apiKey) {
		this.baseUrl = baseUrl;
		this.apiKey = apiKey;
	}

	public CertInfo getCertificate(UUID certId) {
		try {
			return rest.exchange(baseUrl + "/internal/certificates/" + certId, HttpMethod.GET, entity(null),
					CertInfo.class).getBody();
		} catch (Exception e) {
			log.warn("getCertificate failed: {}", e.getMessage());
			return null;
		}
	}

	public HoldingInfo getHolding(UUID userId, UUID certId) {
		try {
			return rest.exchange(baseUrl + "/internal/portfolio/" + userId + "/" + certId, HttpMethod.GET, entity(null),
					HoldingInfo.class).getBody();
		} catch (Exception e) {
			log.warn("getHolding failed: {}", e.getMessage());
			return null;
		}
	}

	public void creditPortfolio(UUID userId, UUID certId, String certTitle, BigDecimal credits, BigDecimal price) {
		try {
			rest.exchange(baseUrl + "/internal/portfolio/credit", HttpMethod.POST, entity(Map.of("userId", userId,
					"certificateId", certId, "certificateTitle", certTitle, "credits", credits, "price", price)),
					Void.class);
		} catch (Exception e) {
			log.error("creditPortfolio failed: {}", e.getMessage());
		}
	}

	public void debitPortfolio(UUID userId, UUID certId, BigDecimal credits) {
		try {
			rest.exchange(baseUrl + "/internal/portfolio/debit", HttpMethod.POST,
					entity(Map.of("userId", userId, "certificateId", certId, "credits", credits)), Void.class);
		} catch (Exception e) {
			log.error("debitPortfolio failed: {}", e.getMessage());
		}
	}

	public void reserveCredits(UUID userId, UUID certId, BigDecimal credits) {
		rest.exchange(baseUrl + "/internal/portfolio/reserve", HttpMethod.POST,
				entity(Map.of("userId", userId, "certificateId", certId, "credits", credits)), Void.class);
	}

	public void releaseCredits(UUID userId, UUID certId, BigDecimal credits) {
		try {
			rest.exchange(baseUrl + "/internal/portfolio/release", HttpMethod.POST,
					entity(Map.of("userId", userId, "certificateId", certId, "credits", credits)), Void.class);
		} catch (Exception e) {
			log.error("releaseCredits failed: {}", e.getMessage());
		}
	}

	private <T> HttpEntity<T> entity(T body) {
		var h = new HttpHeaders();
		h.set("X-Internal-Key", apiKey);
		h.setContentType(MediaType.APPLICATION_JSON);
		return body != null ? new HttpEntity<>(body, h) : new HttpEntity<>(h);
	}

	public record CertInfo(String id, String title, BigDecimal totalCredits, BigDecimal availableCredits, String status,
			String ownerId) {
	}

	public record HoldingInfo(String id, String certificateId, BigDecimal creditsHeld, BigDecimal creditsReserved) {
	}
}
