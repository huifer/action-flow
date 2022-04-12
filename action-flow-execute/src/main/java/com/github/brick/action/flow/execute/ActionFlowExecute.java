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

package com.github.brick.action.flow.execute;

import com.github.brick.action.flow.execute.extract.Extract;
import com.github.brick.action.flow.execute.extract.ExtractActionFlowFactory;
import com.github.brick.action.flow.execute.http.HttpWorker;
import com.github.brick.action.flow.execute.http.OkHttpWorkerImpl;
import com.github.brick.action.flow.execute.jdk.JDKExecuteService;
import com.github.brick.action.flow.execute.jdk.JDKExecuteServiceImpl;
import com.github.brick.action.flow.method.entity.api.ParamIn;
import com.github.brick.action.flow.method.enums.ActionType;
import com.github.brick.action.flow.method.enums.ExtractModel;
import com.github.brick.action.flow.model.ActionFlowFactory;
import com.github.brick.action.flow.model.execute.*;
import com.github.brick.action.flow.storage.api.nv.ActionExecuteEntityStorage;
import com.github.brick.action.flow.storage.api.nv.FlowExecuteEntityStorage;
import com.github.brick.action.flow.storage.api.nv.ResultExecuteEntityStorage;
import com.google.gson.Gson;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

public class ActionFlowExecute {
    public static final String DROOL = "$.";
    private static final Logger logger = LoggerFactory.getLogger(ActionFlowExecute.class);
    private static final String[] ops = new String[]{
            ">",
            ">=",
            "==",
            "<",
            "<=",
            "&&",
            "||",
    };
    static SpelExpressionParser parser = new SpelExpressionParser();
    private final ActionExecuteEntityStorage actionExecuteEntityStorage;
    private final FlowExecuteEntityStorage flowExecuteEntityStorage;
    private final ResultExecuteEntityStorage resultExecuteEntityStorage;
    private final String fileName;
    private final ActionFlowFactory<ExtractModel, Extract> extractFactory;
    Gson gson = new Gson();
    JDKExecuteService jdkExecuteService = new JDKExecuteServiceImpl();

    public ActionFlowExecute(String fileName,
                             ActionExecuteEntityStorage actionExecuteEntityStorage, FlowExecuteEntityStorage flowExecuteEntityStorage, ResultExecuteEntityStorage resultExecuteEntityStorage) {
        this.fileName = fileName;
        this.actionExecuteEntityStorage = actionExecuteEntityStorage;
        this.flowExecuteEntityStorage = flowExecuteEntityStorage;
        this.resultExecuteEntityStorage = resultExecuteEntityStorage;
        extractFactory = new ExtractActionFlowFactory();
    }

    private static boolean inOps(String aChar) {
        for (String op : ops) {
            if (op.contains(aChar)) {
                return true;
            }
        }
        return false;
    }

    private static boolean inOps(char aChar) {
        for (String op : ops) {
            if (op.contains(Character.toString(aChar))) {
                return true;
            }
        }
        return false;
    }

    private Boolean handlerLeftRight(String s2, ExtractModel elType, Object o) {
        char[] chars = s2.toCharArray();
        Extract extract = this.extractFactory.factory(elType);

        if (s2.contains("(") && s2.contains(")")) {


            StringBuilder sb = new StringBuilder();
            int start = 0;
            int end = 0;
            for (int i = 0; i < chars.length; i++) {
                char aChar = chars[i];
                sb.append(aChar);
                if (aChar == '(') {
                    start = i;
                }
                if (aChar == ')') {
                    end = i;
                }
                if (end != 0) {
                    String s = sb.toString();

                    String substring = s.substring(start + 1, end);
                    String[] s1 = substring.trim().split(" ");
                    if (s1.length == 3) {

                        Boolean value = handlerLeftAndRightCondition(s1[0], s1[1], s1[2], extract, o);
                        s2 = s2.replace(substring, value.toString());
                    }
                    start = 0;
                    end = 0;
                }
            }
            Expression expression = parser.parseExpression(s2);
            EvaluationContext context = new StandardEvaluationContext();
            Boolean value = expression.getValue(context, Boolean.class);
            return value;
        }
        else {
            int start = 0;
            int end = 0;
            for (int i = 0; i < chars.length; i++) {
                char aChar = chars[i];
                if (inOps(aChar)) {
                    start = i;
                    if (inOps(Character.toString(aChar) + chars[i + 1])) {
                        end = i + 1;
                        break;

                    }
                    else {
                        end = i;
                    }
                }
            }

            Boolean value = handlerLeftAndRightCondition(s2.substring(0, start), s2.substring(start, end + 1), s2.substring(end + 1), extract, o);
            return value;
        }

    }

