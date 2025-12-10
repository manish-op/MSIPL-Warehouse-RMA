package com.serverManagement.server.management.controller.keyword;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.serverManagement.server.management.request.keyword.GetSubkeywordRequest;
import com.serverManagement.server.management.request.keyword.KeywordRequest;
import com.serverManagement.server.management.request.keyword.UpdateKeywordName;
import com.serverManagement.server.management.request.keyword.UpdateSubKeywordName;
import com.serverManagement.server.management.service.keyword.KeywordService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("api/keyword")
public class KeywordController {

	@Autowired
	private KeywordService keywordService;

	@GetMapping("/keywordList")
	public ResponseEntity<List<String>> getKeyword() {

		return keywordService.getKeyword();
	}

	@PostMapping("/getSubKeyword")
	public ResponseEntity<?> getSubKeywordOnKeyword(@RequestBody GetSubkeywordRequest keyword) {

		return keywordService.getSubKeywordOnKeyword(keyword);
	}

	@PostMapping
	public ResponseEntity<?> addKeyword(HttpServletRequest request, @RequestBody KeywordRequest keywordRequest) {
		return keywordService.addKeyword(request, keywordRequest);
	}

	@PostMapping("/addSubKeyword")
	public ResponseEntity<?> addSubKeyword(HttpServletRequest request, @RequestBody KeywordRequest keywordRequest) {
		return keywordService.addSubKeyword(request, keywordRequest);
	}

	@PutMapping
	public ResponseEntity<?> updateKeywordName(HttpServletRequest request,
			@RequestBody UpdateKeywordName updateKeywordName) {
		return keywordService.updateKeywordName(request, updateKeywordName);
	}

	@PutMapping("/updateSubKeyword")
	public ResponseEntity<?> updateSubKeywordName(HttpServletRequest request,
			@RequestBody UpdateSubKeywordName updateSubKeywordName) {
		return keywordService.updateSubKeyWordName(request, updateSubKeywordName);
	}
}
