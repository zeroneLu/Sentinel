package com.alibaba.csp.sentinel.dashboard.datasource.nacos.gateway;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.gateway.ApiDefinitionEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.nacos.AbstractNacosProvider;
import com.alibaba.csp.sentinel.dashboard.datasource.nacos.NacosConfigClientProvider;
import com.alibaba.csp.sentinel.dashboard.datasource.nacos.NacosConfigUtil;
import com.alibaba.csp.sentinel.dashboard.datasource.nacos.NacosProperties;
import com.alibaba.csp.sentinel.datasource.Converter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 网关api分组规则Nacos持有者；从naocs获取到配置
 * @author modongning
 * @createDate 2022/11/14
 */
@Slf4j
@Component
public class GatewayApiNacosProvider extends AbstractNacosProvider<List<ApiDefinitionEntity>> {


    protected GatewayApiNacosProvider(NacosConfigClientProvider nacosConfigClientProvider, Converter<String, List<ApiDefinitionEntity>> converter, NacosProperties nacosProperties) {
        super(nacosConfigClientProvider, converter, nacosProperties);
    }

    @Override
    protected String getDataIdPostfix() {
        return NacosConfigUtil.FLOW_DATA_ID_POSTFIX;
    }
}