    private Boolean handlerLeftAndRightCondition(String s2, String s21, String s22, Extract extract, Object o) {
        String left = s2;
        String op = s21;
        String right = s22;
        Object leftVal = null;
        Object rightVal = null;
        if (left.contains(DROOL)) {
            leftVal = extract.extract(o, left);
        }
        if (right.contains(DROOL)) {
            rightVal = extract.extract(o, right);
        }


        Expression expression = parser.parseExpression(leftVal + op + rightVal);
        EvaluationContext context = new StandardEvaluationContext();
        Boolean value = expression.getValue(context, Boolean.class);
        return value;
    }

    public String execute(String flowId, String jsonData) {

        FlowExecuteEntity flowExecuteEntity = flowExecuteEntityStorage.getFlow(fileName, flowId);

        ResultExecuteEntity resultExecuteEntity = resultExecuteEntityStorage.getResult(fileName, flowId);

        List<WorkExecuteEntity> works = flowExecuteEntity.getWorks();

        Map<String, Object> stepWorkResult = new HashMap<>(32);
        for (WorkExecuteEntity work : works) {
            executeWork(jsonData, stepWorkResult, work);
        }


        return null;
    }

    private void executeWork(String jsonData, Map<String, Object> stepWorkResult, WorkExecuteEntity work) {
        String refId = work.getRefId();
        // 执行action
        Object o = executeAction(fileName, jsonData, refId);
        // 放入步骤结果容器
        stepWorkResult.put(work.getStep(), o);
        // 处理watcher标签
        List<WatcherExecuteEntity> watchers = work.getWatchers();
        for (WatcherExecuteEntity watcher : watchers) {
            ExtractModel elType = watcher.getElType();
            String condition = watcher.getCondition();
            Boolean aBoolean = handlerLeftRight(condition, elType, o);
            if (aBoolean) {
                List<WorkExecuteEntity> then = watcher.getThen();
                for (WorkExecuteEntity workExecuteEntity : then) {
                    executeWork(jsonData, stepWorkResult, workExecuteEntity);
                }
            }
            else {
                List<WorkExecuteEntity> cat = watcher.getCat();
                for (WorkExecuteEntity workExecuteEntity : cat) {
                    executeWork(jsonData, stepWorkResult, workExecuteEntity);
                }
            }
        }
    }

    private Object executeAction(String fileName, String jsonData, Serializable refId) {
        ActionExecuteEntity actionExecuteEntity = this.actionExecuteEntityStorage.getAction(fileName, refId);
        ActionType type = actionExecuteEntity.getType();
        Object o = null;
        if (type == ActionType.JAVA_METHOD) {
            try {
                o = handlerJavaMethod(actionExecuteEntity, jsonData);
            } catch (Exception e) {
                logger.error("执行action异常", e);
            }
        }
        else if (type == ActionType.REST_API) {
            try {

                o = handlerRestApi(actionExecuteEntity, jsonData);
            } catch (Exception e) {
                logger.error("执行action异常", e);
            }
        }
        return o;
    }

    private Object handlerRestApi(ActionExecuteEntity actionExecuteEntity, String jsonData) throws IOException {
        ActionExecuteEntity.ForRestApi restApi = actionExecuteEntity.getRestApi();

        String url = restApi.getUrl();
        String method = restApi.getMethod();


        List<ParamExecuteEntity> param = restApi.getParam();
        // todo: 支持HTTPCLIENT选择
        HttpWorker httpWorker = new OkHttpWorkerImpl();


        Map<String, String> queryParam = new HashMap<>(32);
        Map<String, String> pathParam = new HashMap<>(32);
        Map<String, String> headers = new HashMap<>(32);
        Map<String, String> formatData = new HashMap<>(32);
        Map<String, String> body = new HashMap<>(32);

        for (ParamExecuteEntity paramExecuteEntity : param) {
            ParamExecuteEntity.ForRestApi restApi1 = paramExecuteEntity.getRestApi();
            handlerRestApiParam(jsonData, queryParam, pathParam, headers, formatData, body, restApi1);

        }


        String work = httpWorker.work(url, method, pathParam, queryParam, headers, formatData, body);
        return work;
    }

