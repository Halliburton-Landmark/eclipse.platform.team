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
package org.eclipse.team.ui.sync.actions.workingsets;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.internal.ui.Utils;
import org.eclipse.team.ui.sync.ISynchronizeView;
import org.eclipse.team.ui.sync.TeamSubscriberParticipant;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IKeyBindingService;
import org.eclipse.ui.IWorkingSet;

public class WorkingSetDropDownAction extends Action implements IMenuCreator {
	
	private MenuManager fMenu;
	private WorkingSetFilterActionGroup wsgroup;
	
	public WorkingSetDropDownAction(Shell shell, IPropertyChangeListener workingSetUpdater, ISynchronizeView view, TeamSubscriberParticipant participant) {
		Utils.initAction(this, "action.workingSets."); //$NON-NLS-1$
		IKeyBindingService kbs = view.getSite().getKeyBindingService();
		setMenuCreator(this);
		wsgroup = new WorkingSetFilterActionGroup(shell, workingSetUpdater, view, participant);
		wsgroup.setMenuDynamic(true);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IMenuCreator#dispose()
	 */
	public void dispose() {
		if (fMenu != null) {
			fMenu.dispose();
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IMenuCreator#getMenu(org.eclipse.swt.widgets.Menu)
	 */
	public Menu getMenu(Menu parent) {
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IMenuCreator#getMenu(org.eclipse.swt.widgets.Control)
	 */
	public Menu getMenu(Control parent) {
		if (fMenu != null) {
			fMenu.dispose();
		}		
		fMenu= new MenuManager();
		fMenu.createContextMenu(parent);
		wsgroup.fillActionBars(new IActionBars() {
			public void clearGlobalActionHandlers() {
			}
			public IAction getGlobalActionHandler(String actionId) {
				return null;
			}
			public IMenuManager getMenuManager() {
				return fMenu;
			}
			public IStatusLineManager getStatusLineManager() {
				return null;
			}
			public IToolBarManager getToolBarManager() {
				return null;
			}
			public void setGlobalActionHandler(String actionId, IAction handler) {
			}
			public void updateActionBars() {
			}
		});
		fMenu.update(true);
		return fMenu.getMenu();
	}
	
	protected void addActionToMenu(Menu parent, Action action) {
		ActionContributionItem item= new ActionContributionItem(action);
		item.fill(parent, -1);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run() {
		// do nothing - this is a menu
	}
	
	public void setWorkingSet(IWorkingSet set) {
		wsgroup.setWorkingSet(set);
	}
	
	public void fillContextMenu(IMenuManager mgr) {
		wsgroup.fillContextMenu(mgr);
	}
}
