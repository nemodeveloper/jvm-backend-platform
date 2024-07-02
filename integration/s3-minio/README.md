# Библиотека интеграции с s3 хранилищем minio

## Подключение библиотеки gradle

    implementation("ru.nemodev.platform:core-integration-s3-minio")

## Changelogs


## Использование

### Application.yml конфигурация
``` yaml 
platform:
  integration:
    s3-minio:
      access-key: ${S3_MINIO_ACCESS_KEY:admin}
      secret-key: ${S3_MINIO_SECRET_KEY:admin1234}
      bucket: ${S3_MINIO_BUCKET:platform-awesome-template}
      file-content-type: ${S3_MINIO_FILE_CONTENT_TYPE:application/pdf}
      http-client:
        url: ${S3_MINIO_URL:http://127.0.0.1:9000}
```
### Springboot конфигурация
После указания настроек выше в spring context появятся bean
``` kotlin
// Для http запросов через корутины, при вызове методов по умолчанию подставляется bucket и file-content-type из настройки
minioS3Client: MinioS3Client
// Если вам не хватает методов оберток используйте minioS3Client.minioClient напрямую или добавьте метод обертку в minioS3Client
```
