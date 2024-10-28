package com.example.demo.services;

import com.example.demo.dto.ObjectDto;
import com.example.demo.repository.MinioRepo;
import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.StreamSupport;

@Service
public class MinioService {

    @Autowired
    private MinioClient minioClient;

    @Autowired
    private MinioRepo minioRepo;

    public void uploadObjects(List<SnowballObject> objects) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        checkBucket("user-files");
        minioClient.uploadSnowballObjects(
                UploadSnowballObjectsArgs.builder().bucket("user-files").objects(objects).build()
        );
    }

    public void removeObject(String objectName) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        minioRepo.removeObject(objectName);
    }

    public void removeFolder(String folderName) throws Exception {
        Iterable<Result<Item>> results = minioRepo.getObjectsRecursive(folderName);

        List<DeleteObject> objectsToDelete = new ArrayList<>();
        for (Result<Item> result : results) {
            objectsToDelete.add(new DeleteObject(result.get().objectName()));
        }

        if (!objectsToDelete.isEmpty()) {
            Iterable<Result<DeleteError>> errors = minioClient.removeObjects(
                    RemoveObjectsArgs.builder()
                            .bucket("user-files")
                            .objects(objectsToDelete)
                            .build()
            );

            for (Result<DeleteError> error : errors) {
                System.err.println("Ошибка при удалении объекта: " + error.get().objectName());
            }
        }
    }

    public void copyObject(String oldObjectName, String newObjectName) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        checkBucket("user-files");
        minioRepo.copyObject(oldObjectName, newObjectName);
    }

    public void checkBucket(String bucketName) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        if (!found) {
            minioRepo.createBucket(bucketName);
        }
    }

    public void createFolder(String folderName) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        minioRepo.createFolder(folderName);
    }

    public void copyFolder(String oldObjectName, String newObjectName) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        checkBucket("user-files");
        //Получить все файлы рекурсивно
        Iterable<Result<Item>> results = minioRepo.getObjectsRecursive(oldObjectName);
        List<String> emptyFolders = new ArrayList<>();

        for (Result<Item> result : results) {
            if (result.get().etag() != null && !result.get().objectName().endsWith("/")) {
                String oldName = result.get().objectName();
                String newName = oldName.replace(oldObjectName, newObjectName);
                minioRepo.copyObject(oldName, newName);
            }else{
                String oldName = result.get().objectName();
                String newName = oldName.replace(oldObjectName, newObjectName);
                emptyFolders.add(newName);
            }
        }
        //Создание пустых папок
        if(!emptyFolders.isEmpty()){
            for (String folder : emptyFolders) {
                minioRepo.createFolder(folder);
            }
        }
    }

    public List<ObjectDto> getFromSearch(int ID, Predicate<String> searchQuery) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        Iterable<Result<Item>> results = minioRepo.getObjectsRecursive("user-" + ID + "-files");
        return getObjectDto(results, searchQuery);
    }

    public List<ObjectDto> getFromPath(String path) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        Iterable<Result<Item>> results = minioRepo.getObjects(path);
        return getObjectDto(results, null);
    }

    private List<ObjectDto> getObjectDto(Iterable<Result<Item>> results,Predicate<String> searchQuery){
        List<ObjectDto> fileObjects = new ArrayList<>();
        List<ObjectDto> folderObjects = new ArrayList<>();
        List<ObjectDto> objects = new ArrayList<>();
        StreamSupport.stream(results.spliterator(), false)
                .forEach(item -> {
                    try {
                        String itemPath = item.get().objectName();
                        String itemEtag = item.get().etag();
                        String itemName = List.of(itemPath.split("/")).getLast();
                        if(searchQuery!=null&&!searchQuery.test(itemName)){

                        }else{
                            if (itemPath.endsWith("/") && itemEtag == null) {
                                folderObjects.add(createObjectDto(itemPath,itemName,true));
                            } else {
                                if (!itemPath.endsWith("/")) {
                                    fileObjects.add(createObjectDto(itemPath,itemName,false));
                                }
                            }
                        }
                    } catch (Exception e) {
                    }
                });
        objects.addAll(folderObjects);
        objects.addAll(fileObjects);
        return objects;
    }

    private ObjectDto createObjectDto(String itemPath,String itemName,boolean isFolder){
        int indexName = itemPath.lastIndexOf(itemName);
        itemPath = itemPath.substring(0, indexName);
        if(isFolder){
            return ObjectDto.builder().name(itemName).path(itemPath).encodePath(URLEncoder.encode(itemPath+itemName+'/', StandardCharsets.UTF_8)).isDirectory(true).build();
        }else{
            return ObjectDto.builder().name(itemName).path(itemPath).encodePath(URLEncoder.encode(itemPath+itemName+'/', StandardCharsets.UTF_8)).isDirectory(false).build();
        }
    }

    //проверка на пустоту папки
    public void reCreateFolder(String path) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        List<ObjectDto> objects = getFromPath(path);
        if(objects.isEmpty()){
            int index = path.lastIndexOf("/");
            path=path.substring(0,index);
            minioRepo.createFolder(path);
        }
    }
}
