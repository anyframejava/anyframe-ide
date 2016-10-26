/*
 * Copyright 2008-2012 the original author or authors.
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
package org.anyframe.plugin.common.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * This LoggingAspect class is an Aspect class to provide logging functionality
 * on this project.
 * 
 * @author Sooyeon Park
 */
@Aspect
@Service
public class LoggingAspect {

	@Before("execution(* org.anyframe.plugin..*Impl.*(..)) "
			// Add new configuration here
			// xplatform-configuration-START
			+ "|| execution(* org.anyframe.xp.query..*XPServiceImpl.*(..)) "
			// xplatform-configuration-END
	)
	public void beforeLogging(JoinPoint thisJoinPoint) {
		Class<? extends Object> clazz = thisJoinPoint.getTarget().getClass();

		Logger logger = LoggerFactory.getLogger(clazz);

		if (logger.isDebugEnabled()) {
			String methodName = thisJoinPoint.getSignature().getName();
			Object[] arguments = thisJoinPoint.getArgs();

			StringBuilder argBuf = new StringBuilder();
			StringBuilder argValueBuf = new StringBuilder();
			int i = 0;
			for (Object argument : arguments) {
				if (argument != null) {
					String argClassName = argument.getClass().getSimpleName();
					if (i > 0) {
						argBuf.append(", ");
					}
					argBuf.append(argClassName + " arg" + ++i);
					argValueBuf.append(".arg" + i + " : " + argument.toString()
							+ "\n");
				} else {
					if (i > 0) {
						argBuf.append(", ");
					}
					argBuf.append("Unknown type arg" + ++i);
					argValueBuf.append(".arg" + i + " : null\n");
				}
			}

			if (i == 0) {
				argValueBuf.append("No arguments\n");
			}

			logger
					.debug(
							"before executing {} ({}) method \n-------------------------------------------------------------------------------\n {} -------------------------------------------------------------------------------",
							new Object[] { methodName, argBuf.toString(),
									argValueBuf.toString() });
		}
	}
}