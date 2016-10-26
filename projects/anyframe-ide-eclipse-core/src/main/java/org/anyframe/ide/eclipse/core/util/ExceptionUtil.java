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
package org.anyframe.ide.eclipse.core.util;

import org.anyframe.ide.eclipse.core.AnyframeIDEPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;


/**
 * This is an ExceptionUtil class.
 * @author Changje Kim
 * @author Sooyeon Park
 */
public class ExceptionUtil {

    protected ExceptionUtil() {
        throw new UnsupportedOperationException(); // prevents calls from subclass
    }

    public static void showException(final String exceptionMsg,
            final int exceptionType, final Throwable exception) {
        IStatus status =
            new Status(exceptionType, AnyframeIDEPlugin.ID, exceptionMsg,
                exception);
        AnyframeIDEPlugin.getDefault().getLog().log(status);
    }
}
