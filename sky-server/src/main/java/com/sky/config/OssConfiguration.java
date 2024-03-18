package com.sky.config;

import com.sky.properties.AliOssProperties;
import com.sky.utils.AliOssUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Select;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

/**
 * ClassName: OssConfiguration
 * Package: com.sky.config
 * Description:
 * Create: 2024/3/7 - 16:42
 * 配置类 用于创建Aliossutionduix
 */

@Configuration
@Slf4j
public class OssConfiguration
{
    @Bean
    @ConditionalOnMissingBean
    public AliOssUtil aliOssUtil(AliOssProperties aliOssProperties)
    {
        log.info("开始创建阿里云上传工具类对象");

        return  new AliOssUtil(aliOssProperties.getEndpoint() ,
                aliOssProperties.getAccessKeyId(),
                aliOssProperties.getAccessKeySecret() ,
                aliOssProperties.getBucketName()) ;
    }
}
