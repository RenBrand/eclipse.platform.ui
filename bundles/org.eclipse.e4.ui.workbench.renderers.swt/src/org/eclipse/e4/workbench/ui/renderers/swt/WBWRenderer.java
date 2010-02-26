/*******************************************************************************
 * Copyright (c) 2008, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.workbench.ui.renderers.swt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.inject.Inject;
import org.eclipse.e4.core.services.Logger;
import org.eclipse.e4.core.services.annotations.PostConstruct;
import org.eclipse.e4.core.services.annotations.PreDestroy;
import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.core.services.context.spi.IContextConstants;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MContext;
import org.eclipse.e4.ui.model.application.MElementContainer;
import org.eclipse.e4.ui.model.application.MPart;
import org.eclipse.e4.ui.model.application.MTrimContainer;
import org.eclipse.e4.ui.model.application.MUIElement;
import org.eclipse.e4.ui.model.application.MWindow;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.services.IStylingEngine;
import org.eclipse.e4.ui.services.events.IEventBroker;
import org.eclipse.e4.workbench.modeling.EPartService;
import org.eclipse.e4.workbench.modeling.ISaveHandler;
import org.eclipse.e4.workbench.ui.IPresentationEngine;
import org.eclipse.e4.workbench.ui.UIEvents;
import org.eclipse.e4.workbench.ui.internal.Workbench;
import org.eclipse.e4.workbench.ui.renderers.swt.dnd.DnDManager;
import org.eclipse.e4.workbench.ui.renderers.swt.dnd.DragHost;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

/**
 * Render a Window or Workbench Window.
 */
public class WBWRenderer extends SWTPartRenderer {

	private class WindowSizeUpdateJob implements Runnable {
		public List<MWindow> windowsToUpdate = new ArrayList<MWindow>();

		public void run() {
			clearSizeUpdate();
			while (!windowsToUpdate.isEmpty()) {
				MWindow window = windowsToUpdate.remove(0);
				Shell shell = (Shell) window.getWidget();
				if (shell == null || shell.isDisposed())
					continue;

				shell.setBounds(window.getX(), window.getY(),
						window.getWidth(), window.getHeight());
			}
		}
	}

	WindowSizeUpdateJob boundsJob;

	void clearSizeUpdate() {
		boundsJob = null;
	}

	boolean ignoreSizeChanges = false;

	@Inject
	Logger logger;

	@Inject
	private IEventBroker eventBroker;

	private EventHandler shellUpdater;
	private EventHandler visibilityHandler;
	private EventHandler sizeHandler;

	public WBWRenderer() {
		super();
	}

