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

package com.github.brick.action.flow.parse.xml;

import com.github.brick.action.flow.model.execute.FlowExecuteEntity;
import com.github.brick.action.flow.model.xml.FlowXML;
import org.junit.Test;

import java.util.List;

public class FlowXMLParseTest extends XMLParseCommonTest {
    FlowXMLParse flowXMLParse = new FlowXMLParse();

    @Test
    public void parse() throws Exception {
        List<FlowExecuteEntity> parse = flowXMLParse.parse(rootElement);
        log(parse);

    }
}