package com.cmsr.hik.vision.service;

import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import io.minio.*;
import io.minio.messages.Item;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.*;

@Data
@Slf4j
@Service
@ConfigurationProperties(prefix = "minio.oss")
public class MinioService {
    private MinioClient client;

    private String endpoint;
    private String accessKey;
    private String secretKey;
    private String bucket;

    private void buildClient() {
        log.debug("Building MinIO client with args: END_POINT: {}, AK: {}, SK: {}", this.endpoint, this.accessKey, this.secretKey);
        client = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
        log.debug("MinIO client build success.");
    }

    private void destroyClient() {
        try {
            if (client != null) {
                client.close();
            }
        } catch (Exception e) {
            log.error("<MinIO> client close failed, {}", System.currentTimeMillis());
        }
    }

    public boolean folderExists(String folderName) {
        buildClient();
        try {
            Iterable<Result<Item>> results = client.listObjects(ListObjectsArgs.builder().bucket(bucket).prefix(folderName).build());
            return !Iterables.isEmpty(results);
        } catch (Exception e) {
            log.error("<MinIO> no such folder: {}", folderName);
        } finally {
            destroyClient();
        }
        return false;
    }

    public Map<String, String> getObjects(String folderName) {
        buildClient();

        ListObjectsArgs listObjectsArgs = ListObjectsArgs.builder()
                .bucket(bucket)
                .prefix(folderName)
                .build();
        List<String> filenameList = new ArrayList<>();

        log.info("<MinIO> accessing bucket: {}, folder: {}", bucket, folderName);
        Iterable<Result<Item>> objects = client.listObjects(listObjectsArgs);

        for (Result<Item> itemResult : objects) {
            try {
                Item item = itemResult.get();
                if (!item.isDir()) {
                    filenameList.add(item.objectName());
                }
            } catch (Exception e) {
                log.error("<MinIO> Cannot access file in folder: {}", folderName);
            } finally {
                destroyClient();
            }

        }

        Map<String, String> fileMap = Maps.newHashMap();

        filenameList.forEach(
                filepath -> {
                    String encodedString = this.getFileBase64(filepath);
                    if (encodedString != null) {
                        String filename = filepath.substring(folderName.length());
                        fileMap.put(filename, encodedString);
                        log.info("<MinIO> File: {} encoded success.", filepath);
                    }
                }
        );
        return fileMap;
    }


    public Map<String, String> getAllObjects() {
        buildClient();

        ListObjectsArgs listObjectsArgs = ListObjectsArgs.builder()
                .bucket(bucket)
                .build();
        List<String> filenameList = new ArrayList<>();

        log.info("<MinIO> accessing bucket: {}", bucket);
        Iterable<Result<Item>> objects = client.listObjects(listObjectsArgs);

        for (Result<Item> itemResult : objects) {
            try {
                Item item = itemResult.get();
                if (!item.isDir()) {
                    filenameList.add(item.objectName());
                }
            } catch (Exception e) {
                log.error("<MinIO> Cannot access file");
            } finally {
                destroyClient();
            }

        }

        Map<String, String> fileMap = Maps.newHashMap();

        filenameList.forEach(
                filepath -> {
                    String encodedString = this.getFileBase64(filepath);
                    if (encodedString != null) {
                        String filename = filepath.substring(10);
                        fileMap.put(filename, encodedString);
                        log.info("<MinIO> File: {} encoded success.", filepath);
                    }
                }
        );
        return fileMap;
    }



    public String getFileBase64(String filepath) {
        buildClient();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        GetObjectArgs getObjectArgs = GetObjectArgs.builder()
                .bucket(bucket)
                .object(filepath)
                .build();
        try (InputStream response = client.getObject(getObjectArgs)) {
            byte[] buffer = new byte[4096];
            int bytesRead;

            while ((bytesRead = response.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (Exception e) {
            log.error("<MinIO> Cannot access file: {}", filepath);
            return null;
        } finally {
            destroyClient();
        }
    }
}
