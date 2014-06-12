/*******************************************************************************
 * Copyright (c) 2006, 2014 Tom Schindl and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl - initial API and implementation
 *     Hendrik Still <hendrik.still@gammas.de> - bug 417676
 *     Lars Vogel <Lars.Vogel@gmail.com> - Bug 414565
 *******************************************************************************/

package org.eclipse.jface.snippets.viewers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableColumn;

/**
 * Example usage of none mandatory interfaces of ITableFontProvider and
 * ITableColorProvider
 *
 * @since 3.2
 */
public class Snippet017TableViewerHideShowColumns {
	private class ShrinkThread extends Thread {
		private int width = 0;
		private TableColumn column;

		public ShrinkThread(int width, TableColumn column) {
			super();
			this.width = width;
			this.column = column;
		}

		@Override
		public void run() {
			column.getDisplay().syncExec(new Runnable() {

				@Override
				public void run() {
					column.setData("restoredWidth", new Integer(width));
				}
			});

			for (int i = width; i >= 0; i--) {
				final int index = i;
				column.getDisplay().syncExec(new Runnable() {

					@Override
					public void run() {
						column.setWidth(index);
					}

				});
			}
		}
	};

	private class ExpandThread extends Thread {
		private int width = 0;
		private TableColumn column;

		public ExpandThread(int width, TableColumn column) {
			super();
			this.width = width;
			this.column = column;
		}

		@Override
		public void run() {
			for (int i = 0; i <= width; i++) {
				final int index = i;
				column.getDisplay().syncExec(new Runnable() {

					@Override
					public void run() {
						column.setWidth(index);
					}

				});
			}
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

	public class MyLabelProvider extends LabelProvider<MyModel> implements
			ITableLabelProvider<MyModel> {

		@Override
		public Image getColumnImage(MyModel element, int columnIndex) {
			return null;
		}

		@Override
		public String getColumnText(MyModel element, int columnIndex) {
			return "Column " + columnIndex + " => " + element.toString();
		}
	}

	public Snippet017TableViewerHideShowColumns(Shell shell) {
		final TableViewer<MyModel, List<MyModel>> v = new TableViewer<MyModel, List<MyModel>>(
				shell, SWT.BORDER | SWT.FULL_SELECTION);
		v.setLabelProvider(new MyLabelProvider());
		v.setContentProvider(ArrayContentProvider.getInstance(MyModel.class));

		TableColumn column = new TableColumn(v.getTable(), SWT.NONE);
		column.setWidth(200);
		column.setText("Column 1");

		column = new TableColumn(v.getTable(), SWT.NONE);
		column.setWidth(200);
		column.setText("Column 2");

		column = new TableColumn(v.getTable(), SWT.NONE);
		column.setWidth(200);
		column.setText("Column 3");

		List<MyModel> model = createModel();
		v.setInput(model);
		v.getTable().setLinesVisible(true);
		v.getTable().setHeaderVisible(true);
		addMenu(v);
	}

	private void addMenu(TableViewer v) {
		final MenuManager mgr = new MenuManager();
		Action action;

		for (int i = 0; i < v.getTable().getColumnCount(); i++) {
			final TableColumn column = v.getTable().getColumn(i);

			action = new Action(v.getTable().getColumn(i).getText(), SWT.CHECK) {
				@Override
				public void runWithEvent(Event event) {
					if (!isChecked()) {
						ShrinkThread t = new ShrinkThread(column.getWidth(),
								column);
						t.run();
					} else {
						ExpandThread t = new ExpandThread(
								((Integer) column.getData("restoredWidth"))
										.intValue(),
								column);
						t.run();
					}
				}

			};
			action.setChecked(true);
			mgr.add(action);
		}

		v.getControl().setMenu(mgr.createContextMenu(v.getControl()));
	}

	private List<MyModel> createModel() {
		List<MyModel> elements = new ArrayList<MyModel>(10);
		for (int i = 0; i < 10; i++) {
			elements.add(i, new MyModel(i));
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
		new Snippet017TableViewerHideShowColumns(shell);
		shell.open();

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}

		display.dispose();

	}

}
