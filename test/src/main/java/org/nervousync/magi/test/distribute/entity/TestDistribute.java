/*
 * Copyright © 2003 Nervousync® Studio, Inc. All rights reserved.
 * This software is the confidential and proprietary information of
 * Nervousync Studio, Inc. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with Nervousync Studio.
 */

package org.nervousync.magi.test.distribute.entity;

import jakarta.persistence.*;
import org.nervousync.brain.enumerations.ddl.GenerationType;
import org.nervousync.commons.Globals;
import org.nervousync.magi.annotations.table.GeneratedData;
import org.nervousync.magi.entity.BaseObject;
import org.nervousync.utils.IDUtils;

import java.math.BigDecimal;
import java.util.Date;

/**
 * The type Test entity.
 *
 * @author Steven Wee	<a href="mailto:wmkm0113@Hotmail.com">wmkm0113@Hotmail.com</a>
 * @version $Revision : 1.0 $ $Date: 11/30/2020 1:05 PM $
 */
@Table(name = "Test_Distribute", indexes = {
		@Index(name = "title_index", columnList = "msgTitle")
})
public final class TestDistribute extends BaseObject {

	/**
	 * The constant serialVersionUID.
	 */
	private static final long serialVersionUID = -1136316002800055959L;

	/**
	 * Identify code.
	 */
	@Id
	@Column(nullable = false)
	@GeneratedData(type = GenerationType.GENERATE, generator = IDUtils.UUIDv4)
	private String identifyCode;
	/**
	 * The Msg title.
	 */
	@Column(nullable = false, length = 200)
	private String msgTitle;
	/**
	 * The Msg content.
	 */
	@Lob
//	@Basic(fetch = FetchType.LAZY)
	@Column
	private byte[] msgBytes;
	/**
	 * The Msg content.
	 */
	@Lob
	@Basic(fetch = FetchType.LAZY)
	@Column
	private String msgContent;
	/**
	 * The Test int.
	 */
	@Column
	private int testInt = Globals.DEFAULT_VALUE_INT;
	/**
	 * The Test short.
	 */
	@Column
	private short testShort = Globals.DEFAULT_VALUE_SHORT;
	/**
	 * The Test double.
	 */
	@Column(precision = 53)
	private double testDouble = Globals.DEFAULT_VALUE_DOUBLE;
	/**
	 * The Test float.
	 */
	@Column(precision = 53)
	private float testFloat = Globals.DEFAULT_VALUE_FLOAT;
	/**
	 * The Test byte.
	 */
	@Column
	private byte testByte;
	/**
	 * The Test boolean.
	 */
	@Column
	private boolean testBoolean = Boolean.FALSE;
	/**
	 * The Test date.
	 */
	@Column
	@Temporal(TemporalType.DATE)
	private Date testDate;
	/**
	 * The Test time.
	 */
	@Column
	@Temporal(TemporalType.TIME)
	private Date testTime;
	/**
	 * The Test timestamp.
	 */
	@Column
	@Temporal(TemporalType.TIMESTAMP)
	private Date testTimestamp;
	/**
	 * The Test big decimal.
	 */
	@Column(precision = 18, scale = 18)
	private BigDecimal testBigDecimal;
	/**
	 * Distribute reference.
	 */
	@OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JoinColumn(columnDefinition = "identifyCode", referencedColumnName = "identifyCode")
	private DistributeReference distributeReference;

	/**
	 * Gets serial version uid.
	 *
	 * @return the serial version uid
	 */
	public static long getSerialVersionUID() {
		return serialVersionUID;
	}

	/**
	 * Gets identify code.
	 *
	 * @return identify code
	 */
	public String getIdentifyCode() {
		return identifyCode;
	}

	/**
	 * Sets identify code.
	 *
	 * @param identifyCode identify code
	 */
	public void setIdentifyCode(String identifyCode) {
		this.identifyCode = identifyCode;
	}

	/**
	 * Gets msg title.
	 *
	 * @return the msg title
	 */
	public String getMsgTitle() {
		return msgTitle;
	}

	/**
	 * Sets msg title.
	 *
	 * @param msgTitle the msg title
	 */
	public void setMsgTitle(String msgTitle) {
		this.msgTitle = msgTitle;
	}

