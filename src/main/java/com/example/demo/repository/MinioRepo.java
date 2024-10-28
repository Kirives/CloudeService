package com.example.demo.repository;

import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.Item;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Repository
public class MinioRepo {

    @Autowired
    private MinioClient minioClient;

    public void createBucket(String bucketName) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        minioClient.makeBucket(
                MakeBucketArgs.builder()
                        .bucket(bucketName)
                        .build()
        );
    }

    public void removeObject(String objectName) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        minioClient.removeObject(RemoveObjectArgs.builder().bucket("user-files").object(objectName).build());
    }

    public Iterable<Result<Item>> getObjects(String path) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        return minioClient.listObjects(ListObjectsArgs.builder().bucket("user-files").prefix(path).build());
    }

    public Iterable<Result<Item>> getObjectsRecursive(String path) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        return minioClient.listObjects(ListObjectsArgs.builder().bucket("user-files").prefix(path + "/").recursive(true).build());
    }

    public void copyObject(String oldObjectName, String newObjectName) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
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

    public void createFolder(String folderName) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket("user-files")
                        .object(folderName + "/")  // Объект с завершающим "/"
                        .stream(new ByteArrayInputStream(new byte[]{}), 0, -1)  // Пустой поток
                        .build()
        );
    }
}
