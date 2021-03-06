/*******************************************************************************
 * Copyright (c) 2006, 2014 Tom Schindl and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl - initial API and implementation
 *     Dinko Ivanov - bug 164365
 *     Jeanderson Candido <http://jeandersonbc.github.io> - Bug 414565
 *     Hendrik Still <hendrik.still@gammas.de> - bug 417676
 *******************************************************************************/

package org.eclipse.jface.snippets.viewers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

/**
 * This snippet represents usage of the ComboBoxCell-Editor
 *
 * @author Tom Schindl <tom.schindl@bestsolution.at>
 *
 */
public class Snippet027ComboBoxCellEditors {
	private class MyCellModifier implements ICellModifier<MyModel> {

		private TableViewer<MyModel, List<MyModel>> viewer;

		public MyCellModifier(TableViewer<MyModel, List<MyModel>> viewer) {
			this.viewer = viewer;
		}

		@Override
		public void modify(Object element, String property, Object value) {
			TableItem item = (TableItem) element;
			// We get the index and need to calculate the real value
			((MyModel) item.getData()).counter = ((Integer) value).intValue() * 10;
			viewer.update((MyModel) item.getData(), null);
		}

		@Override
		public boolean canModify(MyModel element, String property) {
			return true;
		}

		@Override
		public Object getValue(MyModel element, String property) {
			return new Integer(element.counter / 10);
		}
	}

	public class MyModel {
		public int counter;

		public MyModel(int counter) {
			this.counter = counter;
		}

		@Override
		public String toString() {
			return "Item " + this.counter;
		}
	}

	public Snippet027ComboBoxCellEditors(Shell shell) {
		final Table table = new Table(shell, SWT.BORDER | SWT.FULL_SELECTION);
		final TableViewer<MyModel, List<MyModel>> v = new TableViewer<MyModel, List<MyModel>>(
				table);
		final MyCellModifier modifier = new MyCellModifier(v);

		TableColumn column = new TableColumn(table, SWT.NONE);
		column.setWidth(200);

		v.setLabelProvider(new LabelProvider<MyModel>());
		v.setContentProvider(ArrayContentProvider.getInstance(MyModel.class));
		v.setCellModifier(modifier);
		v.setColumnProperties(new String[] { "column1" });
		v.setCellEditors(new CellEditor[] { new ComboBoxCellEditor(
				v.getTable(), new String[] { "Zero", "Ten", "Twenty", "Thirty",
						"Fourty", "Fifty", "Sixty", "Seventy", "Eighty",
						"Ninety" }) });

		v.setInput(createModel());
		v.getTable().setLinesVisible(true);
	}

	private List<MyModel> createModel() {
		List<MyModel> elements = new ArrayList<MyModel>();
		for (int i = 0; i < 10; i++) {
			elements.add(new MyModel(i * 10));
		}
		return elements;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setLayout(new FillLayout());
		new Snippet027ComboBoxCellEditors(shell);
		shell.open();

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
	}

}
