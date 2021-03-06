/*******************************************************************************
 * Copyright (c) 2005, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     John Cortell, Freescale - bug 289409
 *     Hendrik Still <hendrik.still@gammas.de> - bug 413973
 *******************************************************************************/
package org.eclipse.jface.viewers;

/**
 * The ILazyTreeContentProvider is the content provider for tree viewers created
 * using the SWT.VIRTUAL flag that only wish to return their contents as they
 * are queried.
 * @param <E> Type of an single element of the model
 * @param <I> Type of the input
 *
 * @since 3.2
 */
public interface ILazyTreeContentProvider<E,I> extends IContentProvider<I> {
	/**
	 * Called when a previously-blank item becomes visible in the TreeViewer. If
	 * the content provider knows the child element for the given parent at this
	 * index, it should respond by calling
	 * {@link TreeViewer#replace(Object, int, Object)}. The content provider
	 * should also update the child count for any replaced element by calling
	 * {@link TreeViewer#setChildCount(Object, int)}. If the given current child
	 * count is already correct, setChildCount does not have to be called since
	 * a call to replace will not change the child count. If the content
	 * provider doesn't know the child count at this point, and can more
	 * efficiently determine if the element has <i>any</i> children, then it can
	 * instead call {@link TreeViewer#setHasChildren(Object, boolean)}.
	 *
	 * <p>
	 * <strong>NOTE</strong> #updateElement(int index) can be used to determine
	 * selection values. If TableViewer#replace(Object, int) is not called
	 * before returning from this method, selections may have missing or stale
	 * elements. In this situation it is suggested that the selection is asked
	 * for again after replace() has been called.
	 *
	 * @param parent
	 *            The parent of the element, or the viewer's input if the
	 *            element to update is a root element
	 * @param index
	 *            The index of the element to update in the tree
	 */
	public void updateElement(E parent, int index);

	/**
	 * Called when the TreeViewer needs an up-to-date child count for the given
	 * element, for example from {@link TreeViewer#refresh()} and
	 * {@link TreeViewer#setInput(Object)}. If the content provider knows the
	 * given element, it should respond by calling
	 * {@link TreeViewer#setChildCount(Object, int)}. If the given current
	 * child count is already correct, no action has to be taken by this content
	 * provider.
	 *
	 * @param element
	 *            The element for which an up-to-date child count is needed, or
	 *            the viewer's input if the number of root elements is requested
	 * @param currentChildCount
	 * 			  The current child count for the element that needs updating
	 */
	public void updateChildCount(E element, int currentChildCount);

    /**
     * Returns the parent for the given element, or <code>null</code>
     * indicating that the parent can't be computed.
     * In this case the tree-structured viewer can't expand
     * a given node correctly if requested.
     *
     * @param element the element
     * @return the parent element, or <code>null</code> if it
     *   has none or if the parent cannot be computed
     */
	public E getParent(E element);
}
