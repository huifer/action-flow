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

import com.github.brick.action.flow.model.xml.ParamXML;
import org.dom4j.Element;
import org.junit.Test;

import java.util.List;

public class ParamXMLForRestApiParseTest extends XMLParseCommonTest{
    ParamXMLForRestApiParse parse = new ParamXMLForRestApiParse();
    @Test
    public void parse() throws Exception {
        Element actions = this.rootElement.element("actions");
        List<Element> action = actions.elements("action");
        Element element = action.get(0);
        Element param = element.element("param");
        ParamXML parse = this.parse.parse(param);
        log(parse);

    }
}