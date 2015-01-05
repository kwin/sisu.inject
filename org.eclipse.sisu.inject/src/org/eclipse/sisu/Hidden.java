/*******************************************************************************
 * Copyright (c) 2010, 2015 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stuart McCulloch (Sonatype, Inc.) - initial API and implementation
 *******************************************************************************/
package org.eclipse.sisu;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Hides a bean from being located or watched:<br>
 * <br>
 * 
 * <pre>
 * &#064;Named
 * &#064;Hidden
 * public class InternalComponent
 * {
 *     //
 * }
 * </pre>
 * 
 * @see org.eclipse.sisu.inject.BeanLocator
 */
@Target( value = { ElementType.TYPE } )
@Retention( RetentionPolicy.RUNTIME )
@Documented
public @interface Hidden
{
}
