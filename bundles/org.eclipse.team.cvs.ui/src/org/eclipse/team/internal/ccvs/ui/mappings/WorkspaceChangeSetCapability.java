/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.ui.mappings;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.window.Window;
import org.eclipse.team.core.diff.IDiff;
import org.eclipse.team.core.mapping.provider.ResourceDiffTree;
import org.eclipse.team.internal.ccvs.ui.*;
import org.eclipse.team.internal.ccvs.ui.subscriber.CommitSetDialog;
import org.eclipse.team.internal.core.subscribers.ActiveChangeSet;
import org.eclipse.team.internal.core.subscribers.SubscriberChangeSetCollector;
import org.eclipse.team.internal.ui.synchronize.ChangeSetCapability;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;

public class WorkspaceChangeSetCapability extends ChangeSetCapability {

    /* (non-Javadoc)
     * @see org.eclipse.team.internal.ui.synchronize.ChangeSetCapability#enableChangeSetsByDefault()
     */
    public boolean enableChangeSetsByDefault() {
        return CVSUIPlugin.getPlugin().getPreferenceStore().getBoolean(ICVSUIConstants.PREF_COMMIT_SET_DEFAULT_ENABLEMENT);
    }
    
	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ui.synchronize.ChangeSetCapability#supportsActiveChangeSets()
	 */
	public boolean supportsActiveChangeSets() {
		return true;
	}
	
	public boolean enableActiveChangeSetsFor(ISynchronizePageConfiguration configuration) {
        return supportsActiveChangeSets() && 
    	configuration.getMode() != ISynchronizePageConfiguration.INCOMING_MODE;
	}
	
    /* (non-Javadoc)
     * @see org.eclipse.team.internal.ui.synchronize.ChangeSetCapability#createChangeSet(org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration, org.eclipse.team.core.diff.IDiff[])
     */
    public ActiveChangeSet createChangeSet(ISynchronizePageConfiguration configuration, IDiff[] infos) {
        ActiveChangeSet set = getActiveChangeSetManager().createSet(CVSUIMessages.WorkspaceChangeSetCapability_1, new IDiff[0]); 
		CommitSetDialog dialog = new CommitSetDialog(configuration.getSite().getShell(), set, getResources(infos),
		        CVSUIMessages.WorkspaceChangeSetCapability_2, CVSUIMessages.WorkspaceChangeSetCapability_3); // 
		dialog.open();
		if (dialog.getReturnCode() != Window.OK) return null;
		set.add(infos);
		return set;
    }

    private IResource[] getResources(IDiff[] diffs) {
    	Set result = new HashSet();
    	for (int i = 0; i < diffs.length; i++) {
			IDiff diff = diffs[i];
			IResource resource = ResourceDiffTree.getResourceFor(diff);
			if (resource != null)
				result.add(resource);
		}
        return (IResource[]) result.toArray(new IResource[result.size()]);
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.team.ui.synchronize.ChangeSetCapability#editChangeSet(org.eclipse.team.core.subscribers.ActiveChangeSet)
     */
    public void editChangeSet(ISynchronizePageConfiguration configuration, ActiveChangeSet set) {
        CommitSetDialog dialog = new CommitSetDialog(configuration.getSite().getShell(), set, set.getResources(),
		        CVSUIMessages.WorkspaceChangeSetCapability_7, CVSUIMessages.WorkspaceChangeSetCapability_8); // 
		dialog.open();
		if (dialog.getReturnCode() != Window.OK) return;
		// Nothing to do here as the set was updated by the dialog 
    }

    /* (non-Javadoc)
     * @see org.eclipse.team.ui.synchronize.ChangeSetCapability#getActiveChangeSetManager()
     */
    public SubscriberChangeSetCollector getActiveChangeSetManager() {
        return CVSUIPlugin.getPlugin().getChangeSetManager();
    }
}