package org.nervousync.magi.test;

import org.junit.jupiter.api.*;
import org.nervousync.brain.annotations.transactional.Transactional;
import org.nervousync.brain.configs.transactional.TransactionalConfig;
import org.nervousync.brain.enumerations.transactional.Isolation;
import org.nervousync.brain.exceptions.data.InsertException;
import org.nervousync.brain.query.PartialCollection;
import org.nervousync.brain.query.QueryInfo;
import org.nervousync.brain.transactional.TransactionalProxy;
import org.nervousync.commons.Globals;
import org.nervousync.enumerations.beans.StringType;
import org.nervousync.enumerations.logger.LogLevel;
import org.nervousync.enumerations.security.EncodeType;
import org.nervousync.magi.config.MagiConfigure;
import org.nervousync.magi.entity.BaseObject;
import org.nervousync.magi.entity.EntityFactory;
import org.nervousync.utils.core.*;
import org.nervousync.utils.logger.LoggerUtils;
import org.nervousync.utils.security.SecurityUtils;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * <h2 class="en-US">Abstract class of database test instance</h2>
 * <h2 class="zh-CN">数据库测试抽象类</h2>
 *
 * @author Steven Wee	<a href="mailto:wmkm0113@gmail.com">wmkm0113@gmail.com</a>
 * @version $Revision: 1.0.0 $ $Date: Nov 18, 2022 15:22:27 $
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public abstract class BaseTest<T extends BaseObject> {

	private final Class<T> mainClass;
	private final Class<?>[] entityClassList;
	private boolean newTransactional = Boolean.TRUE;
	protected static String IDENTIFY_KEY = Globals.DEFAULT_VALUE_STRING;

	protected final LoggerUtils.Logger logger = LoggerUtils.getLogger(this.getClass());
	protected static final String RESOURCE_VALIDATE;

	static {
		LoggerUtils.initLoggerConfigure(LogLevel.INFO, LoggerUtils.newLogger("org.nervousync", LogLevel.DEBUG));
		String resValidate;
		try {
			byte[] fileBytes = FileUtils.readFileBytes("classpath:org/nervousync/magi/test/resource.jpg");
			resValidate = SecurityUtils.SHA256(fileBytes, EncodeType.HEX);
		} catch (IOException e) {
			resValidate = Globals.DEFAULT_VALUE_STRING;
		}
		RESOURCE_VALIDATE = resValidate;
	}

	protected BaseTest(final MagiConfigure configure, final Class<T> mainClass, final Class<?>... entityClasses)
			throws Exception {
		this.mainClass = mainClass;
		List<Class<?>> classList = new ArrayList<>();
		classList.add(mainClass);
		classList.addAll(Arrays.asList(entityClasses));
		this.entityClassList = classList.toArray(new Class[0]);
		EntityFactory.initialize(configure);
		String string = BeanUtils.objectToString(configure, StringType.XML);
		MagiConfigure parsedConfig =
				BeanUtils.stringToObject(string, StringType.XML,
						MagiConfigure.class, "https://nervousync.org/schemas/magi");
		System.out.println(BeanUtils.objectToString(parsedConfig, StringType.JSON));
	}

	@BeforeEach
	public void testBefore(final TestInfo testInfo) {
		String methodName = testInfo.getTestMethod().orElseThrow().getName();
		this.logger.info("Test_Begin", testInfo.getTestClass().orElseThrow().getName(), methodName);
		Method method = ReflectionUtils.findMethod(this.getClass(), methodName);
		this.newTransactional = Optional.ofNullable(method.getAnnotation(Transactional.class))
				.map(transactional ->
						TransactionalProxy.getTransactionalManager().begin(TransactionalConfig.newInstance(transactional)))
				.orElse(Boolean.TRUE);
	}

	@AfterEach
	public void testAfter(final TestInfo testInfo) throws Exception {
		if (TransactionalProxy.getTransactionalManager().inTransactional()) {
			TransactionalProxy.getTransactionalManager().end(this.newTransactional);
			TransactionalProxy.getTransactionalManager().clear();
		}
		String methodName = testInfo.getTestMethod().orElseThrow().getName();
		this.logger.info("Test_End", testInfo.getTestClass().orElseThrow().getName(), methodName);
	}

	@Test
	@Order(5)
	public void createTable() throws Exception {
		EntityFactory.getInstance().registerTables(this.entityClassList);
		this.logger.info("Test_Initialize_Table");
	}

	@Test
	@Order(10)
	@Transactional(timeout = 5, rollbackFor = InsertException.class, isolation = Isolation.ISOLATION_READ_COMMITTED)
	public void insertRecord() throws Exception {
		T recordObject = this.generateRecord();
		recordObject.save();
		TransactionalProxy.getTransactionalManager().commit(this.newTransactional);
		this.identifyKey(recordObject);
	}

	@Test
	@Order(15)
	@Transactional(timeout = 5, rollbackFor = InsertException.class, isolation = Isolation.ISOLATION_READ_COMMITTED)
	public void insertRecords() throws Exception {
		this.processInsertRecords(Boolean.FALSE);
	}

	@Test
	@Order(18)
	@Transactional(timeout = 5, rollbackFor = InsertException.class, isolation = Isolation.ISOLATION_READ_COMMITTED)
	public void rollback() throws Exception {
		this.processInsertRecords(Boolean.TRUE);
	}

	@Test
	@Order(20)
	public void retrieveRecord() throws Exception {
		this.logger.info("Test_Identify_Code", IDENTIFY_KEY);
		T mainObject = EntityFactory.getInstance().retrieveRecord(IDENTIFY_KEY, this.mainClass, Boolean.FALSE);
		if (mainObject == null) {
			this.logger.info("Test_Retrieve_Null");
		} else {
			this.verifyRetrieve(mainObject);
		}
	}

	@Test
	@Order(30)
	@Transactional(timeout = 5, rollbackFor = InsertException.class, isolation = Isolation.ISOLATION_READ_COMMITTED)
	public void updateRecord() throws Exception {
		EntityFactory entityFactory = EntityFactory.getInstance();
		this.logger.info("Test_Identify_Code", IDENTIFY_KEY);
		T mainObject = entityFactory.retrieveRecord(IDENTIFY_KEY, this.mainClass, Boolean.TRUE);
		if (mainObject == null) {
			this.logger.info("Test_Retrieve_Null");
		} else {
			this.modifyObject(mainObject).update();
			TransactionalProxy.getTransactionalManager().commit(this.newTransactional);
		}
	}

	@Test
	@Order(40)
	public void queryRecord() throws Exception {
		EntityFactory entityFactory = EntityFactory.getInstance();
		PartialCollection partialCollection = entityFactory.query(this.queryInfo());
		this.logger.info("Test_Query_Total_Count", partialCollection.getTotalCount());
		int index = 0;
		for (Map<String, Object> dataMap : partialCollection.asList()) {
			T mainObject = entityFactory.dataMapToObject(this.mainClass, dataMap);
			this.verifyQueryRecord(index, mainObject);
			index++;
		}
	}

	@Test
	@Order(50)
	public void deleteRecord() throws Exception {
		this.logger.info("Test_Identify_Code", IDENTIFY_KEY);
		T mainObject = EntityFactory.getInstance().retrieveRecord(IDENTIFY_KEY, this.mainClass, Boolean.TRUE);
		if (mainObject == null) {
			this.logger.info("Test_Retrieve_Null");
			return;
		}
		this.verifyRetrieve(mainObject);
		mainObject.delete();
		TransactionalProxy.getTransactionalManager().commit(this.newTransactional);
	}

	@Test
	@Order(60)
	public void truncateTable() throws Exception {
		EntityFactory.getInstance().truncateTables(this.entityClassList);
	}

	@Test
	@Order(70)
	public void removeTable() throws Exception {
		EntityFactory.getInstance().dropTables(this.entityClassList);
	}

	private void processInsertRecords(boolean rollback) throws Exception {
		try {
			for (T record : this.generateRecords()) {
				record.save();
			}
			if (rollback) {
				TransactionalProxy.getTransactionalManager().rollback(new InsertException(0L));
			} else {
				TransactionalProxy.getTransactionalManager().commit(this.newTransactional);
			}
		} catch (Exception e) {
			TransactionalProxy.getTransactionalManager().rollback(e);
		}
	}

	protected abstract T generateRecord() throws IOException;

	protected abstract List<T> generateRecords() throws IOException;

	protected abstract void identifyKey(T recordObject);

	protected abstract void verifyRetrieve(T mainObject);

	protected abstract T modifyObject(T mainObject);

	protected abstract QueryInfo queryInfo() throws Exception;

	protected abstract void verifyQueryRecord(int index, T mainObject);
}
