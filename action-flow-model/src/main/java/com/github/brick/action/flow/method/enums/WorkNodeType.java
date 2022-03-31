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

package com.github.brick.action.flow.method.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 工作节点类型
 */
@AllArgsConstructor
@Getter
public enum WorkNodeType {
    /**
     * 开始节点
     */
    START(1L),
    /**
     * 成功节点
     */
    THEN(2L),

    /**
     * 失败节点
     */
    CAT(3L),
    ;
    private Long id;

}