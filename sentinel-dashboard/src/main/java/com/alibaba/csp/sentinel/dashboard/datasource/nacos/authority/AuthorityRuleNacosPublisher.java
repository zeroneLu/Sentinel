/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.dashboard.datasource.nacos.authority;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.AuthorityRuleEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.nacos.AbstractNacosPublisher;
import com.alibaba.csp.sentinel.dashboard.datasource.nacos.NacosConfigClientProvider;
import com.alibaba.csp.sentinel.dashboard.datasource.nacos.NacosConfigUtil;
import com.alibaba.csp.sentinel.dashboard.datasource.nacos.NacosProperties;
import com.alibaba.csp.sentinel.datasource.Converter;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author itmuch.com
 */
@Component("authorityRuleNacosPublisher")
public class AuthorityRuleNacosPublisher extends AbstractNacosPublisher<List<AuthorityRuleEntity>> {


    protected AuthorityRuleNacosPublisher(NacosConfigClientProvider nacosConfigClientProvider, Converter<List<AuthorityRuleEntity>, String> converter, NacosProperties nacosProperties) {
        super(nacosConfigClientProvider, converter, nacosProperties);
    }

    @Override
    protected String getDataIdPostfix() {
        return NacosConfigUtil.AUTHORITY_DATA_ID_POSTFIX;
    }
}
