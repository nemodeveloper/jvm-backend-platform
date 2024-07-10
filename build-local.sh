./gradlew clean &&
./gradlew :core:environment:build :core:environment:publishToMavenLocal &&
./gradlew :core:service:build :core:service:publishToMavenLocal &&
./gradlew :core:spring:build :core:spring:publishToMavenLocal &&
./gradlew :core:extensions:build :core:extensions:publishToMavenLocal &&
./gradlew :core:validation:build :core:validation:publishToMavenLocal &&
./gradlew :core:metrics:build :core:metrics:publishToMavenLocal &&
./gradlew :core:logging:build :core:logging:publishToMavenLocal &&
./gradlew :core:logging-sl4j:build :core:logging-sl4j:publishToMavenLocal &&
./gradlew :core:build-info:build :core:build-info:publishToMavenLocal &&
./gradlew :core:api:build :core:api:publishToMavenLocal &&
./gradlew :core:tracing:build :core:tracing:publishToMavenLocal &&
./gradlew :core:open-api:build :core:open-api:publishToMavenLocal &&
./gradlew :core:exception:build :core:exception:publishToMavenLocal &&
./gradlew :core:async:build :core:async:publishToMavenLocal &&
./gradlew :core:api-exception-handler:build :core:api-exception-handler:publishToMavenLocal &&
./gradlew :core:http-server:build :core:http-server:publishToMavenLocal &&
./gradlew :core:starter:build :core:starter:publishToMavenLocal
./gradlew :db:core:build :db:core:publishToMavenLocal &&
./gradlew :integration:http:build :integration:http:publishToMavenLocal &&
./gradlew :integration:kafka:build :integration:kafka:publishToMavenLocal &&
./gradlew :integration:s3-minio:build :integration:s3-minio:publishToMavenLocal &&
./gradlew :security:oauth2-resource:build :security:oauth2-resource:publishToMavenLocal &&
./gradlew :security:api-key:build :security:api-key:publishToMavenLocal