	@PostConstruct
	public void init() {
		shellUpdater = new EventHandler() {
			public void handleEvent(Event event) {
				// Ensure that this event is for a MMenuItem
				Object objElement = event
						.getProperty(UIEvents.EventTags.ELEMENT);
				if (!(event.getProperty(UIEvents.EventTags.ELEMENT) instanceof MWindow))
					return;

				// Is this listener interested ?
				MWindow windowModel = (MWindow) objElement;
				if (windowModel.getRenderer() != WBWRenderer.this)
					return;

				// No widget == nothing to update
				Shell theShell = (Shell) windowModel.getWidget();
				if (theShell == null)
					return;

				String attName = (String) event
						.getProperty(UIEvents.EventTags.ATTNAME);

				if (UIEvents.UILabel.LABEL.equals(attName)) {
					String newTitle = (String) event
							.getProperty(UIEvents.EventTags.NEW_VALUE);
					theShell.setText(newTitle);
				} else if (UIEvents.UILabel.ICONURI.equals(attName)) {
					theShell.setImage(getImage(windowModel));
				} else if (UIEvents.UILabel.TOOLTIP.equals(attName)) {
					String newTTip = (String) event
							.getProperty(UIEvents.EventTags.NEW_VALUE);
					theShell.setToolTipText(newTTip);
				}
			}
		};

		eventBroker.subscribe(UIEvents.buildTopic(UIEvents.UILabel.TOPIC),
				shellUpdater);

		visibilityHandler = new EventHandler() {
			public void handleEvent(Event event) {
				// Ensure that this event is for a MMenuItem
				Object objElement = event
						.getProperty(UIEvents.EventTags.ELEMENT);
				if (!(objElement instanceof MWindow))
					return;

				// Is this listener interested ?
				MWindow windowModel = (MWindow) objElement;
				if (windowModel.getRenderer() != WBWRenderer.this)
					return;

				// No widget == nothing to update
				Shell theShell = (Shell) windowModel.getWidget();
				if (theShell == null)
					return;

				String attName = (String) event
						.getProperty(UIEvents.EventTags.ATTNAME);

				if (UIEvents.UIElement.VISIBLE.equals(attName)) {
					boolean isVisible = (Boolean) event
							.getProperty(UIEvents.EventTags.NEW_VALUE);
					theShell.setVisible(isVisible);
				}
			}
		};

		eventBroker.subscribe(UIEvents.buildTopic(UIEvents.UIElement.TOPIC,
				UIEvents.UIElement.VISIBLE), visibilityHandler);

		sizeHandler = new EventHandler() {
			public void handleEvent(Event event) {
				if (ignoreSizeChanges)
					return;

				// Ensure that this event is for a MMenuItem
				Object objElement = event
						.getProperty(UIEvents.EventTags.ELEMENT);
				if (!(objElement instanceof MWindow)) {
					return;
				}

				// Is this listener interested ?
				MWindow windowModel = (MWindow) objElement;
				if (windowModel.getRenderer() != WBWRenderer.this) {
					return;
				}

				// No widget == nothing to update
				Shell theShell = (Shell) windowModel.getWidget();
				if (theShell == null) {
					return;
				}

				String attName = (String) event
						.getProperty(UIEvents.EventTags.ATTNAME);

				if (UIEvents.Window.X.equals(attName)
						|| UIEvents.Window.Y.equals(attName)
						|| UIEvents.Window.WIDTH.equals(attName)
						|| UIEvents.Window.HEIGHT.equals(attName)) {
					if (boundsJob == null) {
						boundsJob = new WindowSizeUpdateJob();
						boundsJob.windowsToUpdate.add(windowModel);
						theShell.getDisplay().asyncExec(boundsJob);
					} else {
						if (!boundsJob.windowsToUpdate.contains(windowModel))
							boundsJob.windowsToUpdate.add(windowModel);
					}
				}
			}
		};

		eventBroker.subscribe(UIEvents.buildTopic(UIEvents.Window.TOPIC),
				sizeHandler);

		context.set(ISaveHandler.class.getName(), new ISaveHandler() {

			public Save promptToSave(MPart dirtyPart) {
				Shell shell = (Shell) context
						.get(IServiceConstants.ACTIVE_SHELL);
				Object[] elements = promptForSave(shell, Collections
						.singleton(dirtyPart));
				if (elements == null) {
					return Save.CANCEL;
				}
				return elements.length == 0 ? Save.NO : Save.YES;
			}

			public Save[] promptToSave(Collection<MPart> dirtyParts) {
				List<MPart> parts = new ArrayList<MPart>(dirtyParts);
				Shell shell = (Shell) context
						.get(IServiceConstants.ACTIVE_SHELL);
				Save[] response = new Save[dirtyParts.size()];
				Object[] elements = promptForSave(shell, parts);
				if (elements == null) {
					Arrays.fill(response, Save.CANCEL);
				} else {
					Arrays.fill(response, Save.NO);
					for (int i = 0; i < elements.length; i++) {
						response[parts.indexOf(elements[i])] = Save.YES;
					}
				}
				return response;
			}

		});
	}

	@PreDestroy
	public void contextDisposed() {
		eventBroker.unsubscribe(shellUpdater);
		eventBroker.unsubscribe(visibilityHandler);
		eventBroker.unsubscribe(sizeHandler);
	}