	/**
	 * Get msg bytes byte [ ].
	 *
	 * @return the byte [ ]
	 */
	public byte[] getMsgBytes() {
		return msgBytes;
	}

	/**
	 * Sets msg bytes.
	 *
	 * @param msgBytes the msg bytes
	 */
	public void setMsgBytes(byte[] msgBytes) {
		this.msgBytes = msgBytes;
	}

	/**
	 * Get msg content char [ ].
	 *
	 * @return the char [ ]
	 */
	public String getMsgContent() {
		return msgContent;
	}

	/**
	 * Sets msg content.
	 *
	 * @param msgContent the msg content
	 */
	public void setMsgContent(String msgContent) {
		this.msgContent = msgContent;
	}

	/**
	 * Gets test int.
	 *
	 * @return the test int
	 */
	public int getTestInt() {
		return testInt;
	}

	/**
	 * Sets test int.
	 *
	 * @param testInt the test int
	 */
	public void setTestInt(int testInt) {
		this.testInt = testInt;
	}

	/**
	 * Gets test short.
	 *
	 * @return the test short
	 */
	public short getTestShort() {
		return testShort;
	}

	/**
	 * Sets test short.
	 *
	 * @param testShort the test short
	 */
	public void setTestShort(short testShort) {
		this.testShort = testShort;
	}

	/**
	 * Gets test double.
	 *
	 * @return the test double
	 */
	public double getTestDouble() {
		return testDouble;
	}

	/**
	 * Sets test double.
	 *
	 * @param testDouble the test double
	 */
	public void setTestDouble(double testDouble) {
		this.testDouble = testDouble;
	}

	/**
	 * Gets test float.
	 *
	 * @return the test float
	 */
	public float getTestFloat() {
		return testFloat;
	}

	/**
	 * Sets test float.
	 *
	 * @param testFloat the test float
	 */
	public void setTestFloat(float testFloat) {
		this.testFloat = testFloat;
	}

	/**
	 * Gets test byte.
	 *
	 * @return the test byte
	 */
	public byte getTestByte() {
		return testByte;
	}

	/**
	 * Sets test byte.
	 *
	 * @param testByte the test byte
	 */
	public void setTestByte(byte testByte) {
		this.testByte = testByte;
	}

	/**
	 * Is test boolean.
	 *
	 * @return the boolean
	 */
	public boolean isTestBoolean() {
		return testBoolean;
	}

	/**
	 * Sets test boolean.
	 *
	 * @param testBoolean the test boolean
	 */
	public void setTestBoolean(boolean testBoolean) {
		this.testBoolean = testBoolean;
	}

	/**
	 * Gets test date.
	 *
	 * @return the test date
	 */
	public Date getTestDate() {
		return testDate;
	}

	/**
	 * Sets test date.
	 *
	 * @param testDate the test date
	 */
	public void setTestDate(Date testDate) {
		this.testDate = testDate;
	}

	/**
	 * Gets test time.
	 *
	 * @return the test time
	 */
	public Date getTestTime() {
		return testTime;
	}

	/**
	 * Sets test time.
	 *
	 * @param testTime the test time
	 */
	public void setTestTime(Date testTime) {
		this.testTime = testTime;
	}

	/**
	 * Gets test timestamp.
	 *
	 * @return the test timestamp
	 */
	public Date getTestTimestamp() {
		return testTimestamp;
	}

	/**
	 * Sets test timestamp.
	 *
	 * @param testTimestamp the test timestamp
	 */
	public void setTestTimestamp(Date testTimestamp) {
		this.testTimestamp = testTimestamp;
	}

	/**
	 * Gets test big decimal.
	 *
	 * @return the test big decimal
	 */
	public BigDecimal getTestBigDecimal() {
		return testBigDecimal;
	}

	/**
	 * Sets test big decimal.
	 *
	 * @param testBigDecimal the test big decimal
	 */
	public void setTestBigDecimal(BigDecimal testBigDecimal) {
		this.testBigDecimal = testBigDecimal;
	}

	/**
	 * Gets distribute reference.
	 *
	 * @return distribute reference
	 */
	public DistributeReference getDistributeReference() {
		return distributeReference;
	}

	/**
	 * Sets distribute reference.
	 *
	 * @param distributeReference distribute reference
	 */
	public void setDistributeReference(DistributeReference distributeReference) {
		this.distributeReference = distributeReference;
	}
}
