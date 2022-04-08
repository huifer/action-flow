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

package com.github.brick.action.flow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class LoginAction {
    private static final Logger logger = LoggerFactory.getLogger(LoginAction.class);

    public Map<String, Object> login(String username, String password) {
        if (logger.isInfoEnabled()) {
            logger.info("login,username = {}, password = {}", username, password);
        }
//        if (true) {
//            throw new RuntimeException("aaa");
//
//        }

        HashMap<String, Object> stringStringHashMap = new HashMap<>();
        stringStringHashMap.put("username","zhangsan" + username);
        stringStringHashMap.put("age", "18");
        stringStringHashMap.put("login_time", LocalDate.now());

        return stringStringHashMap;
    }
}
