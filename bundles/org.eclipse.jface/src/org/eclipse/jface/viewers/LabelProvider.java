/*******************************************************************************
 * Copyright (c) 2000, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Hendrik Still <hendrik.still@gammas.de> - bug 412273
 *******************************************************************************/
package org.eclipse.jface.viewers;

import org.eclipse.swt.graphics.Image;

/**
 * A label provider implementation which, by default, uses an element's
 * <code>toString</code> value for its text and <code>null</code> for its
 * image.
 * <p>
 * This class may be used as is, or subclassed to provide richer labels.
 * Subclasses may override any of the following methods:
 * <ul>
 * <li><code>isLabelProperty</code></li>
 * <li><code>getImage</code></li>
 * <li><code>getText</code></li>
 * <li><code>dispose</code></li>
 * </ul>
 * </p>
 * @param <E> Type of an element of the model
 */
public class LabelProvider<E> extends BaseLabelProvider<E> implements ILabelProvider<E> {

	/**
	 * Creates a new label provider.
	 */
	public LabelProvider() {
	}


	/**
	 * The <code>LabelProvider</code> implementation of this
	 * <code>ILabelProvider</code> method returns <code>null</code>.
	 * Subclasses may override.
	 */
	@Override
	public Image getImage(E element) {
		return null;
	}

	/**
	 * The <code>LabelProvider</code> implementation of this
	 * <code>ILabelProvider</code> method returns the element's
	 * <code>toString</code> string. Subclasses may override.
	 */
	@Override
	public String getText(E element) {
		return element == null ? "" : element.toString();//$NON-NLS-1$
	}
}
