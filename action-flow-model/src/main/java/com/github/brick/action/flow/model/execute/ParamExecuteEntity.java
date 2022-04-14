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

package com.github.brick.action.flow.model.execute;

import com.github.brick.action.flow.model.enums.ParamIn;
import lombok.Data;

import java.util.List;

@Data
public class ParamExecuteEntity {

    private ForRestApi restApi;
    private ForJavaMethod javaMethod;

    @Data
    public static class ForRestApi {
        private ParamIn in;
        private String name;
        private boolean require;
        private String value;
        private ExtractExecuteEntity extract;

        private List<ForRestApi> restApis;

    }

    @Data
    public static class ForJavaMethod {

        private Integer index;
        private String type;
        private String name;
        private String value;
        private ExtractExecuteEntity extract;

        private List<ForJavaMethod> restApis;


    }

}
