/*******************************************************************************
 * Copyright (c) 2006, 2014 Tom Schindl and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl - initial API and implementation
 *     Jeanderson Candido <http://jeandersonbc.github.io> - Bug 414565
 *     Hendrik Still <hendrik.still@gammas.de> - bug 417676
 *******************************************************************************/

package org.eclipse.jface.snippets.viewers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewerEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerEditor;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

/**
 * Edit cell values in a table
 *
 * @author Tom Schindl <tom.schindl@bestsolution.at>
 *
 */
public class Snippet025TabEditing {

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

	public Snippet025TabEditing(Shell shell) {
		final TableViewer<MyModel, List<MyModel>> viewer = new TableViewer<MyModel, List<MyModel>>(
				shell, SWT.BORDER | SWT.FULL_SELECTION);

		createColumnFor(viewer, "Column 1", 100);
		createColumnFor(viewer, "Column 2", 200);

		viewer.setLabelProvider(new LabelProvider<MyModel>());
		viewer.setContentProvider(ArrayContentProvider
				.getInstance(MyModel.class));
		viewer.setCellModifier(new ICellModifier<MyModel>() {

			@Override
			public void modify(Object element, String property, Object value) {
				TableItem item = (TableItem) element;
				((MyModel) item.getData()).counter = Integer.parseInt(value
						.toString());
				viewer.update((MyModel) item.getData(), null);
			}

			@Override
			public boolean canModify(MyModel element, String property) {
				return element.counter % 2 == 0;
			}

			@Override
			public Object getValue(MyModel element, String property) {
				return element.counter + "";
			}

		});

		viewer.setColumnProperties(new String[] { "column1", "column2" });
		viewer.setCellEditors(new CellEditor[] {
				new TextCellEditor(viewer.getTable()),
				new TextCellEditor(viewer.getTable()) });

		TableViewerEditor
				.create(viewer,
						new ColumnViewerEditorActivationStrategy<MyModel, List<MyModel>>(
								viewer),
						ColumnViewerEditor.TABBING_HORIZONTAL
								| ColumnViewerEditor.TABBING_MOVE_TO_ROW_NEIGHBOR
								| ColumnViewerEditor.TABBING_VERTICAL);

		viewer.setInput(createModel());
		viewer.getTable().setLinesVisible(true);
	}

	private void createColumnFor(TableViewer<MyModel, List<MyModel>> viewer,
			String label, int width) {
		TableColumn tc = new TableColumn(viewer.getTable(), SWT.NONE);
		tc.setWidth(width);
		tc.setText(label);
	}

	private List<MyModel> createModel() {
		List<MyModel> elements = new ArrayList<MyModel>();
		for (int i = 0; i < 10; i++) {
			elements.add(new MyModel(i));

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
		new Snippet025TabEditing(shell);
		shell.open();

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();

	}

}
