package com.example.demo.controllers;

import com.example.demo.models.security.UsersDetails;
import com.example.demo.services.MinioService;
import com.example.demo.services.StringServices;
import io.minio.SnowballObject;
import io.minio.errors.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping
public class MinioController {

    @Autowired
    private MinioService minioService;

    @Autowired
    private StringServices stringServices;

    @PostMapping("/createFolder")
    public String createFolder(@AuthenticationPrincipal UsersDetails usersDetails, @RequestParam("folderName") String folderName, @RequestParam("currentUrl") String currentUrl) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        String pathParameter = stringServices.getLastSub(currentUrl,"path=");
        pathParameter = URLDecoder.decode(pathParameter, StandardCharsets.UTF_8);

        minioService.createFolder(pathParameter+folderName);
        String pathParamEncode = URLEncoder.encode(pathParameter, StandardCharsets.UTF_8);
        return "redirect::"+usersDetails.getID()+"?path="+pathParamEncode;
    }

    @PostMapping("/uploadFile")
    public String uploadFile(@AuthenticationPrincipal UsersDetails usersDetails, @RequestParam("file") MultipartFile[] multipartFiles, @RequestParam("currentUrl") String currentUrl) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        String pathParameter = stringServices.getLastSub(currentUrl,"path=");
        List<SnowballObject> snowballObjects = new ArrayList<>();
        for (MultipartFile multipartFile : multipartFiles) {
            snowballObjects.add(new SnowballObject(pathParameter+multipartFile.getOriginalFilename(),new ByteArrayInputStream(multipartFile.getBytes()),multipartFile.getSize(),null));
        }
        minioService.uploadObjects(snowballObjects);
        String pathParamEncode = URLEncoder.encode(pathParameter, StandardCharsets.UTF_8);
        return "redirect::"+usersDetails.getID()+"?path="+pathParamEncode;
    }

    @PostMapping("/copyFolderSearch")
    public String copyFolderSearch(@AuthenticationPrincipal UsersDetails usersDetails,@RequestParam("nameFolder") String nameFolder ,@RequestParam("pathFolder")String pathFolder, @RequestParam(value = "newFileName")String newName) throws Exception {
        minioService.copyFolder(pathFolder+nameFolder,pathFolder+newName);
        minioService.removeFolder(pathFolder+nameFolder);
        String pathParamEncode = URLEncoder.encode(pathFolder, StandardCharsets.UTF_8);
        return "redirect::"+usersDetails.getID()+"?path="+pathParamEncode;
    }


    @PostMapping("/copyFileSearch")
    public String copySomethingSearch(@AuthenticationPrincipal UsersDetails usersDetails,@RequestParam("fileName") String fileName ,@RequestParam("filePath")String filePath, @RequestParam(value = "newFileName")String newName) throws Exception {
        String exp = "."+stringServices.getLastSub(fileName,".");
        newName=newName+exp;
        minioService.copyObject(filePath+fileName,filePath+newName);
        minioService.removeObject(filePath+fileName);
        String pathParamEncode = URLEncoder.encode(filePath, StandardCharsets.UTF_8);
        return "redirect::"+usersDetails.getID()+"?path="+pathParamEncode;
    }

    @PostMapping("/deleteFolderSearch")
    public String deleteFolderSearch(@AuthenticationPrincipal UsersDetails usersDetails, @RequestParam("nameFolder") String nameFolder , @RequestParam("pathFolder")String pathFolder) throws Exception {
        minioService.removeFolder(pathFolder+nameFolder);
        minioService.reCreateFolder(pathFolder);
        String pathParamEncode = URLEncoder.encode(pathFolder, StandardCharsets.UTF_8);
        return "redirect::"+usersDetails.getID()+"?path="+pathParamEncode;
    }

    @PostMapping("/deleteFileSearch")
    public String deleteFileSearch(@AuthenticationPrincipal UsersDetails usersDetails,@RequestParam("fileName") String fileName ,@RequestParam("filePath")String filePath) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        minioService.removeObject(filePath+fileName);
        minioService.reCreateFolder(filePath);
        String pathParamEncode = URLEncoder.encode(filePath, StandardCharsets.UTF_8);
        return "redirect::"+usersDetails.getID()+"?path="+pathParamEncode;
    }
}
