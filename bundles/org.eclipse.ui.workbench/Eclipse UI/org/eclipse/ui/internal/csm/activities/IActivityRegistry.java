/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.csm.activities;

import java.util.List;

/**
 * <p>
 * JAVADOC
 * </p>
 * <p>
 * This interface is not intended to be extended or implemented by clients.
 * </p>
 * <p>
 * <em>EXPERIMENTAL</em>
 * </p>
 * 
 * @since 3.0
 */
public interface IActivityRegistry {

	/**
	 * Registers an IActivityRegistryListener instance with this activity registry.
	 *
	 * @param activityRegistryListener the IActivityRegistryListener instance to register.
	 */
	void addActivityRegistryListener(IActivityRegistryListener activityRegistryListener);

	/**
	 * JAVADOC
	 *
	 * @return
	 */
	List getActivityDefinitions();

	/**
	 * Unregisters an IActivityRegistryListener instance with this activity registry.
	 *
	 * @param activityRegistryListener the IActivityRegistryListener instance to unregister.
	 */
	void removeActivityRegistryListener(IActivityRegistryListener activityRegistryListener);
}
