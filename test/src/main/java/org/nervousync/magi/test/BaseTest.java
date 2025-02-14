package org.nervousync.magi.test;

import org.apache.logging.log4j.Level;
import org.junit.jupiter.api.*;
import org.nervousync.brain.configs.BrainConfigure;
import org.nervousync.brain.configs.transactional.TransactionalConfig;
import org.nervousync.brain.exceptions.data.InsertException;
import org.nervousync.brain.query.QueryInfo;
import org.nervousync.commons.Globals;
import org.nervousync.magi.annotations.transactional.Transactional;
import org.nervousync.magi.entity.BaseObject;
import org.nervousync.magi.entity.EntityFactory;
import org.nervousync.magi.enumerations.transactional.Isolation;
import org.nervousync.magi.query.PartialCollection;
import org.nervousync.utils.*;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public abstract class BaseTest<T extends BaseObject> {

	private final Class<T> mainClass;
	private final Class<?>[] entityClassList;
	protected static String IDENTIFY_KEY = Globals.DEFAULT_VALUE_STRING;

	protected final LoggerUtils.Logger logger = LoggerUtils.getLogger(this.getClass());
	protected static final String RESOURCE_VALIDATE;

	static {
		LoggerUtils.initLoggerConfigure(Level.INFO, LoggerUtils.newLogger("org.nervousync", Level.DEBUG));
		String resValidate;
		try {
			byte[] fileBytes = FileUtils.readFileBytes("classpath:org/nervousync/magi/test/resource.jpg");
			resValidate = ConvertUtils.toHex(SecurityUtils.SHA256(fileBytes));
		} catch (IOException e) {
			resValidate = Globals.DEFAULT_VALUE_STRING;
		}
		RESOURCE_VALIDATE = resValidate;
	}

	protected BaseTest(final BrainConfigure configure, final Class<T> mainClass, final Class<?>... entityClasses)
			throws Exception {
		this.mainClass = mainClass;
		List<Class<?>> classList = new ArrayList<>();
		classList.add(mainClass);
		classList.addAll(Arrays.asList(entityClasses));
		this.entityClassList = classList.toArray(new Class[0]);
		EntityFactory.initialize(configure);
	}

	private TransactionalConfig txConfig(final String methodName) {
		if (StringUtils.isEmpty(methodName)) {
			return null;
		}
		Method method = ReflectionUtils.findMethod(this.getClass(), methodName);
		Transactional transactional = method.getAnnotation(Transactional.class);
		if (transactional == null) {
			return null;
		}
		return TransactionalConfig.newInstance(transactional.timeout(), transactional.isolation().value(),
				transactional.rollbackFor());
	}

	@BeforeEach
	public void testBefore(final TestInfo testInfo) throws Exception {
		String methodName = testInfo.getTestMethod().orElseThrow().getName();
		this.logger.info("Test_Begin", testInfo.getTestClass().orElseThrow().getName(), methodName);
		TransactionalConfig txConfig = this.txConfig(methodName);
		if (txConfig != null) {
			this.logger.info("Test_Transactional_Config", txConfig.getTimeout(), txConfig.getIsolation());
		}
		EntityFactory.getInstance().beginTransactional(txConfig);
	}

	@AfterEach
	public void testAfter(final TestInfo testInfo) throws Exception {
		EntityFactory.getInstance().endTransactional();
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
		this.identifyKey(recordObject);
		EntityFactory.getInstance().commit();
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
			entityFactory.commit();
		}
	}

	@Test
	@Order(40)
	public void queryRecord() throws Exception {
		EntityFactory entityFactory = EntityFactory.getInstance();
		PartialCollection<T> partialCollection = entityFactory.query(this.mainClass, this.queryInfo());
		this.logger.info("Test_Query_Total_Count", partialCollection.getTotalCount());
		int index = 0;
		for (T mainObject : partialCollection.asList()) {
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
		EntityFactory entityFactory = EntityFactory.getInstance();
		try {
			for (T record : this.generateRecords()) {
				record.save();
			}
			if (rollback) {
				entityFactory.rollback(new InsertException(0L));
			} else {
				entityFactory.commit();
			}
		} catch (Exception e) {
			entityFactory.rollback(e);
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
