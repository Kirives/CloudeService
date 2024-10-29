package com.example.demo.controllers;

import com.example.demo.dto.ObjectDto;
import com.example.demo.models.security.UsersDetails;
import com.example.demo.services.MinioService;
import io.minio.errors.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

@Controller
@RequestMapping("/search")
public class SearchController {

    @Autowired
    private MinioService minioService;

    @PostMapping("")
    public String search2(@AuthenticationPrincipal UsersDetails usersDetails, @RequestParam("searchQuery") String searchQuery, Model model, HttpServletRequest request) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        String paramQuery = searchQuery;
        String pathParamEncode = URLEncoder.encode(paramQuery, StandardCharsets.UTF_8);
        return "redirect:/search"+"?searchQuery="+pathParamEncode;
    }

    @GetMapping("")
    public String searchMage(@AuthenticationPrincipal UsersDetails usersDetails, Model model,@RequestParam("searchQuery") String searchQuery,HttpServletRequest request) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(request.getRequestURL().toString().replace("search",String.valueOf(usersDetails.getID())));
        builder.queryParam("path", "");
        model.addAttribute("currentUrl", builder.toUriString());
        List<ObjectDto> objectDtoList = new ArrayList<>();
        Predicate<String> searchQueryPredicate = name -> name.contains(searchQuery);
        objectDtoList.addAll(minioService.getFromSearch(usersDetails.getID(),searchQueryPredicate));
        model.addAttribute("objectDtoList", objectDtoList);
        model.addAttribute("user", usersDetails);
        return"searchPage";
    }
}
