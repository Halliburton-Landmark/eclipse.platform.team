/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.ui.synchronize;

import org.eclipse.team.core.subscribers.ActiveChangeSet;
import org.eclipse.team.core.subscribers.SubscriberChangeSetCollector;
import org.eclipse.team.core.synchronize.SyncInfo;

/**
 * A change set capability is used by a SubscriberSynchronizePage
 * to determine what, if any change set capabilities should be enabled
 * for the pags of the particpant.
 * @since 3.1
 */
public abstract class ChangeSetCapability {

    /**
     * Return whether the associated participant supports
     * the display of checked-in change sets. The default is
     * unsupported (<code>false</code>). If subclasses support
     * checked-in change sets, they must override the 
     * <code>createCheckedInChangeSetCollector</code>
     * method to return an appropriate values.
     * @return whether the associated participant supports
     * the display of checked-in change sets
     */
    public boolean supportsCheckedInChangeSets() {
        return false;
    }
    
    /**
     * Return whether the associated participant supports
     * the use of active change sets. The default is unsupported
     * (<code>false</code>). If a subclass overrides this method in
     * order to support active change sets, they must also override the methods 
     * <code>getActiveChangeSetManager</code>,
     * <code>createChangeSet</code> and <code>editChangeSet</code>.
     * @return whether the associated participant supports
     * the use of active change sets
     */
    public boolean supportsActiveChangeSets() {
        return false;
    }
    
    /**
     * Return the change set collector that manages the active change
     * set for the particpant associated with this capability. A <code>null</code>
     * is returned if active change sets are not supported. The default is to 
     * return <code>null</code>.  This method must be
     * overridden by subclasses that support active change sets.
     * @return the change set collector that manages the active change
     * set for the particpant associated with this capability or
     * <code>null</code> if active change sets are not supported.
     */
    public SubscriberChangeSetCollector getActiveChangeSetManager() {
        return null;
    }
    
    /**
     * Create a change set from the given manager that contains the given sync info.
     * This method is invoked from the UI thread. A <code>null</code>
     * is returned if active change sets are not supported. The default is to 
     * return <code>null</code>.  This method must be
     * overridden by subclasses that support active change sets.
     * @param configuration the configuration of the page displaying the change sets
     * @param infos the sync info to be added to the change set
     * @param manager a change set manager
     * @return the created set.
     */
    public ActiveChangeSet createChangeSet(ISynchronizePageConfiguration configuration, SyncInfo[] infos) {
        return null;
    }
    
    /**
     * Edit the title and comment of the given change set. This method must be
     * overridden by subclasses that support active change sets.
     * This method is invoked from the UI thread.
     * @param configuration the configuration of the page displaying the change sets
     * @param set the set to be edited
     */
    public void editChangeSet(ISynchronizePageConfiguration configuration, ActiveChangeSet set) {
        // Default is to do nothing
    }
    
    /**
     * Return a collector that can be used to group a set of checked-in changes
     * into a set of checked-in change sets.  This method must be
     * overridden by subclasses that support checked-in change sets.
     * @param configuration the configuration for the page that will be displaying the change sets
     * @return a change set collector
     */
    public SyncInfoSetChangeSetCollector createCheckedInChangeSetCollector(ISynchronizePageConfiguration configuration) {
        return null;
    }
    
    /**
     * Return an action group for contributing context menu items
     * to the synchronize page while change sets are enabled.
     * Return <code>null</code> if no custom actions are required.
     * Note that only context menus can be contributed since the view menu
     * and toolbar menu are fixed. This method can be overridden by subclasses
     * who wish to support custom change set actions.
     * @return an action group for contributing context menu items
     * to the synchronize page while change sets are enabled or <code>null</code>
     */
    public SynchronizePageActionGroup getActionGroup() {
        return null;
    }
    
}
