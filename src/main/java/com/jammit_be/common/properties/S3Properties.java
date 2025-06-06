package com.jammit_be.common.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;


public class S3Properties {
    private String bucket;
    private String accessKey;
    private String secretKey;
    private String region;
}