	public Object createWidget(MUIElement element, Object parent) {
		final Widget newWidget;

		if (!(element instanceof MWindow)
				|| (parent != null && !(parent instanceof Shell)))
			return null;

		MWindow wbwModel = (MWindow) element;

		Shell parentShell = (Shell) parent;

		IEclipseContext parentContext = getContextForParent(element);
		Shell wbwShell;
		if (parentShell == null) {
			wbwShell = new Shell(Display.getCurrent(), SWT.SHELL_TRIM);
		} else if (wbwModel.getTags().contains(DragHost.DragHostId)) {
			wbwShell = new Shell(parentShell, SWT.BORDER);
			wbwShell.setAlpha(110);
		} else {
			wbwShell = new Shell(parentShell, SWT.SHELL_TRIM);
		}
		wbwShell.setBackgroundMode(SWT.INHERIT_DEFAULT);
		wbwShell.setBounds(wbwModel.getX(), wbwModel.getY(), wbwModel
				.getWidth(), wbwModel.getHeight());
		wbwShell.setVisible(element.isVisible());

		wbwShell.setLayout(new FillLayout());
		newWidget = wbwShell;
		bindWidget(element, newWidget);

		// set up context
		IEclipseContext localContext = getContext(wbwModel);
		localContext.set(IContextConstants.DEBUG_STRING, "MWindow"); //$NON-NLS-1$
		parentContext.set(IContextConstants.ACTIVE_CHILD, localContext);

		// Add the shell into the WBW's context
		localContext.set(Shell.class.getName(), wbwShell);
		localContext.set(Workbench.LOCAL_ACTIVE_SHELL, wbwShell);

		if (element instanceof MWindow) {
			TrimmedPartLayout tl = new TrimmedPartLayout(wbwShell);
			wbwShell.setLayout(tl);
		} else {
			wbwShell.setLayout(new FillLayout());
		}
		if (wbwModel.getLabel() != null)
			wbwShell.setText(wbwModel.getLabel());

		wbwShell.setImage(getImage(wbwModel));

		// Install the drag and drop handler on all regular windows
		if (!wbwModel.getTags().contains(DragHost.DragHostId)) {
			new DnDManager(wbwModel);
		}

		return newWidget;
	}

	@Override
	public void hookControllerLogic(MUIElement me) {
		super.hookControllerLogic(me);

		Widget widget = (Widget) me.getWidget();

		if (widget instanceof Shell && me instanceof MWindow) {
			final Shell shell = (Shell) widget;
			final MWindow w = (MWindow) me;
			shell.addControlListener(new ControlListener() {
				public void controlResized(ControlEvent e) {
					try {
						ignoreSizeChanges = true;
						w.setWidth(shell.getSize().x);
						w.setHeight(shell.getSize().y);
					} finally {
						ignoreSizeChanges = false;
					}
				}

				public void controlMoved(ControlEvent e) {
					try {
						ignoreSizeChanges = true;
						w.setX(shell.getLocation().x);
						w.setY(shell.getLocation().y);
					} finally {
						ignoreSizeChanges = false;
					}
				}
			});

			shell.addShellListener(new ShellAdapter() {
				public void shellClosed(ShellEvent e) {
					MContext context = (MContext) e.widget.getData(OWNING_ME);
					EPartService partService = (EPartService) context
							.getContext().get(EPartService.class.getName());
					if (partService != null) {
						e.doit = partService.saveAll(true);
					}
				}
			});
			shell.addListener(SWT.Activate, new Listener() {
				public void handleEvent(org.eclipse.swt.widgets.Event event) {
					IEclipseContext parentContext = getContextForParent(w);
					MApplication app = (MApplication) w.getContext().get(
							MApplication.class.getName());
					if (app != null && parentContext != null) {
						if (app.getSelectedElement() == w) {
							return;
						}
						app.setSelectedElement(w);
						parentContext.set(IContextConstants.ACTIVE_CHILD, w
								.getContext());
					}
				}
			});
		}
	}

