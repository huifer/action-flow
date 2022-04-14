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

package com.github.brick.action.flow.core.context;

import com.github.brick.action.flow.method.content.ActionFlowXmlContext;
import com.github.brick.action.flow.model.enums.StorageType;
import org.junit.Test;

public class ActionFlowContentTest {

    @Test
    public void init() {

        ActionFlowXmlContext content = new ActionFlowXmlContext(StorageType.MEMORY,"flow.xml");
        content.execute("flow.xml", "1", "");


    }

}