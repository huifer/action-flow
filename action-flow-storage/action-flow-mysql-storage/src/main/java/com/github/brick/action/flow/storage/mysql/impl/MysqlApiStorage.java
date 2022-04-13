/*
 *    Copyright [2022] [brick-team]
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.github.brick.action.flow.storage.mysql.impl;

import com.github.brick.action.flow.method.entity.api.ApiEntity;
import com.github.brick.action.flow.method.entity.api.ApiParamEntity;
import com.github.brick.action.flow.storage.api.ApiStorage;
import com.github.brick.action.flow.storage.mysql.entity.AfApiEntity;
import com.github.brick.action.flow.storage.mysql.repository.AfApiEntityRepository;
import com.github.brick.action.flow.storage.mysql.repository.AfApiParamExEntityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class MysqlApiStorage implements ApiStorage {
    @Autowired(required = false)
    private AfApiEntityRepository afApiEntityRepository;
    @Autowired(required = false)
    private MysqlApiParamStorage apiParamStorage;
    @Autowired(required = false)
    private AfApiParamExEntityRepository afApiParamExEntityRepository;

    @Override
    public Long save(String url, String method, String desc) {
        AfApiEntity entity = new AfApiEntity();
        entity.setUrl(url);
        entity.setMethod(method);
        entity.setDesca(desc);

        AfApiEntity save = afApiEntityRepository.save(entity);
        return save.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveForApi(List<ApiEntity> list, boolean b) {
        for (ApiEntity apiEntity : list) {
            // step1: 保存当前API数据
            Long apiId = this.save(apiEntity.getUrl(), apiEntity.getMethod(), apiEntity.getDesc());
            // step2: 保存参数数据
            List<ApiParamEntity> params = apiEntity.getParams();
            extracted(apiId, params, null);
        }
    }

    private void extracted(Long apiId, List<ApiParamEntity> params, Long pid) {
        for (ApiParamEntity param : params) {
            try {

                // step1: 基础数据保存
                Long save = apiParamStorage.save(apiId, pid, param.getIn() != null ? param.getIn().name() : null, param.getName(), param.isRequire());


                List<ApiParamEntity> paramEntities = param.getParamEntities();
                if (!CollectionUtils.isEmpty(paramEntities)) {
                    extracted(apiId, paramEntities, save);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public ApiEntity findById(Long apiId) {
        Optional<AfApiEntity> byId1 = this.afApiEntityRepository.findById(apiId);
        if (byId1.isPresent()) {
            AfApiEntity byId = byId1.get();
            List<ApiParamEntity> list = this.apiParamStorage.findByAppId(apiId);
            ApiEntity apiEntity = new ApiEntity();
            apiEntity.setId(byId.getId().toString());
            apiEntity.setUrl(byId.getUrl());
            apiEntity.setMethod(byId.getMethod());
            apiEntity.setDesc(byId.getDesca());
            apiEntity.setParams(list);


            return apiEntity;
        }
        return null;

    }

    @Override
    public List<ApiEntity> all() {
        List<AfApiEntity> all = this.afApiEntityRepository.findAll();
        List<ApiEntity> res = new ArrayList<>();
        for (AfApiEntity afApiEntity : all) {
            res.add(findById(afApiEntity.getId()));
        }
        return res;
    }
}
