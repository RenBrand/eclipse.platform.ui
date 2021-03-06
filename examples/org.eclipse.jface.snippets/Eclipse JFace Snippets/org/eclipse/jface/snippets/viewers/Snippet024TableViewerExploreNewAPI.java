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

import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * Example usage of ITableLabelProvider using images and labels
 *
 * @author Tom Schindl <tom.schindl@bestsolution.at>
 *
 */
public class Snippet024TableViewerExploreNewAPI {

	public class Person {
		public String givenname;
		public String surname;
		public String email;

		public Person(String givenname, String surname, String email) {
			this.givenname = givenname;
			this.surname = surname;
			this.email = email;
		}

	}

	protected abstract class AbstractEditingSupport extends
			EditingSupport<Person, List<Person>> {
		private TextCellEditor editor;

		public AbstractEditingSupport(TableViewer<Person, List<Person>> viewer) {
			super(viewer);
			this.editor = new TextCellEditor(viewer.getTable());
		}

		@Override
		protected boolean canEdit(Person element) {
			return true;
		}

		@Override
		protected CellEditor getCellEditor(Person element) {
			return editor;
		}

		@Override
		protected void setValue(Person element, Object value) {
			doSetValue(element, value);
			getViewer().update(element, null);
		}

		protected abstract void doSetValue(Object element, Object value);
	}

	public Snippet024TableViewerExploreNewAPI(Shell shell) {
		TableViewer<Person, List<Person>> v = new TableViewer<Person, List<Person>>(
				shell, SWT.BORDER | SWT.FULL_SELECTION);
		v.setContentProvider(ArrayContentProvider.getInstance(Person.class));

		TableViewerColumn<Person, List<Person>> column = createColumnFor(v,
				"Givenname");
		column.setLabelProvider(new ColumnLabelProvider<Person, List<Person>>() {

			@Override
			public String getText(Person element) {
				return element.givenname;
			}
		});

		column.setEditingSupport(new AbstractEditingSupport(v) {

			@Override
			protected void doSetValue(Object element, Object value) {
				((Person) element).givenname = value.toString();
			}

			@Override
			protected Object getValue(Person element) {
				return element.givenname;
			}

		});

		column = createColumnFor(v, "Surname");
		column.setLabelProvider(new ColumnLabelProvider<Person, List<Person>>() {

			@Override
			public String getText(Person element) {
				return element.surname;
			}

		});

		column.setEditingSupport(new AbstractEditingSupport(v) {

			@Override
			protected void doSetValue(Object element, Object value) {
				((Person) element).surname = value.toString();
			}

			@Override
			protected Object getValue(Person element) {
				return element.surname;
			}

		});

		column = createColumnFor(v, "E-Mail");
		column.setLabelProvider(new ColumnLabelProvider<Person, List<Person>>() {

			@Override
			public String getText(Person element) {
				return element.email;
			}

		});

		column.setEditingSupport(new AbstractEditingSupport(v) {

			@Override
			protected void doSetValue(Object element, Object value) {
				((Person) element).email = value.toString();
			}

			@Override
			protected Object getValue(Person element) {
				return element.email;
			}

		});

		v.setInput(createModel());
		v.getTable().setLinesVisible(true);
		v.getTable().setHeaderVisible(true);
	}

	private TableViewerColumn<Person, List<Person>> createColumnFor(
			TableViewer<Person, List<Person>> viewer, String label) {
		TableViewerColumn<Person, List<Person>> column = new TableViewerColumn<Person, List<Person>>(
				viewer, SWT.NONE);
		column.getColumn().setWidth(200);
		column.getColumn().setText(label);
		column.getColumn().setMoveable(true);
		return column;
	}

	private List<Person> createModel() {
		return Arrays.asList(new Person("Tom", "Schindl",
				"tom.schindl@bestsolution.at"), new Person("Boris", "Bokowski",
				"Boris_Bokowski@ca.ibm.com"), new Person("Tod", "Creasey",
				"Tod_Creasey@ca.ibm.com"), new Person("Wayne", "Beaton",
				"wayne@eclipse.org"), new Person("Lars", "Vogel",
				"lars.vogel@gmail.com"), new Person("Hendrik", "Still",
				"hendrik.still@vogella.com"), new Person("Jeanderson",
				"Candido", "jeandersonbc@gmail.com"));
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Display display = new Display();

		Shell shell = new Shell(display);
		shell.setLayout(new FillLayout());
		new Snippet024TableViewerExploreNewAPI(shell);
		shell.open();

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
	}

}
