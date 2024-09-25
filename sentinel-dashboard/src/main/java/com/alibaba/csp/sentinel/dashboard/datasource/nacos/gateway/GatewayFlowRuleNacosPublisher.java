package com.alibaba.csp.sentinel.dashboard.datasource.nacos.gateway;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.gateway.GatewayFlowRuleEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.nacos.AbstractNacosPublisher;
import com.alibaba.csp.sentinel.dashboard.datasource.nacos.NacosConfigClientProvider;
import com.alibaba.csp.sentinel.dashboard.datasource.nacos.NacosConfigUtil;
import com.alibaba.csp.sentinel.dashboard.datasource.nacos.NacosProperties;
import com.alibaba.csp.sentinel.datasource.Converter;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 网关流控规则Nacos发布者
 */
@Component("gatewayFlowRuleNacosPublisher")
public class GatewayFlowRuleNacosPublisher extends AbstractNacosPublisher<List<GatewayFlowRuleEntity>> {


    protected GatewayFlowRuleNacosPublisher(NacosConfigClientProvider nacosConfigClientProvider, Converter<List<GatewayFlowRuleEntity>, String> converter, NacosProperties nacosProperties) {
        super(nacosConfigClientProvider, converter, nacosProperties);
    }

    @Override
    protected String getDataIdPostfix() {
        return NacosConfigUtil.GW_FLOW_DATA_ID_POSTFIX;
    }
}