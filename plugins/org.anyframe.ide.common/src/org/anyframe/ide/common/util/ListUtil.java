/*
 * Copyright 2008-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.anyframe.ide.common.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * This is ListUtil class.
 * 
 * @author Juyong Lim
 */
public class ListUtil {

	/**
	 * check this List is null(or empty)
	 * 
	 * @param list
	 * @return
	 */
	public static <E> boolean isEmptyOrNull(Collection<E> list) {
		if (list == null || list.isEmpty()) {
			return true;
		}
		return false;
	}

	/**
	 * @param <E>
	 * @param list
	 */
	public static <E> void makeUnique(List<E> list) {
		if (isEmptyOrNull(list)) {
			return;
		}

		Set<E> set = new HashSet<E>();
		Iterator<E> itr = list.iterator();
		while (itr.hasNext()) {
			E obj = itr.next();

			if (set.contains(obj)) {
				itr.remove();
			} else {
				set.add(obj);
			}
		}
	}

	/**
	 * use for big ArrayList (size > 500)
	 * 
	 * @param <E>
	 * @param list
	 * @return unique list
	 */
	public static <E> List<E> makeUniqueArrayList(List<E> list) {
		if (isEmptyOrNull(list)) {
			return list;
		}

		return new ArrayList<E>(new LinkedHashSet<E>(list));
	}
}
