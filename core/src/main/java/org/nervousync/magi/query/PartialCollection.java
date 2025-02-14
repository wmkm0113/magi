/*
 * Copyright © 2003 Nervousync® Studio, Inc. All rights reserved.
 * This software is the confidential and proprietary information of
 * Nervousync Studio, Inc. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with Nervousync Studio.
 */
package org.nervousync.magi.query;

import org.nervousync.commons.Globals;
import org.nervousync.utils.StringUtils;

import java.io.Serializable;
import java.util.*;

/**
 * Partial collection
 *
 * @author Steven Wee	<a href="mailto:wmkm0113@Hotmail.com">wmkm0113@Hotmail.com</a>
 * @version $Revision : 1.0 $ $Date: Jan 13, 2010 4:07:14 PM $
 */
public final class PartialCollection<T> implements Serializable {

	/**
	 * Serial Version UID
	 */
	private static final long serialVersionUID = 2086690645677391624L;

	/**
	 * The constant TOTAL_COUNT_KEY.
	 */
	private static final String TOTAL_COUNT_KEY = "totalCount";
	/**
	 * The constant RESULT_LIST_KEY.
	 */
	private static final String RESULT_LIST_KEY = "objectList";

	/**
	 * Collection of entities (part of some another collection)
	 */
	private final List<T> resultList;

	/**
	 * Total number of elements in collection this collection is part of
	 */
	private final long totalCount;

	/**
	 * Creates new instance of PartialCollection with specified collection and total
	 *
	 * @param resultList Result list
	 * @param totalCount Total size of collection, which part is contained in this instance
	 */
	public PartialCollection(final List<T> resultList, final long totalCount) {
		this.resultList = resultList;
		this.totalCount = totalCount;
	}

	/**
	 * As list.
	 *
	 * @return the list
	 */
	public List<T> asList() {
		return this.resultList;
	}

	/**
	 * Gets the size of part of initial collection that is contained here
	 *
	 * @return number of elements in partial collection
	 */
	public int size() {
		return this.resultList.size();
	}

	/**
	 * Figures out is partial collection empty
	 *
	 * @return <code>true</code> if this collection is empty
	 */
	public boolean isEmpty() {
		return this.resultList.isEmpty();
	}

	/**
	 * Gets total number of elements in initial collection
	 *
	 * @return total number of elements
	 */
	public long getTotalCount() {
		return this.totalCount;
	}

	/**
	 * Parse partial collection.
	 *
	 * @param cacheData the cache data
	 * @param clazz     <span class="en-US">Query result class</span>
	 *                  <span class="zh-CN">查询结果类</span>
	 * @param <T>       <span class="en-US">Query result generic class</span>
	 *                  <span class="zh-CN">查询结果泛型类</span>
	 * @return the partial collection
	 */
	public static <T> PartialCollection<T> parse(final String cacheData, final Class<T> clazz) {
		if (StringUtils.isEmpty(cacheData)) {
			return null;
		}

		Map<String, Object> convertMap = StringUtils.dataToMap(cacheData, StringUtils.StringType.JSON);
		if (convertMap.isEmpty()) {
			return null;
		}
		return new PartialCollection<>(
				StringUtils.stringToList((String) convertMap.get(RESULT_LIST_KEY), Globals.DEFAULT_ENCODING, clazz),
				Long.parseLong(
						(String) convertMap.getOrDefault(TOTAL_COUNT_KEY, Long.toHexString(Globals.DEFAULT_VALUE_LONG)),
						16)
		);
	}

	/**
	 * Cache data string.
	 *
	 * @return the string
	 */
	public String cacheData() {
		Map<String, Object> convertMap = new HashMap<>();
		convertMap.put(TOTAL_COUNT_KEY, Long.toHexString(this.totalCount));
		convertMap.put(RESULT_LIST_KEY, StringUtils.objectToString(this.resultList, StringUtils.StringType.JSON, Boolean.FALSE));

		return StringUtils.objectToString(convertMap, StringUtils.StringType.JSON, Boolean.TRUE);
	}
}