    private void handlerRestApiParam(String jsonData, Map<String, String> queryParam, Map<String, String> pathParam, Map<String, String> headers, Map<String, String> formatData, Map<String, String> body, ParamExecuteEntity.ForRestApi restApi1) {
        ParamIn in = restApi1.getIn();
        ExtractExecuteEntity extract = restApi1.getExtract();
        String name = restApi1.getName();
        String value = restApi1.getValue();


        if (in == ParamIn.header) {
            handlerDataMap(value, extract, jsonData, headers, name);
        }
        else if (in == ParamIn.formdata) {
            handlerDataMap(value, extract, jsonData, formatData, name);
        }
        else if (in == ParamIn.body) {
            handlerDataMap(value, extract, jsonData, body, name);
        }
        else if (in == ParamIn.path) {
            handlerDataMap(value, extract, jsonData, pathParam, name);
        }
        else if (in == ParamIn.query) {
            handlerDataMap(value, extract, jsonData, queryParam, name);
        }

        // todo: 子集参数处理
        List<ParamExecuteEntity.ForRestApi> restApis = restApi1.getRestApis();
        for (ParamExecuteEntity.ForRestApi api : restApis) {
            List<ParamExecuteEntity.ForRestApi> restApis1 = api.getRestApis();
            for (ParamExecuteEntity.ForRestApi forRestApi : restApis1) {
                handlerRestApiParam(jsonData, queryParam, pathParam, headers, formatData, body, forRestApi);
            }
        }
    }

    private void handlerDataMap(String value, ExtractExecuteEntity extract, String jsonData, Map<String, String> dataMap, String name) {
        if (strValueIsNull(value)) {
            Serializable fromAction = extract.getFromAction();
            String el = extract.getEl();
            Extract factory = extractFactory.factory(extract.getElType());
            Object o = executeAction(fileName, jsonData, fromAction);
            Object exData = factory.extract(o, el);
            dataMap.put(name, gson.toJson(exData));
        }
        else {
            dataMap.put(name, value);
        }
    }

    private boolean strValueIsNull(String value) {
        return "".equals(value) || value == null;
    }

    private Object handlerJavaMethod(ActionExecuteEntity actionExecuteEntity, String jsonData) throws Exception {

        ActionExecuteEntity.ForJavaMethod javaMethod = actionExecuteEntity.getJavaMethod();
        String className = javaMethod.getClassName();
        String method = javaMethod.getMethod();
        List<ParamExecuteEntity> param = javaMethod.getParam();


        List<IndexAndType> indexAndTypes = new ArrayList<>(8);
        Object[] args = new Object[param.size()];
        for (int i = 0; i < param.size(); i++) {
            ParamExecuteEntity paramExecuteEntity = param.get(i);
            ParamExecuteEntity.ForJavaMethod javaMethod1 = paramExecuteEntity.getJavaMethod();

            IndexAndType e = new IndexAndType();
            e.setIndex(javaMethod1.getIndex());
            e.setTyp(javaMethod1.getType());

            indexAndTypes.add(e);
            String value = javaMethod1.getValue();
            if (strValueIsNull(value)) {
                ExtractExecuteEntity extract = javaMethod1.getExtract();
                Serializable fromAction = extract.getFromAction();
                String el = extract.getEl();
                Extract factory = extractFactory.factory(extract.getElType());
                Object o = executeAction(fileName, jsonData, fromAction);
                Object exData = factory.extract(o, el);
                args[i] = exData;
            }
            else {
                args[i] = value;
            }


        }

        indexAndTypes.sort(Comparator.comparing(IndexAndType::getIndex));

        List<String> paramTypes = indexAndTypes.stream().map(IndexAndType::getTyp).collect(Collectors.toList());

        return jdkExecuteService.execute(className, method, paramTypes.toArray(new String[]{}), args);
    }

    @Data
    private static class IndexAndType {
        private Integer index;
        private String typ;
    }

}