	/*
	 * Processing the contents of a Workbench window has to take into account
	 * that theere may be trim elements contained in its child list. Since the
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.e4.workbench.ui.renderers.swt.SWTPartFactory#processContents
	 * (org.eclipse.e4.ui.model.application.MPart)
	 */
	@Override
	public void processContents(MElementContainer<MUIElement> me) {
		if (!(((MUIElement) me) instanceof MWindow))
			return;
		MWindow wbwModel = (MWindow) ((MUIElement) me);
		super.processContents(me);

		// Populate the main menu
		if (wbwModel.getMainMenu() != null) {
			IPresentationEngine renderer = (IPresentationEngine) context
					.get(IPresentationEngine.class.getName());
			renderer.createGui(wbwModel.getMainMenu(), me.getWidget());
			Shell shell = (Shell) me.getWidget();
			shell.setMenuBar((Menu) wbwModel.getMainMenu().getWidget());
			// createMenu(me, me.getWidget(), wbwModel.getMainMenu());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.e4.ui.workbench.swt.internal.AbstractPartRenderer#getUIContainer
	 * (org.eclipse.e4.ui.model.application.MUIElement)
	 */
	@Override
	public Object getUIContainer(MUIElement element) {
		if (element instanceof MTrimContainer<?>)
			return super.getUIContainer(element);

		Composite shellComp = (Composite) element.getParent().getWidget();
		if (element instanceof MWindow)
			return shellComp;

		TrimmedPartLayout tpl = (TrimmedPartLayout) shellComp.getLayout();
		return tpl.clientArea;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.e4.workbench.ui.renderers.PartFactory#postProcess(org.eclipse
	 * .e4.ui.model.application.MPart)
	 */
	@Override
	public void postProcess(MUIElement childME) {
		super.postProcess(childME);

		Shell shell = (Shell) childME.getWidget();
		shell.layout(true);
	}

	private Object[] promptForSave(Shell parentShell,
			Collection<MPart> saveableParts) {
		SaveablePartPromptDialog dialog = new SaveablePartPromptDialog(
				parentShell, saveableParts);
		if (dialog.open() == Window.CANCEL) {
			return null;
		}

		return dialog.getCheckedElements();
	}

	@Inject
	private IEclipseContext context;

	private void applyDialogStyles(Control control) {
		IStylingEngine engine = (IStylingEngine) context
				.get(IStylingEngine.SERVICE_NAME);
		if (engine != null) {
			Shell shell = control.getShell();
			if (shell.getBackgroundMode() == SWT.INHERIT_NONE) {
				shell.setBackgroundMode(SWT.INHERIT_DEFAULT);
			}

			engine.style(shell);
		}
	}

	class SaveablePartPromptDialog extends Dialog {

		private Collection<MPart> collection;

		private CheckboxTableViewer tableViewer;

		private Object[] checkedElements = new Object[0];

		SaveablePartPromptDialog(Shell shell, Collection<MPart> collection) {
			super(shell);
			this.collection = collection;
		}

		@Override
		protected Control createDialogArea(Composite parent) {
			parent = (Composite) super.createDialogArea(parent);

			Label label = new Label(parent, SWT.LEAD);
			label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			label.setText("Select the parts to save:"); //$NON-NLS-1$

			tableViewer = CheckboxTableViewer.newCheckList(parent, SWT.SINGLE
					| SWT.BORDER);
			GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
			data.heightHint = 250;
			data.widthHint = 300;
			tableViewer.getControl().setLayoutData(data);
			tableViewer.setLabelProvider(new LabelProvider() {
				@Override
				public String getText(Object element) {
					return ((MPart) element).getLabel();
				}
			});
			tableViewer.setContentProvider(ArrayContentProvider.getInstance());
			tableViewer.setInput(collection);
			tableViewer.setAllChecked(true);

			return parent;
		}

		@Override
		public void create() {
			super.create();
			applyDialogStyles(getShell());
		}

		@Override
		protected void okPressed() {
			checkedElements = tableViewer.getCheckedElements();
			super.okPressed();
		}

		public Object[] getCheckedElements() {
			return checkedElements;
		}

	}

}