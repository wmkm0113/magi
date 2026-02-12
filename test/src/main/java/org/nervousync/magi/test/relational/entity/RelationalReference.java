/*
 * Copyright © 2003 Nervousync® Studio, Inc. All rights reserved.
 * This software is the confidential and proprietary information of
 * Nervousync Studio, Inc. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with Nervousync Studio.
 */

package org.nervousync.magi.test.relational.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Table;
import org.nervousync.magi.entity.BaseObject;
import org.nervousync.magi.entity.CompositeId;

/**
 * @author Steven Wee	<a href="mailto:wmkm0113@gmail.com">wmkm0113@gmail.com</a>
 * @version $Revision: 1.0 $ $Date: 3/16/2021 03:12 PM $
 */
@SuppressWarnings("unused")
@Table(name = "Relational_Reference")
public class RelationalReference extends BaseObject {

	private static final long serialVersionUID = 5529322451235666123L;

	/**
	 * Identify code.
	 */
	@EmbeddedId
	private ReferenceCompositeId compositeId;
	/**
	 * The Ref statue.
	 */
	@Column(nullable = false)
	private int refStatue;

	/**
	 * Instantiates a new Distribute reference.
	 */
	public RelationalReference() {
		this.compositeId = new ReferenceCompositeId();
	}

	public ReferenceCompositeId getCompositeId() {
		return compositeId;
	}

	public void setCompositeId(ReferenceCompositeId compositeId) {
		this.compositeId = compositeId;
	}

	/**
	 * Gets ref statue.
	 *
	 * @return the ref statue
	 */
	public int getRefStatue() {
		return refStatue;
	}

	/**
	 * Sets ref statue.
	 *
	 * @param refStatue the ref statue
	 */
	public void setRefStatue(int refStatue) {
		this.refStatue = refStatue;
	}

	public static final class ReferenceCompositeId extends CompositeId {

		@Column(nullable = false)
		private String identifyCode;
		@Column(nullable = false, name = "identifyTime")
		private long currentTime;

		public ReferenceCompositeId() {
		}

		public ReferenceCompositeId(final String identifyCode, final long currentTime) {
			this.identifyCode = identifyCode;
			this.currentTime = currentTime;
		}

		public String getIdentifyCode() {
			return identifyCode;
		}

		public void setIdentifyCode(String identifyCode) {
			this.identifyCode = identifyCode;
		}

		public long getCurrentTime() {
			return currentTime;
		}

		public void setCurrentTime(long currentTime) {
			this.currentTime = currentTime;
		}
	}
}
