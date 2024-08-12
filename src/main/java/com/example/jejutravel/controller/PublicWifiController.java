package com.example.jejutravel.controller;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
// import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.example.jejutravel.global.PythonTranslator;
import com.example.jejutravel.global.api.ApiResponse;

@RestController
@Slf4j
@RequestMapping("/api/v1")
@RequiredArgsConstructor

public class PublicWifiController {
    
    @Value("${jeju.api.key}")
	private String jejuApiKey;

    @GetMapping("/publicWifi")
    public ApiResponse<?> callApi(
		@RequestParam(value = "pageNo", defaultValue = "1") int pageNo
	){
        StringBuilder result = new StringBuilder();
        try {
            String apiUrl = "https://open.jejudatahub.net/api/proxy/Dtb18ta1btbD1Da1a81aaDttab6tDabb/" +
            jejuApiKey + "?" +
            "number=" + pageNo +
            "&limit=5";

            URL url = new URL(apiUrl);
			HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setRequestMethod("GET");
			urlConnection.setRequestProperty("Content-type", "application/json");

            BufferedReader rd;
			if (urlConnection.getResponseCode() >= 200 && urlConnection.getResponseCode() <= 300) {
				rd = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "UTF-8"));
			} else {
				rd = new BufferedReader(new InputStreamReader(urlConnection.getErrorStream(), "UTF-8"));
			}
            String line;
			while ((line = rd.readLine()) != null) {
				result.append(line);
			}
			rd.close();
            urlConnection.disconnect();
			
			 // JSON 파싱 및 재구성
			ObjectMapper objectMapper = new ObjectMapper();
			JsonNode rootNode = objectMapper.readTree(result.toString());
			JsonNode dataArray = rootNode.path("data");

			List<Map<String, String>> filteredData = new ArrayList<>();
			for (JsonNode dataNode : dataArray) {
				Map<String, String> newItem = new HashMap<>();

				newItem.put("latitude", dataNode.path("latitude").asText());
				newItem.put("longitude", dataNode.path("longitude").asText());
				
				// 한국어 -> 중국어 번역
				String apGroupNameKo = dataNode.path("apGroupName").asText();
				String apGroupNameZh = PythonTranslator.translate(apGroupNameKo, "ko", "zh-cn");
				newItem.put("apGroupName", apGroupNameZh);
				// newItem.put("apGroupName", dataNode.path("apGroupName").asText());

				String installLocationDetailKo = dataNode.path("installLocationDetail").asText();
				String installLocationDetailZh = PythonTranslator.translate(installLocationDetailKo, "ko", "zh-cn");
				newItem.put("installLocationDetail", installLocationDetailZh);
				// newItem.put("installLocationDetail", dataNode.path("installLocationDetail").asText());

				String categoryKo = dataNode.path("category").asText();
				String categoryZh = PythonTranslator.translate(categoryKo, "ko", "zh-cn");
				newItem.put("category", categoryZh);
				// newItem.put("category", dataNode.path("category").asText());
				
				String categoryDetailKo = dataNode.path("categoryDetail").asText();
				String categoryDetailZh = PythonTranslator.translate(categoryDetailKo, "ko", "zh-cn");
				newItem.put("categoryDetail", categoryDetailZh);
				// newItem.put("categoryDetail", dataNode.path("categoryDetail").asText());
				
				String addressDongKo = dataNode.path("addressDong").asText();
				String addressDongZh = PythonTranslator.translate(addressDongKo, "ko", "zh-cn");
				newItem.put("addressDong", addressDongZh);
				// newItem.put("addressDong", dataNode.path("addressDong").asText());
				
				String addressDetailKo = dataNode.path("addressDetail").asText();
				String addressDetailZh = PythonTranslator.translate(addressDetailKo, "ko", "zh-cn");
				newItem.put("addressDetail", addressDetailZh);
				// newItem.put("addressDetail", dataNode.path("addressDetail").asText());

				filteredData.add(newItem);
			}

