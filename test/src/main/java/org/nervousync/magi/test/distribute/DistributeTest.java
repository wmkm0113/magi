package org.nervousync.magi.test.distribute;

import jakarta.annotation.Nonnull;
import org.nervousync.brain.configs.BrainConfigure;
import org.nervousync.brain.configs.builder.BrainConfigureBuilder;
import org.nervousync.brain.configs.builder.SchemaConfigBuilder;
import org.nervousync.brain.configs.server.ServerInfo;
import org.nervousync.brain.enumerations.query.JoinType;
import org.nervousync.brain.query.QueryInfo;
import org.nervousync.commons.Globals;
import org.nervousync.magi.query.builder.QueryBuilder;
import org.nervousync.magi.test.BaseTest;
import org.nervousync.magi.test.distribute.entity.DistributeReference;
import org.nervousync.magi.test.distribute.entity.TestDistribute;
import org.nervousync.utils.ConvertUtils;
import org.nervousync.utils.FileUtils;
import org.nervousync.utils.SecurityUtils;
import org.nervousync.utils.StringUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

public abstract class DistributeTest extends BaseTest<TestDistribute> {

	protected DistributeTest(@Nonnull final String dialectName, final String databaseName,
	                         final List<ServerInfo> serverList, final boolean useSSL,
	                         final String userName, final String passWord) throws Exception {
		super(generateConfigure(dialectName, databaseName, serverList, useSSL, userName, passWord),
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
		testDistribute.setTestDate(new Date());
		testDistribute.setTestDouble(227d);
		testDistribute.setTestFloat(227f);
		testDistribute.setTestInt(227);
		testDistribute.setTestShort(Short.parseShort("227"));
		testDistribute.setTestTime(new Date());
		testDistribute.setTestTimestamp(new Date());

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
		this.logger.info("Test_Retrieve_Record", mainObject.toFormattedJson());
		String md5 = ConvertUtils.toHex(SecurityUtils.SHA256(mainObject.getMsgBytes()));
		this.logger.info("Test_Retrieve_Verify", md5.equals(RESOURCE_VALIDATE));
		this.logger.info("Test_Retrieve_Reference", mainObject.getDistributeReference().toFormattedJson());
	}

	@Override
	protected TestDistribute modifyObject(TestDistribute mainObject) {
		this.logger.info("Test_Update_Record", mainObject.toFormattedJson());
		mainObject.setMsgTitle("Update title");
		Optional.ofNullable(mainObject.getDistributeReference())
				.ifPresent(distributeReference -> distributeReference.setRefStatue(2));
		return mainObject;
	}

	@Override
	protected QueryInfo queryInfo() throws Exception {
		return QueryBuilder.newBuilder(TestDistribute.class)
				.joinTable(JoinType.LEFT, TestDistribute.class, DistributeReference.class, Globals.DEFAULT_VALUE_STRING)
				.equalTo(TestDistribute.class, "msgTitle", "Update title")
				.equalTo(DistributeReference.class, "refStatue", 2)
				.configPager(2, 5)
				.confirm();
	}

	@Override
	protected void verifyQueryRecord(int index, TestDistribute mainObject) {
		this.logger.info("Test_Query_Record", index, mainObject.toFormattedJson());
	}

	private static BrainConfigure generateConfigure(final String dialectName, final String databaseName,
	                                           final List<ServerInfo> serverList, final boolean useSSL,
	                                           final String userName, final String passWord) throws Exception {
		SchemaConfigBuilder.DistributeConfigBuilder configBuilder = BrainConfigureBuilder.newBuilder()
				.distributeConfig("Distribute")
				.dialect(dialectName)
				.databaseName(databaseName)
				.useSsl(useSSL)
				.lowQuery(1000)
				.request(15)
				.timeout(5, 5);
		for (ServerInfo serverInfo : serverList) {
			configBuilder.addServer(serverInfo.getServerName(), serverInfo.getServerAddress(),
					serverInfo.getServerPort(), serverInfo.getServerLevel());
		}
		if (StringUtils.notBlank(userName)) {
			configBuilder.userAuthenticationBuilder().authenticate(userName, passWord)
					.confirmParent(SchemaConfigBuilder.DistributeConfigBuilder.class);
		}
		return configBuilder.confirmParent(BrainConfigureBuilder.class).defaultSchema("Distribute").confirm();
	}
}
