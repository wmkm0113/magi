/*
 * Licensed to the Nervousync Studio (NSYC) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.nervousync.magi.test;

import org.junit.jupiter.api.*;
import org.nervousync.enumerations.logger.LogLevel;
import org.nervousync.magi.data.DataUtils;
import org.nervousync.utils.logger.LoggerUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DataUtilsTest {

	static {
		LoggerUtils.initLoggerConfigure(LogLevel.DEBUG);
	}

	@Test
	@Order(0)
	public void generator() throws Exception {
		try (DataUtils.DataGenerator dataGenerator = DataUtils.newGenerator("D:\\generator.dat")) {
			AtomicInteger counter = new AtomicInteger(0);
			this.dataList().forEach(dataMap -> {
				String tableName = "Test_Table_" + (counter.getAndIncrement() % 4);
				dataGenerator.appendData(Boolean.FALSE, tableName, dataMap);
			});
		}
	}

	private List<Map<String, Object>> dataList() {
		List<Map<String, Object>> dataList = new ArrayList<>();
		for (int i = 0; i < 100; i++) {
			Map<String, Object> dataMap = new HashMap<>();
			for (int j = 0; j < 10; j++) {
				dataMap.put("column" + j, "data" + j + "." + i);
			}
			dataList.add(dataMap);
		}
		return dataList;
	}
}