  			 // 객체를 직접 반환
			return ApiResponse.createSuccessWithMessage(filteredData, "공공와이파이 목록 조회 성공했습니다.");

        }catch (Exception e) {
			e.printStackTrace();
			return ApiResponse.createError("(공공와이파이 /publicWifi)API 호출 중 오류가 발생했습니다.");
		}
    }

	//publicWifi 검색
	@GetMapping("/publicWifi/search")
    public ApiResponse<?> searchPublicWifi(
		@RequestParam("apGroupName") String apGroupName, //검색할 apGroupName 받아옴.
		@RequestParam(value = "pageNo", defaultValue = "1") int pageNo
		) { 
        StringBuilder result = new StringBuilder();
        try {
			// 중국어로 된 파라미터를 한국어로 번역
			String translatedApGroupName = PythonTranslator.translate(apGroupName, "zh-cn", "ko");
			// String translatedStationName = "한국병원";

			String encodedApGroupName = URLEncoder.encode(translatedApGroupName, StandardCharsets.UTF_8.toString()); //검색한 apGroupName 인코딩
            String apiUrl = "https://open.jejudatahub.net/api/proxy/Dtb18ta1btbD1Da1a81aaDttab6tDabb/" +
            jejuApiKey + "?" +
            "apGroupName=" + encodedApGroupName +						
            "&number=" + pageNo +
            "&limit=5";

            URL url = new URL(apiUrl);
			HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setRequestMethod("GET");
			urlConnection.setRequestProperty("Content-type", "application/json");

            BufferedReader rd;
			if (urlConnection.getResponseCode() >= 200 && urlConnection.getResponseCode() <= 300) {
				rd = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "UTF-8"));
			} else {
				rd = new BufferedReader(new InputStreamReader(urlConnection.getErrorStream(), "UTF-8"));
			}
            String line;
			while ((line = rd.readLine()) != null) {
				result.append(line);
			}
			rd.close();
            urlConnection.disconnect();
			
			 // JSON 파싱 및 재구성
			ObjectMapper objectMapper = new ObjectMapper();
			JsonNode rootNode = objectMapper.readTree(result.toString());
			JsonNode dataArray = rootNode.path("data");

			List<Map<String, String>> filteredData = new ArrayList<>();
			for (JsonNode dataNode : dataArray) {
				Map<String, String> newItem = new HashMap<>();

				newItem.put("latitude", dataNode.path("latitude").asText());
				newItem.put("longitude", dataNode.path("longitude").asText());
				
				// 한국어 -> 중국어 번역
				String apGroupNameKo = dataNode.path("apGroupName").asText();
				String apGroupNameZh = PythonTranslator.translate(apGroupNameKo, "ko", "zh-cn");
				newItem.put("apGroupName", apGroupNameZh);
				// newItem.put("apGroupName", dataNode.path("apGroupName").asText());

				String installLocationDetailKo = dataNode.path("installLocationDetail").asText();
				String installLocationDetailZh = PythonTranslator.translate(installLocationDetailKo, "ko", "zh-cn");
				newItem.put("installLocationDetail", installLocationDetailZh);
				// newItem.put("installLocationDetail", dataNode.path("installLocationDetail").asText());

				String categoryKo = dataNode.path("category").asText();
				String categoryZh = PythonTranslator.translate(categoryKo, "ko", "zh-cn");
				newItem.put("category", categoryZh);
				// newItem.put("category", dataNode.path("category").asText());
				
				String categoryDetailKo = dataNode.path("categoryDetail").asText();
				String categoryDetailZh = PythonTranslator.translate(categoryDetailKo, "ko", "zh-cn");
				newItem.put("categoryDetail", categoryDetailZh);
				// newItem.put("categoryDetail", dataNode.path("categoryDetail").asText());
				
				String addressDongKo = dataNode.path("addressDong").asText();
				String addressDongZh = PythonTranslator.translate(addressDongKo, "ko", "zh-cn");
				newItem.put("addressDong", addressDongZh);
				// newItem.put("addressDong", dataNode.path("addressDong").asText());
				
				String addressDetailKo = dataNode.path("addressDetail").asText();
				String addressDetailZh = PythonTranslator.translate(addressDetailKo, "ko", "zh-cn");
				newItem.put("addressDetail", addressDetailZh);
				// newItem.put("addressDetail", dataNode.path("addressDetail").asText());
				
				filteredData.add(newItem);
			}

  			 // 객체를 직접 반환
			return ApiResponse.createSuccessWithMessage(filteredData, "공공와이파이 목록 검색 성공했습니다.");

        }catch (Exception e) {
			e.printStackTrace();
			return ApiResponse.createError("(공공와이파이 /publicWifi)API 호출 중 오류가 발생했습니다.");
		}
    }
}
