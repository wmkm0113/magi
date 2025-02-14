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

package org.nervousync.magi.test.distribute.impl;

import org.junit.jupiter.api.AfterEach;
import org.nervousync.brain.configs.server.ServerInfo;
import org.nervousync.commons.Globals;
import org.nervousync.magi.test.distribute.DistributeTest;

import java.util.List;

public final class CassandraTest extends DistributeTest {

	public CassandraTest() throws Exception {
		super("Cassandra", Globals.DEFAULT_VALUE_STRING, serverList(),
				Boolean.FALSE, "nervousync", "ns0528AO@!");
	}

	@AfterEach
	public void delay() throws InterruptedException {
		Thread.sleep(2000L);
	}

	private static List<ServerInfo> serverList() {
		ServerInfo serverInfo = new ServerInfo();
		serverInfo.setServerName("datacenter1");
		serverInfo.setServerAddress("192.168.166.51");
		serverInfo.setServerPort(9042);
		serverInfo.setServerLevel(1);
		return List.of(serverInfo);
	}
}
