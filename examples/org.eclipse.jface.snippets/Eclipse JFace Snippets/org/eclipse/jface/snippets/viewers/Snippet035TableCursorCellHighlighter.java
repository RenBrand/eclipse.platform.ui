/*******************************************************************************
 * Copyright (c) 2006, 2014 Tom Schindl and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl - initial API and implementation
 *     Lars Vogel (lars.vogel@gmail.com) - Bug 413427
 *     Jeanderson Candido <http://jeandersonbc.github.io> - Bug 414565
 *     Hendrik Still <hendrik.still@gammas.de> - bug 417676
 *******************************************************************************/

package org.eclipse.jface.snippets.viewers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewerEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableFontProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerEditor;
import org.eclipse.jface.viewers.TableViewerFocusCellManager;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableColumn;

/**
 * Example usage of none mandatory interfaces of ITableFontProvider and
 * ITableColorProvider
 */
public class Snippet035TableCursorCellHighlighter {

	public static boolean flag = true;

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
			ITableLabelProvider<MyModel>, ITableFontProvider<MyModel>,
			ITableColorProvider<MyModel> {
		FontRegistry registry = new FontRegistry();

		@Override
		public Image getColumnImage(MyModel element, int columnIndex) {
			return null;
		}

		@Override
		public String getColumnText(MyModel element, int columnIndex) {
			return "Column " + columnIndex + " => " + element.toString();
		}

		@Override
		public Font getFont(MyModel element, int columnIndex) {
			if (element.counter % 2 == 0) {
				return registry.getBold(Display.getCurrent().getSystemFont()
						.getFontData()[0].getName());
			}
			return null;
		}

		@Override
		public Color getBackground(MyModel element, int columnIndex) {
			if (element.counter % 2 == 0) {
				return Display.getCurrent().getSystemColor(SWT.COLOR_RED);
			}
			return null;
		}

		@Override
		public Color getForeground(MyModel element, int columnIndex) {
			if (element.counter % 2 == 1) {
				return Display.getCurrent().getSystemColor(SWT.COLOR_RED);
			}
			return null;
		}

	}

	public Snippet035TableCursorCellHighlighter(Shell shell) {
		final TableViewer<MyModel, List<MyModel>> v = new TableViewer<MyModel, List<MyModel>>(
				shell, SWT.BORDER | SWT.HIDE_SELECTION | SWT.FULL_SELECTION);
		v.setLabelProvider(new MyLabelProvider());
		v.setContentProvider(ArrayContentProvider.getInstance(MyModel.class));

		v.setCellEditors(new CellEditor[] { new TextCellEditor(v.getTable()),
				new TextCellEditor(v.getTable()) });
		v.setCellModifier(new ICellModifier<MyModel>() {

			@Override
			public void modify(Object element, String property, Object value) {

			}

			@Override
			public boolean canModify(MyModel element, String property) {
				return true;
			}

			@Override
			public Object getValue(MyModel element, String property) {
				return "Column " + property + " => " + element.toString();
			}

		});
		v.setColumnProperties(new String[] { "1", "2" });

		TableViewerFocusCellManager<MyModel, List<MyModel>> focusCellManager = new TableViewerFocusCellManager<MyModel, List<MyModel>>(
				v, new CursorCellHighlighter<MyModel, List<MyModel>>(v,
						new TableCursor(v)));
		ColumnViewerEditorActivationStrategy<MyModel, List<MyModel>> actSupport = new ColumnViewerEditorActivationStrategy<MyModel, List<MyModel>>(
				v) {
			@Override
			protected boolean isEditorActivationEvent(
					ColumnViewerEditorActivationEvent event) {
				return event.eventType == ColumnViewerEditorActivationEvent.TRAVERSAL
						|| event.eventType == ColumnViewerEditorActivationEvent.MOUSE_DOUBLE_CLICK_SELECTION
						|| (event.eventType == ColumnViewerEditorActivationEvent.KEY_PRESSED && event.keyCode == SWT.CR)
						|| event.eventType == ColumnViewerEditorActivationEvent.PROGRAMMATIC;
			}
		};
		TableViewerEditor.create(v, focusCellManager, actSupport,
				ColumnViewerEditor.TABBING_HORIZONTAL
						| ColumnViewerEditor.TABBING_MOVE_TO_ROW_NEIGHBOR
						| ColumnViewerEditor.TABBING_VERTICAL
						| ColumnViewerEditor.KEYBOARD_ACTIVATION);

		TableColumn column = new TableColumn(v.getTable(), SWT.NONE);
		column.setWidth(200);
		column.setText("Column 1");

		column = new TableColumn(v.getTable(), SWT.NONE);
		column.setWidth(200);
		column.setText("Column 2");

		List<MyModel> model = createModel();
		v.setInput(model);
		v.getTable().setLinesVisible(true);
		v.getTable().setHeaderVisible(true);
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
		new Snippet035TableCursorCellHighlighter(shell);
		shell.open();

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();

	}

}
