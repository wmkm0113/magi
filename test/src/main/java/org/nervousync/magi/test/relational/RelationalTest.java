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
import org.nervousync.brain.configs.builder.BrainConfigureBuilder;
import org.nervousync.brain.configs.builder.SchemaConfigBuilder;
import org.nervousync.brain.enumerations.ddl.DDLType;
import org.nervousync.brain.enumerations.query.JoinType;
import org.nervousync.brain.query.QueryInfo;
import org.nervousync.builder.ParentBuilder;
import org.nervousync.commons.Globals;
import org.nervousync.enumerations.beans.StringType;
import org.nervousync.enumerations.security.EncodeType;
import org.nervousync.magi.config.MagiConfigure;
import org.nervousync.magi.config.builder.MagiConfigureBuilder;
import org.nervousync.magi.query.builder.EntityQueryBuilder;
import org.nervousync.magi.test.BaseTest;
import org.nervousync.magi.test.relational.entity.RelationalReference;
import org.nervousync.magi.test.relational.entity.TestRelational;
import org.nervousync.utils.core.BeanUtils;
import org.nervousync.utils.core.DateTimeUtils;
import org.nervousync.utils.core.FileUtils;
import org.nervousync.utils.core.StringUtils;
import org.nervousync.utils.security.SecurityUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * <h2 class="en-US">Abstract class of relational database test instance</h2>
 * <h2 class="zh-CN">关系型数据库测试抽象类</h2>
 *
 * @author Steven Wee	<a href="mailto:wmkm0113@gmail.com">wmkm0113@gmail.com</a>
 * @version $Revision: 1.0.0 $ $Date: Nov 18, 2022 15:22:27 $
 */
public abstract class RelationalTest extends BaseTest<TestRelational> {

	protected RelationalTest(@Nonnull final String dialectName, @Nonnull final String jdbcUrl,
	                         @Nonnull final String serverAddress, final int serverPort,
	                         @Nonnull final String catalog, final boolean pooled,
	                         final String userName, final String passWord) throws Exception {
		super(generateConfigure(dialectName, jdbcUrl, serverAddress, serverPort, catalog, pooled, userName, passWord),
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
		testRelational.setTestDouble(227d);
		testRelational.setTestFloat(227f);
		testRelational.setTestInt(Short.parseShort("227"));
		testRelational.setTestShort(Short.parseShort("227"));

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
		this.logger.info("Test_Retrieve_Record", BeanUtils.objectToString(mainObject, StringType.JSON));
		String sha256 = SecurityUtils.SHA256(mainObject.getMsgBytes(), EncodeType.HEX);
		this.logger.info("Test_Retrieve_Verify", RESOURCE_VALIDATE.equalsIgnoreCase(sha256));
		this.logger.info("Test_Retrieve_Reference", BeanUtils.objectToString(mainObject.getRelationalReference(), StringType.JSON));
	}

	@Override
	protected final TestRelational modifyObject(TestRelational mainObject) {
		this.logger.info("Test_Update_Record", BeanUtils.objectToString(mainObject, StringType.JSON));
		mainObject.setMsgTitle("Update title");
		Optional.ofNullable(mainObject.getRelationalReference())
				.ifPresent(relationalReference -> relationalReference.setRefStatue(2));
		return mainObject;
	}

	@Override
	protected final QueryInfo queryInfo() throws Exception {
		return EntityQueryBuilder.newBuilder(TestRelational.class)
				.joins()
				.referenceJoin(JoinType.LEFT, TestRelational.class, RelationalReference.class, "ref").confirm()
				.where()
				.equalTo(RelationalReference.class, "refStatue").matchValue(2).confirm()
				.confirm()
				.pager(2, 5)
				.useCache()
				.build();
	}

	@Override
	protected final void verifyQueryRecord(int index, TestRelational mainObject) {
		this.logger.info("Test_Query_Record", index, BeanUtils.objectToString(mainObject, StringType.JSON));
	}

	private static MagiConfigure generateConfigure(final String dialectName, final String jdbcUrl,
	                                               final String serverAddress, final int serverPort,
												   @Nonnull final String catalog, final boolean pooled,
	                                               final String userName, final String passWord) {
		SchemaConfigBuilder.JdbcConfigBuilder<BrainConfigureBuilder<MagiConfigureBuilder<ParentBuilder>>> configBuilder =
				MagiConfigureBuilder.newBuilder(null, null).brainBuilder()
						.ddlMode(DDLType.SYNCHRONIZE)
						.jdbcConfig("JDBC")
						.dialect(dialectName)
						.serverBuilder(serverAddress, serverPort)
						.confirm()
						.jdbcUrl(jdbcUrl)
						.catalog(catalog)
						.connectionPool(pooled, 2, 10)
						.lowQuery(1000)
						.timeout(2, 2)
						.retry(3, 500L);
		if (StringUtils.notBlank(userName)) {
			configBuilder = configBuilder.basicAuth(userName, passWord);
		}
		return configBuilder.confirm().defaultSchema("JDBC").confirm().build();
	}
}
