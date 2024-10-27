package com.example.demo.services;

import com.example.demo.dto.ObjectDto;
import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class MinioService {

    @Autowired
    private MinioClient minioClient;

    public void createBucket(String bucketName) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        minioClient.makeBucket(
                MakeBucketArgs.builder()
                        .bucket(bucketName)
                        .build()
        );
    }

    public void uploadObjects(List<SnowballObject> objects) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        checkBucket("user-files");
        minioClient.uploadSnowballObjects(
                UploadSnowballObjectsArgs.builder().bucket("user-files").objects(objects).build()
        );
    }

    public void removeObject(String objectName) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        minioClient.removeObject(RemoveObjectArgs.builder().bucket("user-files").object(objectName).build());
    }

    public void removeFolder(String folderName) throws Exception {
        Iterable<Result<Item>> results = getAllFilesInDirectory(folderName);

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
        minioClient.copyObject(
                CopyObjectArgs.builder()
                        .bucket("user-files")
                        .object(newObjectName)
                        .source(
                                CopySource.builder()
                                        .bucket("user-files")
                                        .object(oldObjectName)
                                        .build()
                        ).build()
        );
    }

    public void checkBucket(String bucketName) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        if (!found) {
            createBucket(bucketName);
        }
    }

    public void createFolder(String folderName) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket("user-files")
                        .object(folderName + "/")  // Объект с завершающим "/"
                        .stream(new ByteArrayInputStream(new byte[]{}), 0, -1)  // Пустой поток
                        .build()
        );
    }

    public Iterable<Result<Item>> listObject(String path) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        return minioClient.listObjects(ListObjectsArgs.builder().bucket("user-files").prefix(path).build());
    }

    public Iterable<Result<Item>> listObjectAll(String path) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        return minioClient.listObjects(ListObjectsArgs.builder().bucket("user-files").prefix(path).recursive(true).build());
    }

    public void copyFolder(String oldObjectName, String newObjectName) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        checkBucket("user-files");
        //Получить все файлы рекурсивно
        Iterable<Result<Item>> results = getAllFilesInDirectory(oldObjectName);
        List<String> oldObjects = new ArrayList<>();
        List<String> newObjects = new ArrayList<>();
        List<String> emptyFolders = new ArrayList<>();

        for (Result<Item> result : results) {
            if (result.get().etag() != null && !result.get().objectName().endsWith("/")) {
                String oldName = result.get().objectName();
                String newName = oldName.replace(oldObjectName, newObjectName);
                oldObjects.add(oldName);
                newObjects.add(newName);

                minioClient.copyObject(
                        CopyObjectArgs.builder()
                                .bucket("user-files")
                                .object(newName)
                                .source(
                                        CopySource.builder()
                                                .bucket("user-files")
                                                .object(oldName)
                                                .build()
                                ).build()
                );
            }else{
                String oldName = result.get().objectName();
                String newName = oldName.replace(oldObjectName, newObjectName);
                emptyFolders.add(newName);
            }
        }

        //Создание пустых папок
        if(!emptyFolders.isEmpty()){
            for (String folder : emptyFolders) {
                createFolder(folder);
            }
        }
    }

    private Iterable<Result<Item>> getAllFilesInDirectory(String folderName) {
        Iterable<Result<Item>> results = minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket("user-files")
                        .prefix(folderName + "/")
                        .recursive(true)
                        .build()
        );
        return results;
    }

    public List<ObjectDto> searchAllFiles(int ID, String searchQuery) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        Iterable<Result<Item>> results = listObjectAll("user-" + ID + "-files/");
        List<ObjectDto> objects = new ArrayList<>(StreamSupport.stream(results.spliterator(), false)
                .map(item -> {
                    try {
                        String itemPath = item.get().objectName();
                        // фильтруем папки-объекты
                        if (!itemPath.endsWith("/")) {
                            //Надо определить папка или нет
                            String itemName = List.of(itemPath.split("/")).getLast();
                            int indexName = itemPath.indexOf(itemName);
                            itemPath = itemPath.substring(0, indexName);
                            if (itemName.contains(searchQuery)) {
                                return ObjectDto.builder().name(itemName).path(itemPath).encodePath(URLEncoder.encode(itemPath+itemName+'/', StandardCharsets.UTF_8)).isDirectory(false).build();
                            }
                        }
                    } catch (Exception e) {
                        // Логируем ошибку, чтобы не потерять ее
                    }
                    return null; // Возвращаем null в случае ошибки, чтобы отфильтровать их далее
                })
                .filter(Objects::nonNull) // Фильтруем null значения, которые возникли из-за ошибок
                .collect(Collectors.toList()));
        return objects;
    }

    public List<ObjectDto> searchAllFolders(int ID, String searchQuery) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        Iterable<Result<Item>> results = listObject("user-" + ID + "-files/");

        List<ObjectDto> objects = new ArrayList<>(StreamSupport.stream(results.spliterator(), false)
                .map(item -> {
                    try {
                        String itemPath = item.get().objectName();
                        String itemEtag= item.get().etag();
                        // фильтруем папки-объекты
                        if (itemPath.endsWith("/")&&itemEtag==null) {
                            //Надо определить папка или нет
                            String itemName = List.of(itemPath.split("/")).getLast();
                            int indexName = itemPath.indexOf(itemName);
                            itemPath = itemPath.substring(0, indexName);
                            if (itemName.contains(searchQuery)) {
                                return ObjectDto.builder().name(itemName).path(itemPath).encodePath(URLEncoder.encode(itemPath+itemName+'/', StandardCharsets.UTF_8)).isDirectory(true).build();
                            }
                        }
                    } catch (Exception e) {
                        // Логируем ошибку, чтобы не потерять ее
                    }
                    return null; // Возвращаем null в случае ошибки, чтобы отфильтровать их далее
                })
                .filter(Objects::nonNull) // Фильтруем null значения, которые возникли из-за ошибок
                .collect(Collectors.toList()));
        return objects;
    }

    public List<ObjectDto> getFromPath(String path) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        Iterable<Result<Item>> results = listObject(path);
        List<ObjectDto> fileObjects = new ArrayList<>();
        List<ObjectDto> folderObjects = new ArrayList<>();
        List<ObjectDto> objects = new ArrayList<>();

        StreamSupport.stream(results.spliterator(), false)
                .forEach(item -> {
                    try {
                        String itemPath = item.get().objectName();
                        String itemEtag = item.get().etag();
                        // фильтруем папки-объекты
                        if (itemPath.endsWith("/") && itemEtag == null) {
                            //Надо определить папка или нет
                            //какойто тупой срез идёт, а надо просто узнать и сделать сред
                            String itemName = List.of(itemPath.split("/")).getLast();
                            int indexName = itemPath.lastIndexOf(itemName);
                            itemPath = itemPath.substring(0, indexName);
                            folderObjects.add(ObjectDto.builder().name(itemName).path(itemPath).encodePath(URLEncoder.encode(itemPath+itemName+'/', StandardCharsets.UTF_8)).isDirectory(true).build());
                        } else {
                            if (!itemPath.endsWith("/")) {
                                String itemName = List.of(itemPath.split("/")).getLast();
                                int indexName = itemPath.indexOf(itemName);
                                itemPath = itemPath.substring(0, indexName);
                                fileObjects.add(ObjectDto.builder().name(itemName).path(itemPath).encodePath(URLEncoder.encode(itemPath+itemName+'/', StandardCharsets.UTF_8)).isDirectory(false).build());
                            }
                        }
                    } catch (Exception e) {
                        // Логируем ошибку, чтобы не потерять ее
                    }
                });
        objects.addAll(folderObjects);
        objects.addAll(fileObjects);
        return objects;
    }

    //проверка на пустоту папки
    public void reCreateFolder(String path) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        List<ObjectDto> objects = getFromPath(path);
        if(objects.isEmpty()){
            int index = path.lastIndexOf("/");
            path=path.substring(0,index);
            createFolder(path);
        }
    }
}
