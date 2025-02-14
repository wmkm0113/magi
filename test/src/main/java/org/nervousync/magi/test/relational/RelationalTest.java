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

package org.nervousync.magi.test.relational;

import jakarta.annotation.Nonnull;
import org.nervousync.brain.configs.BrainConfigure;
import org.nervousync.brain.configs.builder.BrainConfigureBuilder;
import org.nervousync.brain.configs.builder.SchemaConfigBuilder;
import org.nervousync.brain.enumerations.ddl.DDLType;
import org.nervousync.brain.enumerations.query.JoinType;
import org.nervousync.brain.query.QueryInfo;
import org.nervousync.commons.Globals;
import org.nervousync.magi.query.builder.QueryBuilder;
import org.nervousync.magi.test.BaseTest;
import org.nervousync.magi.test.relational.entity.RelationalReference;
import org.nervousync.magi.test.relational.entity.TestRelational;
import org.nervousync.utils.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public abstract class RelationalTest extends BaseTest<TestRelational> {

	protected RelationalTest(@Nonnull final String dialectName, @Nonnull final String jdbcUrl,
	                         @Nonnull final String serverAddress, final int serverPort, final boolean pooled,
	                         final String userName, final String passWord) throws Exception {
		super(generateConfigure(dialectName, jdbcUrl, serverAddress, serverPort, pooled, userName, passWord),
				TestRelational.class, RelationalReference.class);
	}

	@Override
	protected final TestRelational generateRecord() throws IOException {
		return generate(Globals.INITIALIZE_INT_VALUE);
	}

	private TestRelational generate(int index) throws IOException {
		TestRelational testRelational = new TestRelational();
		testRelational.setMsgContent("Message Content " + (index % 3));
		testRelational.setMsgBytes(FileUtils.readFileBytes(FileUtils.getFile("classpath:org/nervousync/magi/test/resource.jpg")));
		testRelational.setMsgTitle("Message title " + (index % 3));
		testRelational.setTestBigDecimal(new BigDecimal(Math.PI));
		testRelational.setTestBoolean(true);
		testRelational.setTestByte((byte) 227);
		testRelational.setTestDate(new Date());
		testRelational.setTestDouble(227d);
		testRelational.setTestFloat(227f);
		testRelational.setTestInt(Short.parseShort("227"));
		testRelational.setTestShort(Short.parseShort("227"));
		testRelational.setTestTime(new Date());
		testRelational.setTestTimestamp(new Date());

		RelationalReference relationalReference = new RelationalReference();
		relationalReference.getCompositeId().setCurrentTime(DateTimeUtils.currentUTCTimeMillis());
		relationalReference.setRefStatue(1);
		testRelational.setRelationalReference(relationalReference);

		return testRelational;
	}

	@Override
	protected final List<TestRelational> generateRecords() throws IOException {
		List<TestRelational> records = new ArrayList<>();
		for (int i = 0; i < 20; i++) {
			records.add(generate(i));
		}
		return records;
	}

	@Override
	protected final void identifyKey(TestRelational recordObject) {
		IDENTIFY_KEY = recordObject.getIdentifyCode();
	}

	@Override
	protected final void verifyRetrieve(TestRelational mainObject) {
		this.logger.info("Test_Retrieve_Record", mainObject.toFormattedJson());
		String md5 = ConvertUtils.toHex(SecurityUtils.SHA256(mainObject.getMsgBytes()));
		this.logger.info("Test_Retrieve_Verify", md5.equals(RESOURCE_VALIDATE));
		this.logger.info("Test_Retrieve_Reference", mainObject.getRelationalReference().toFormattedJson());
	}

	@Override
	protected final TestRelational modifyObject(TestRelational mainObject) {
		this.logger.info("Test_Update_Record", mainObject.toFormattedJson());
		mainObject.setMsgTitle("Update title");
		Optional.ofNullable(mainObject.getRelationalReference())
				.ifPresent(relationalReference -> relationalReference.setRefStatue(2));
		return mainObject;
	}

	@Override
	protected final QueryInfo queryInfo() throws Exception {
		return QueryBuilder.newBuilder(TestRelational.class)
				.joinTable(JoinType.LEFT, TestRelational.class, RelationalReference.class, Globals.DEFAULT_VALUE_STRING)
				.equalTo(RelationalReference.class, "refStatue", 2)
				.configPager(2, 5)
				.useCache(Boolean.TRUE)
				.confirm();
	}

	@Override
	protected final void verifyQueryRecord(int index, TestRelational mainObject) {
		this.logger.info("Test_Query_Record", index, mainObject.toFormattedJson());
	}

	private static BrainConfigure generateConfigure(final String dialectName, final String jdbcUrl,
	                                           final String serverAddress, final int serverPort, final boolean pooled,
	                                           final String userName, final String passWord) throws Exception {
		SchemaConfigBuilder.JdbcConfigBuilder configBuilder = BrainConfigureBuilder.newBuilder()
				.configDDL(DDLType.CREATE)
				.jdbcConfig("JDBC")
				.dialect(dialectName)
				.addServer(serverAddress, serverPort, Globals.INITIALIZE_INT_VALUE)
				.jdbcUrl(jdbcUrl)
				.connectionPool(pooled, 2, 10)
				.lowQuery(1000)
				.timeout(1, 1)
				.retry(3, 500L);
		if (StringUtils.notBlank(userName)) {
			configBuilder.userAuthenticationBuilder().authenticate(userName, passWord)
					.confirmParent(SchemaConfigBuilder.JdbcConfigBuilder.class);
		}
		return configBuilder.confirmParent(BrainConfigureBuilder.class)
				.defaultSchema("JDBC")
				.confirm();
	}
}
