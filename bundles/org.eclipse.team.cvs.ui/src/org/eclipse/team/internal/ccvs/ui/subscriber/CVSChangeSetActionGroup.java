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
package org.eclipse.team.internal.ccvs.ui.subscriber;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;
import org.eclipse.team.ui.synchronize.SynchronizePageActionGroup;

/**
 * Action group that is used by CVS Change Set Capabilities
 */
public class CVSChangeSetActionGroup extends SynchronizePageActionGroup {

    private OpenChangeSetAction openCommitSet;
    
	public void initialize(ISynchronizePageConfiguration configuration) {
		super.initialize(configuration);
		openCommitSet = new OpenChangeSetAction(configuration);
	}
	
	/* (non-Javadoc)
     * @see org.eclipse.team.ui.synchronize.SynchronizePageActionGroup#fillContextMenu(org.eclipse.jface.action.IMenuManager)
     */
    public void fillContextMenu(IMenuManager menu) {
        if (getConfiguration().getMode() == ISynchronizePageConfiguration.OUTGOING_MODE) {
    		appendToGroup(
    				menu, 
    				ISynchronizePageConfiguration.FILE_GROUP, 
    				openCommitSet);
        }
    }
}
