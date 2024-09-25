package com.alibaba.csp.sentinel.dashboard.datasource.nacos;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

//只有存在NacosProperties实体的时候才启用
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(NacosProperties.class)
public class NacosConfiguration {

    @Bean
    public NacosConfigClientProvider nacosConfigClientProvider(NacosProperties nacosProperties, BeanFactory beanFactory, ObjectMapper objectMapper){
        return new NacosConfigClientProvider(nacosProperties,beanFactory,objectMapper);
    }


}
