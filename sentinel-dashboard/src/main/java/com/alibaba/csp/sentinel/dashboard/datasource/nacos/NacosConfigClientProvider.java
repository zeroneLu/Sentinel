package com.alibaba.csp.sentinel.dashboard.datasource.nacos;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.*;
import com.alibaba.csp.sentinel.dashboard.repository.rule.RuleRepository;
import com.alibaba.csp.sentinel.dashboard.util.YamlPropertyLoader;
import com.alibaba.csp.sentinel.util.function.Function;
import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.config.ConfigFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import lombok.Getter;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.context.properties.PropertyMapper;
import org.springframework.core.ResolvableType;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

public class NacosConfigClientProvider implements InitializingBean, DisposableBean {

    protected final Logger log = LoggerFactory.getLogger(getClass());


    private static final String SENTINEL_DATA_SOURCE = "sentinel-namespace-config.yaml";

    private static final String FLOW = "flow";

    private static final String SYSTEM = "system";

    private static final String DEGRADE = "degrade";

    private static final String PARAM = "param";

    private static final String AUTHORITY = "authority";


    private final Function<String, String> flowRule = x -> x + NacosConfigUtil.FLOW_DATA_ID_POSTFIX;

    private final Function<String, String> systemRule = x -> x + NacosConfigUtil.SYSTEM_DATA_ID_POSTFIX;

    private final Function<String, String> degradeRule = x -> x + NacosConfigUtil.DEGRADE_DATA_ID_POSTFIX;

    private final Function<String, String> paramRule = x -> x + NacosConfigUtil.PARAM_FLOW_DATA_ID_POSTFIX;

    private final Function<String, String> authorityRule = x -> x + NacosConfigUtil.AUTHORITY_DATA_ID_POSTFIX;

    private final Map<String, Class<?>> ruleTypeCache = Map.of(
            FLOW, FlowRuleEntity.class,
            SYSTEM, SystemRuleEntity.class,
            DEGRADE, DegradeRuleEntity.class,
            PARAM, ParamFlowRuleEntity.class,
            AUTHORITY, AuthorityRuleEntity.class
    );


    @Getter
    private SentinelNacosSourceProperties properties;


    private final ConfigService configService;


    private final NacosProperties nacosProperties;


    private final Map<String, ConfigService> configServiceMap;


    private final BeanFactory beanFactory;


    private final ObjectMapper objectMapper;


    public NacosConfigClientProvider(NacosProperties nacosProperties, BeanFactory beanFactory, ObjectMapper objectMapper) {
        this.nacosProperties = nacosProperties;
        this.beanFactory = beanFactory;
        this.objectMapper = objectMapper;
        this.configServiceMap = new ConcurrentHashMap<>();
        this.configService = createNacosConfigService(nacosProperties.getNamespace());

    }

    public ConfigService getConfigService(String app) {
        return configServiceMap.get(app);
    }



    private Listener createListener() {
        return new PropertyListener();
    }




    private void createRuleListener(ConfigService configService, String appName) {

        String flowDataId = flowRule.apply(appName);
        String systemDataId = systemRule.apply(appName);
        String degradeDataId = degradeRule.apply(appName);
        String paramDataId = paramRule.apply(appName);
        String authorityDataId = authorityRule.apply(appName);
        try {
            configService.addListener(flowDataId,nacosProperties.getGroup(), new RulePropertyListener(FLOW,flowDataId));
            configService.addListener(systemDataId,nacosProperties.getGroup(), new RulePropertyListener(SYSTEM,systemDataId));
            configService.addListener(degradeDataId,nacosProperties.getGroup(), new RulePropertyListener(DEGRADE,degradeDataId));
            configService.addListener(paramDataId,nacosProperties.getGroup(), new RulePropertyListener(PARAM,paramDataId));
            configService.addListener(authorityDataId,nacosProperties.getGroup(), new RulePropertyListener(AUTHORITY,authorityDataId));
        } catch (NacosException e) {
            log.error("create rule listener error", e);
        }
    }


    private <R> ObjectProvider<RuleRepository<R, Long>> getRuleRepository(String appType) {

        Class<?> aClass = ruleTypeCache.get(appType);
        if (aClass == null) {
            return null;
        }
        ResolvableType beanType = ResolvableType.forClassWithGenerics(RuleRepository.class, aClass, Long.class);
        return beanFactory.getBeanProvider(beanType);
    }


    class RulePropertyListener implements Listener {


        private final String appType;


        private final String appName;


        RulePropertyListener(String appType, String appName) {
            this.appType = appType;
            this.appName = appName;
        }


        @Override
        public Executor getExecutor() {
            return null;
        }

