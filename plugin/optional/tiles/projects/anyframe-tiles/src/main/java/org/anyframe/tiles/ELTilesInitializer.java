/* Copyright 2002-2012 the original author or authors.
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
package org.anyframe.tiles;

import org.apache.tiles.TilesApplicationContext;
import org.apache.tiles.factory.AbstractTilesContainerFactory;
import org.apache.tiles.startup.AbstractTilesInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;


/**
 * It is a custom Tiles Initializer to support Tiles EL with Spring 3.0. 
 * 
 * @author Changje Kim
 */
public class ELTilesInitializer extends AbstractTilesInitializer {
    
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    
    private String[] definitions; 
    
    public void setDefinitions(String[] definitions) {
        this.definitions = definitions;
        if (definitions != null) {
            String defs = StringUtils.arrayToCommaDelimitedString(definitions);
            logger.info("TilesConfigurer: adding definitions [{}]", defs);
        }
     }
 
    protected AbstractTilesContainerFactory createContainerFactory(TilesApplicationContext context) { 
            return  new ELTilesContainerFactory(definitions);
    }
            
}
