package org.nervousync.magi.test.distribute;

import jakarta.annotation.Nonnull;
import org.nervousync.brain.configs.builder.BrainConfigureBuilder;
import org.nervousync.brain.configs.builder.SchemaConfigBuilder;
import org.nervousync.brain.configs.server.ServerInfo;
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
import org.nervousync.magi.test.distribute.entity.DistributeReference;
import org.nervousync.magi.test.distribute.entity.TestDistribute;
import org.nervousync.utils.core.BeanUtils;
import org.nervousync.utils.core.FileUtils;
import org.nervousync.utils.core.StringUtils;
import org.nervousync.utils.security.SecurityUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

/**
 * <h2 class="en-US">Abstract class of distribute database test instance</h2>
 * <h2 class="zh-CN">分布式数据库测试抽象类</h2>
 *
 * @author Steven Wee	<a href="mailto:wmkm0113@gmail.com">wmkm0113@gmail.com</a>
 * @version $Revision: 1.0.0 $ $Date: Nov 18, 2022 15:22:27 $
 */
public abstract class DistributeTest extends BaseTest<TestDistribute> {

	protected DistributeTest(@Nonnull final String dialectName, final String databaseName,
	                         final List<ServerInfo> serverList, final boolean ssl,
	                         final String userName, final String passWord) throws Exception {
		super(generateConfigure(dialectName, databaseName, serverList, ssl, userName, passWord),
				TestDistribute.class, DistributeReference.class);
	}

	@Override
	protected TestDistribute generateRecord() throws IOException {
		return generate(Globals.INITIALIZE_INT_VALUE);
	}

	private TestDistribute generate(int index) throws IOException {
		TestDistribute testDistribute = new TestDistribute();
		testDistribute.setMsgContent("Message Content " + (index % 3));
		testDistribute.setMsgBytes(FileUtils.readFileBytes(FileUtils.getFile("classpath:org/nervousync/magi/test/resource.jpg")));
		testDistribute.setMsgTitle("Message title " + (index % 3));
		testDistribute.setTestBigDecimal(new BigDecimal(Math.PI));
		testDistribute.setTestBoolean(true);
		testDistribute.setTestByte((byte) 227);
		testDistribute.setTestDate(LocalDate.now());
		testDistribute.setTestDouble(227d);
		testDistribute.setTestFloat(227f);
		testDistribute.setTestInt(227);
		testDistribute.setTestShort(Short.parseShort("227"));
		testDistribute.setTestTime(LocalTime.now());
		testDistribute.setTestTimestamp(LocalDateTime.now());

		DistributeReference distributeReference = new DistributeReference();
		distributeReference.setRefStatue(1);
		testDistribute.setDistributeReference(distributeReference);

		return testDistribute;
	}

	@Override
	protected List<TestDistribute> generateRecords() throws IOException {
		List<TestDistribute> recordList = new ArrayList<>();
		for (int i = 0; i < 20; i++) {
			recordList.add(generate(i));
		}
		return recordList;
	}

	@Override
	protected void identifyKey(TestDistribute recordObject) {
		IDENTIFY_KEY = recordObject.getIdentifyCode();
	}

	@Override
	protected void verifyRetrieve(TestDistribute mainObject) {
		this.logger.info("Test_Retrieve_Record", BeanUtils.objectToString(mainObject, StringType.JSON));
		String sha256 = SecurityUtils.SHA256(mainObject.getMsgBytes(), EncodeType.HEX);
		this.logger.info("Test_Retrieve_Verify", RESOURCE_VALIDATE.equalsIgnoreCase(sha256));
		this.logger.info("Test_Retrieve_Reference", BeanUtils.objectToString(mainObject.getDistributeReference(), StringType.JSON));
	}

	@Override
	protected TestDistribute modifyObject(TestDistribute mainObject) {
		this.logger.info("Test_Update_Record", BeanUtils.objectToString(mainObject, StringType.JSON));
		mainObject.setMsgTitle("Update title");
		Optional.ofNullable(mainObject.getDistributeReference())
				.ifPresent(distributeReference -> distributeReference.setRefStatue(2));
		return mainObject;
	}

	@Override
	protected QueryInfo queryInfo() throws Exception {
		return EntityQueryBuilder.newBuilder(TestDistribute.class)
				.joins()
				.referenceJoin(JoinType.LEFT, TestDistribute.class, DistributeReference.class, "ref").confirm()
				.where()
				.equalTo(TestDistribute.class, "msgTitle").matchValue("Update title").confirm()
				.equalTo(DistributeReference.class, "refStatue").matchValue(2).confirm()
				.confirm()
				.pager(1, 5)
				.build();
	}

	@Override
	protected void verifyQueryRecord(int index, TestDistribute mainObject) {
		this.logger.info("Test_Query_Record", index, BeanUtils.objectToString(mainObject, StringType.JSON));
	}

	private static MagiConfigure generateConfigure(final String dialectName, final String databaseName,
	                                               final List<ServerInfo> serverList, final boolean ssl,
	                                               final String userName, final String passWord) {
		SchemaConfigBuilder.DistributeConfigBuilder<BrainConfigureBuilder<MagiConfigureBuilder<ParentBuilder>>> configBuilder =
				MagiConfigureBuilder.newBuilder(null, new MagiConfigure()).brainBuilder()
						.ddlMode(DDLType.SYNCHRONIZE)
						.distributeConfig("Distribute")
						.dialect(dialectName)
						.databaseName(databaseName)
						.useSsl(ssl)
						.lowQuery(1000)
						.request(15)
						.timeout(5, 5);
		for (ServerInfo serverInfo : serverList) {
			configBuilder = configBuilder.serverBuilder(serverInfo.getServerAddress(), serverInfo.getServerPort())
					.name(serverInfo.getServerName())
					.level(serverInfo.getServerLevel())
					.confirm();
		}
		if (StringUtils.notBlank(userName)) {
			configBuilder = configBuilder.basicAuth(userName, passWord);
		}

		return configBuilder.confirm().defaultSchema("Distribute").confirm().build();
	}
}