        @Override
        public void receiveConfigInfo(String configInfo) {

            if (StringUtils.isBlank(configInfo)){
                return;
            }
            Class<?> aClass = ruleTypeCache.get(appType);
            if (aClass == null) {
                return;
            }

            try {
                CollectionType collectionType = objectMapper.getTypeFactory().constructCollectionType(List.class, aClass);
                Object o = objectMapper.readValue(configInfo, collectionType);
                ObjectProvider<RuleRepository<Object, Long>> ruleRepository = getRuleRepository(appType);
                if (ruleRepository != null) {
                    ruleRepository.ifAvailable(r -> {
                        List<Object> rules = r.findAllByApp(appName);
                        if (CollectionUtils.isEmpty(rules)) {
                            r.save(o);
                            return;
                        }
                        String local = JSON.toJSONString(rules);
                        String s = DigestUtils.md5Hex(local);
                        String s1 = DigestUtils.md5Hex(configInfo);
                        if (!s.equals(s1)){
                            r.save(o);
                        }
                    });
                }
            } catch (Exception e) {
                log.error("refresh rule error", e);
            }
        }
    }


    class PropertyListener implements Listener {


        @Override
        public Executor getExecutor() {
            return null;
        }

        @Override
        public void receiveConfigInfo(String configInfo) {
            try {
                refreshConfiguration(configInfo);
            } catch (Exception e) {
                log.error("refresh dataId [{}] error, {}", SENTINEL_DATA_SOURCE, configInfo, e);
            }
        }
    }


    private void initialConfiguration() {

        try {
            String configInfo =
                    configService
                            .getConfig(SENTINEL_DATA_SOURCE, nacosProperties.getGroup(), nacosProperties.getTimeout());
            refreshConfiguration(configInfo);
        } catch (Exception e) {
            log.error("add configuration dataId [{}] error", SENTINEL_DATA_SOURCE, e);
        }
    }

    private void refreshConfiguration(String configInfo) {
        if (StringUtils.isBlank(configInfo)) {
            log.warn("remove config:{}", SENTINEL_DATA_SOURCE);
            return;
        }
        this.properties = YamlPropertyLoader.loadAs("sentinel-service", configInfo, SentinelNacosSourceProperties.class);

        List<SentinelNacosSourceProperties.AppSource> namespaces = properties.getSource();
        if (CollectionUtils.isEmpty(namespaces)) {
            destroyConfigServiceClient(configServiceMap.keySet());
            return;
        }

        if (!CollectionUtils.isEmpty(configServiceMap)) {
            Set<String> keys = namespaces.stream().map(SentinelNacosSourceProperties.AppSource::getName).collect(Collectors.toSet());
            Set<String> excludeApp = configServiceMap.keySet().stream().filter(k -> !keys.contains(k)).collect(Collectors.toSet());
            destroyConfigServiceClient(excludeApp);
        }
        // 刷新客户端
        namespaces.forEach((n) -> configServiceMap.computeIfAbsent(n.getName(), k -> {
            ConfigService nacosConfigService = createNacosConfigService(n.getNamespace());
            createRuleListener(nacosConfigService,n.getName());
            return nacosConfigService;
        }));
    }


    private void destroyConfigServiceClient(Set<String> excludeApp) {

        if (CollectionUtils.isEmpty(excludeApp)) {
            return;
        }

        for (String app : excludeApp) {
            ConfigService destroyApp = configServiceMap.remove(app);
            try {
                destroyApp.shutDown();
            } catch (NacosException e) {
                log.error("shutdown [{}] nacos client:error", app, e);
            }
        }

    }


    private ConfigService createNacosConfigService(String namespace) {

        Properties properties = new Properties();
        PropertyMapper map = PropertyMapper.get().alwaysApplyingWhenNonNull();
        map.from(namespace).to(properties.in(PropertyKeyConst.NAMESPACE));
        map.from(nacosProperties::getServerAddr).to(properties.in(PropertyKeyConst.SERVER_ADDR));
        map.from(nacosProperties::getSecretKey).to(properties.in(PropertyKeyConst.SECRET_KEY));
        map.from(nacosProperties::getAccessKey).to(properties.in(PropertyKeyConst.ACCESS_KEY));
        map.from(nacosProperties::getUsername).to(properties.in(PropertyKeyConst.USERNAME));
        map.from(nacosProperties::getPassword).to(properties.in(PropertyKeyConst.PASSWORD));
        try {
            return ConfigFactory.createConfigService(properties);
        } catch (NacosException e) {
            throw new IllegalStateException(e);
        }
    }

    private static class Properties extends java.util.Properties {

        <V> java.util.function.Consumer<V> in(String key) {
            return (value) -> put(key, value);
        }


    }


    @Override
    public void afterPropertiesSet() {
        //
        initialConfiguration();
        // register Listener
        try {
            configService
                    .addListener(SENTINEL_DATA_SOURCE, nacosProperties.getGroup(), createListener());
        } catch (NacosException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void destroy() {
        destroyConfigServiceClient(configServiceMap.keySet());
    }


}
