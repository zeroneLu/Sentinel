package com.alibaba.csp.sentinel.dashboard.datasource.nacos.gateway;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.gateway.ApiDefinitionEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.nacos.AbstractNacosPublisher;
import com.alibaba.csp.sentinel.dashboard.datasource.nacos.NacosConfigClientProvider;
import com.alibaba.csp.sentinel.dashboard.datasource.nacos.NacosConfigUtil;
import com.alibaba.csp.sentinel.dashboard.datasource.nacos.NacosProperties;
import com.alibaba.csp.sentinel.datasource.Converter;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 网关api分组规则Nacos发布者
 */
@Component
public class GatewayApiNacosPublisher extends AbstractNacosPublisher<List<ApiDefinitionEntity>> {


    protected GatewayApiNacosPublisher(NacosConfigClientProvider nacosConfigClientProvider, Converter<List<ApiDefinitionEntity>, String> converter, NacosProperties nacosProperties) {
        super(nacosConfigClientProvider, converter, nacosProperties);
    }

    @Override
    protected String getDataIdPostfix() {
        return NacosConfigUtil.FLOW_DATA_ID_POSTFIX;
    }
}
