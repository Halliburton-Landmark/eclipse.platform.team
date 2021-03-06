/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.ui.subscriber;

import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.team.internal.ccvs.ui.ICVSUIConstants;
import org.eclipse.team.internal.ui.synchronize.ChangeSetCapability;
import org.eclipse.team.internal.ui.synchronize.SyncInfoSetChangeSetCollector;
import org.eclipse.team.ui.synchronize.*;


public class CVSChangeSetCapability extends ChangeSetCapability {

    /* (non-Javadoc)
     * @see org.eclipse.team.ui.synchronize.ChangeSetCapability#supportsCheckedInChangeSets()
     */
    public boolean supportsCheckedInChangeSets() {
        return true;
    }

    /* (non-Javadoc)
     * @see org.eclipse.team.ui.synchronize.ChangeSetCapability#supportsActiveChangeSets()
     */
    public boolean supportsActiveChangeSets() {
        return getActiveChangeSetManager() != null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.team.ui.synchronize.ChangeSetCapability#createCheckedInChangeSetCollector(org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration)
     */
    public SyncInfoSetChangeSetCollector createSyncInfoSetChangeSetCollector(ISynchronizePageConfiguration configuration) {
        return new CVSChangeSetCollector(configuration);
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.team.ui.synchronize.ChangeSetCapability#getActionGroup()
     */
    public SynchronizePageActionGroup getActionGroup() {
        return new CVSChangeSetActionGroup();
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.team.ui.synchronize.ChangeSetCapability#enableChangeSetsByDefault()
     */
    public boolean enableChangeSetsByDefault() {
        return CVSUIPlugin.getPlugin().getPreferenceStore().getBoolean(ICVSUIConstants.PREF_COMMIT_SET_DEFAULT_ENABLEMENT);
    }
}
