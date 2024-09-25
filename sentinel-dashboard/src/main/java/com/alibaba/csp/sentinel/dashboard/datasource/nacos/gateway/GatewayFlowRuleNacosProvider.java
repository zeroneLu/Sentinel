package com.alibaba.csp.sentinel.dashboard.datasource.nacos.gateway;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.gateway.GatewayFlowRuleEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.nacos.AbstractNacosProvider;
import com.alibaba.csp.sentinel.dashboard.datasource.nacos.NacosConfigClientProvider;
import com.alibaba.csp.sentinel.dashboard.datasource.nacos.NacosConfigUtil;
import com.alibaba.csp.sentinel.dashboard.datasource.nacos.NacosProperties;
import com.alibaba.csp.sentinel.datasource.Converter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 网关流控规则Nacos持有者
 */
@Slf4j
@Component
public class GatewayFlowRuleNacosProvider extends AbstractNacosProvider<List<GatewayFlowRuleEntity>> {


    protected GatewayFlowRuleNacosProvider(NacosConfigClientProvider nacosConfigClientProvider, Converter<String, List<GatewayFlowRuleEntity>> converter, NacosProperties nacosProperties) {
        super(nacosConfigClientProvider, converter, nacosProperties);
    }

    @Override
    protected String getDataIdPostfix() {
        return NacosConfigUtil.GW_FLOW_DATA_ID_POSTFIX;
    }
}