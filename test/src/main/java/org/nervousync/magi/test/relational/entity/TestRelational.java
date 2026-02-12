/*
 * Copyright © 2003 Nervousync® Studio, Inc. All rights reserved.
 * This software is the confidential and proprietary information of
 * Nervousync Studio, Inc. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with Nervousync Studio.
 */

package org.nervousync.magi.test.relational.entity;

import jakarta.persistence.*;
import org.nervousync.brain.enumerations.ddl.GenerationType;
import org.nervousync.commons.Globals;
import org.nervousync.magi.annotations.table.GeneratedData;
import org.nervousync.magi.annotations.table.Options;
import org.nervousync.magi.entity.BaseObject;
import org.nervousync.utils.id.IDUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * The type Test entity.
 *
 * @author Steven Wee	<a href="mailto:wmkm0113@gmail.com">wmkm0113@gmail.com</a>
 * @version $Revision: 1.0 $ $Date: 11/30/2020 1:05 PM $
 */
@SuppressWarnings("unused")
@Options(lockOption = LockModeType.PESSIMISTIC_WRITE)
@Table(name = "Test_Relational")
public final class TestRelational extends BaseObject {

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
	@Basic(fetch = FetchType.LAZY)
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
	private short testInt = Globals.DEFAULT_VALUE_INT;
	/**
	 * The Test short.
	 */
	@Column
	private short testShort = Globals.DEFAULT_VALUE_SHORT;
	/**
	 * The Test double.
	 */
	@Column(precision = 50, scale = 3)
	private double testDouble = Globals.DEFAULT_VALUE_DOUBLE;
	/**
	 * The Test float.
	 */
	@Column(precision = 50, scale = 3)
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
//	@Temporal(TemporalType.DATE)
	@GeneratedData(type = GenerationType.CURRENT_DATE)
	private LocalDate testDate;
	/**
	 * The Test time.
	 */
	@Column
//	@Temporal(TemporalType.TIME)
	@GeneratedData(type = GenerationType.CURRENT_TIME)
	private LocalTime testTime;
	/**
	 * The Test timestamp.
	 */
	@Column
//	@Temporal(TemporalType.TIMESTAMP)
	@GeneratedData(type = GenerationType.CURRENT_TIMESTAMP)
	private Instant testTimestamp;
	/**
	 * The Test big decimal.
	 */
	@Column(precision = 31, scale = 14)
	private BigDecimal testBigDecimal;
	/**
	 * The Relational reference.
	 */
	@OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JoinColumn(columnDefinition = "identifyCode", referencedColumnName = "identifyCode")
	private RelationalReference relationalReference;

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
	public short getTestInt() {
		return testInt;
	}

	/**
	 * Sets test int.
	 *
	 * @param testInt the test int
	 */
	public void setTestInt(short testInt) {
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
	public LocalDate getTestDate() {
		return testDate;
	}

	/**
	 * Sets test date.
	 *
	 * @param testDate the test date
	 */
	public void setTestDate(LocalDate testDate) {
		this.testDate = testDate;
	}

	/**
	 * Gets test time.
	 *
	 * @return the test time
	 */
	public LocalTime getTestTime() {
		return testTime;
	}

	/**
	 * Sets test time.
	 *
	 * @param testTime the test time
	 */
	public void setTestTime(LocalTime testTime) {
		this.testTime = testTime;
	}

	/**
	 * Gets test timestamp.
	 *
	 * @return the test timestamp
	 */
	public Instant getTestTimestamp() {
		return testTimestamp;
	}

	/**
	 * Sets test timestamp.
	 *
	 * @param testTimestamp the test timestamp
	 */
	public void setTestTimestamp(Instant testTimestamp) {
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
	 * Gets relational reference.
	 *
	 * @return the relational reference
	 */
	public RelationalReference getRelationalReference() {
		return relationalReference;
	}

	/**
	 * Sets relational reference.
	 *
	 * @param relationalReference the relational reference
	 */
	public void setRelationalReference(RelationalReference relationalReference) {
		this.relationalReference = relationalReference;
	}
}
