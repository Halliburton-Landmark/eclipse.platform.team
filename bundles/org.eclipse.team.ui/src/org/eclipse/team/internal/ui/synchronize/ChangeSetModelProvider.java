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
package org.eclipse.team.internal.ui.synchronize;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.compare.structuremergeviewer.IDiffElement;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.widgets.Control;
import org.eclipse.team.core.subscribers.ChangeSet;
import org.eclipse.team.core.subscribers.IChangeSetChangeListener;
import org.eclipse.team.core.synchronize.*;
import org.eclipse.team.internal.ui.*;
import org.eclipse.team.internal.ui.synchronize.actions.ChangeSetActionGroup;
import org.eclipse.team.ui.synchronize.*;

/**
 * Model provider for showing change sets in a sync page.
 */
public class ChangeSetModelProvider extends CompositeModelProvider {

    private ViewerSorter viewerSorter = new ChangeSetModelSorter(this, ChangeSetModelSorter.COMMENT);
	
	// The id of the sub-provider
    private final String subProvierId;
	
	private Map rootToProvider = new HashMap(); // Maps ISynchronizeModelElement -> AbstractSynchronizeModelProvider
	
	private ViewerSorter embeddedSorter;
	
	private SyncInfoSetChangeSetCollector checkedInCollector;
	
	private IChangeSetChangeListener checkedInCollectorListener = new IChangeSetChangeListener() {
	    
        /* (non-Javadoc)
         * @see org.eclipse.team.core.subscribers.IChangeSetChangeListener#setAdded(org.eclipse.team.core.subscribers.ChangeSet)
         */
        public void setAdded(ChangeSet set) {
            ISynchronizeModelElement node = getModelElement(set);
            ISynchronizeModelProvider provider = null;
            if (node != null) {
                provider = getProviderRootedAt(node);
            }
            if (provider == null) {
                provider = createProvider(set);
            }
            provider.prepareInput(null);
        }

        /* (non-Javadoc)
         * @see org.eclipse.team.core.subscribers.IChangeSetChangeListener#defaultSetChanged(org.eclipse.team.core.subscribers.ChangeSet, org.eclipse.team.core.subscribers.ChangeSet)
         */
        public void defaultSetChanged(ChangeSet previousDefault, ChangeSet set) {
            // There is no default set for checked-in change sets
        }

        /* (non-Javadoc)
         * @see org.eclipse.team.core.subscribers.IChangeSetChangeListener#setRemoved(org.eclipse.team.core.subscribers.ChangeSet)
         */
        public void setRemoved(ChangeSet set) {
            removeModelElementForSet(set);
        }

        /* (non-Javadoc)
         * @see org.eclipse.team.core.subscribers.IChangeSetChangeListener#nameChanged(org.eclipse.team.core.subscribers.ChangeSet)
         */
        public void nameChanged(ChangeSet set) {
            // The name of checked-in change sets should not change
        }

        /* (non-Javadoc)
         * @see org.eclipse.team.core.subscribers.IChangeSetChangeListener#resourcesChanged(org.eclipse.team.core.subscribers.ChangeSet, org.eclipse.core.resources.IResource[])
         */
        public void resourcesChanged(ChangeSet set, IResource[] resources) {
            // The sub-providers listen directly to the sets for changes
            // There is no global action to be taken for such changes
        }
    };
	
    private ActiveChangeSetCollector activeCollector;
    
    private IChangeSetChangeListener activeChangeSetListener = new IChangeSetChangeListener() {

        public void setAdded(final ChangeSet set) {
            syncExec(new Runnable() {
                public void run() {
                    // Remove any resources that are in the new set
                    activeCollector.remove(set.getResources());
                    createActiveChangeSetModelElement(set);
                }
            });

        }
        
        public void defaultSetChanged(final ChangeSet previousDefault, final ChangeSet set) {
            syncExec(new Runnable() {
                public void run() {
		            // Refresh the label for both of the sets involved
		            refreshLabel(getModelElement(previousDefault));
		            refreshLabel(getModelElement(set));
                }
            });
        }

        private void refreshLabel(final ISynchronizeModelElement node) {
            if (node != null) {
                getViewer().refresh(node);
            }
        }
        
        public void setRemoved(final ChangeSet set) {
            syncExec(new Runnable() {
                public void run() {
                    removeModelElementForSet(set);
		            activeCollector.remove(set);
                }
            });
        }

        public void nameChanged(final ChangeSet set) {
            syncExec(new Runnable() {
                public void run() {
                    refreshLabel(getModelElement(set));
                }
            });
        }

        public void resourcesChanged(final ChangeSet set, final IResource[] resources) {
            // Changes are handled by the sets themselves.
        }
        
    };
    
	/* *****************************************************************************
	 * Descriptor for this model provider
	 */
	public static class ChangeSetModelProviderDescriptor implements ISynchronizeModelProviderDescriptor {
		public static final String ID = TeamUIPlugin.ID + ".modelprovider_cvs_changelog"; //$NON-NLS-1$
		public String getId() {
			return ID;
		}		
		public String getName() {
			return Policy.bind("ChangeLogModelProvider.5"); //$NON-NLS-1$
		}		
		public ImageDescriptor getImageDescriptor() {
			return TeamUIPlugin.getImageDescriptor(ITeamUIImages.IMG_CHANGE_SET);
		}
	};
	private static final ChangeSetModelProviderDescriptor descriptor = new ChangeSetModelProviderDescriptor();
	
    protected ChangeSetModelProvider(ISynchronizePageConfiguration configuration, SyncInfoSet set, String subProvierId) {
        super(configuration, set);
        this.subProvierId = subProvierId;
        ChangeSetCapability changeSetCapability = getChangeSetCapability();
        if (changeSetCapability.supportsCheckedInChangeSets()) {
	        checkedInCollector = changeSetCapability.createCheckedInChangeSetCollector(configuration);
	        checkedInCollector.setProvider(this);
	        checkedInCollector.addListener(checkedInCollectorListener);
        }
        if (changeSetCapability.supportsActiveChangeSets()) {
            activeCollector = new ActiveChangeSetCollector(configuration, this);
            activeCollector.getActiveChangeSetManager().addListener(activeChangeSetListener);
            configuration.addMenuGroup(ISynchronizePageConfiguration.P_CONTEXT_MENU, ChangeSetActionGroup.CHANGE_SET_GROUP);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.team.internal.ui.synchronize.AbstractSynchronizeModelProvider#handleChanges(org.eclipse.team.core.synchronize.ISyncInfoTreeChangeEvent, org.eclipse.core.runtime.IProgressMonitor)
     */
    protected void handleChanges(ISyncInfoTreeChangeEvent event, IProgressMonitor monitor) {
        if (checkedInCollector != null && getConfiguration().getMode() == ISynchronizePageConfiguration.INCOMING_MODE) {
            checkedInCollector.handleChange(event);
        } else if (activeCollector != null && getConfiguration().getMode() == ISynchronizePageConfiguration.OUTGOING_MODE) {
            activeCollector.handleChange(event);
        } else {
            // Forward the event to the root provider
            ISynchronizeModelProvider provider = getProviderRootedAt(getModelRoot());
            if (provider != null) {
                ((SynchronizeModelProvider)provider).syncInfoChanged(event, monitor);
            }
        }
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.team.internal.ui.synchronize.CompositeModelProvider#handleAddition(org.eclipse.team.core.synchronize.SyncInfo)
     */
    protected void handleAddition(SyncInfo info) {
        // Nothing to do since change handling was bypassed
    }

    /* (non-Javadoc)
     * @see org.eclipse.team.internal.ui.synchronize.AbstractSynchronizeModelProvider#buildModelObjects(org.eclipse.team.ui.synchronize.ISynchronizeModelElement)
     */
    protected IDiffElement[] buildModelObjects(ISynchronizeModelElement node) {
        // This method is invoked on a reset after the provider state has been cleared.
        // Resetting the collector will rebuild the model
        
		if (node == getModelRoot()) {
		    
	        // First, disable the collectors
	        checkedInCollector.reset(null);
	        checkedInCollector.removeListener(checkedInCollectorListener);
	        activeCollector.reset(null);
	        activeCollector.getActiveChangeSetManager().removeListener(activeChangeSetListener);
	        
	        // Then, re-enable the proper collection method
		    if (checkedInCollector != null && getConfiguration().getMode() == ISynchronizePageConfiguration.INCOMING_MODE) {
		        checkedInCollector.addListener(checkedInCollectorListener);
		        checkedInCollector.reset(getSyncInfoSet());
		        
		    } else if (activeCollector != null && getConfiguration().getMode() == ISynchronizePageConfiguration.OUTGOING_MODE) {
		        activeCollector.getActiveChangeSetManager().addListener(activeChangeSetListener);
	            activeCollector.reset(getSyncInfoSet());
	        } else {
		        // Forward the sync info to the root provider and trigger a build
	            ISynchronizeModelProvider provider = getProviderRootedAt(getModelRoot());
	            if (provider != null) {
	                ((SynchronizeModelProvider)provider).getSyncInfoSet().addAll(getSyncInfoSet());
	            }
		    }
		}
		return new IDiffElement[0];
    }

    /* (non-Javadoc)
     * @see org.eclipse.team.internal.ui.synchronize.ISynchronizeModelProvider#getDescriptor()
     */
    public ISynchronizeModelProviderDescriptor getDescriptor() {
        return descriptor;
    }

    /* (non-Javadoc)
     * @see org.eclipse.team.internal.ui.synchronize.ISynchronizeModelProvider#getViewerSorter()
     */
    public ViewerSorter getViewerSorter() {
        return viewerSorter;
    }

    /*
     * Method to allow ChangeSetActionGroup to set the viewer sorter of this provider.
     */
    public void setViewerSorter(ViewerSorter viewerSorter) {
        this.viewerSorter = viewerSorter;
        firePropertyChange(ISynchronizeModelProvider.P_VIEWER_SORTER, null, null);
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.team.internal.ui.synchronize.AbstractSynchronizeModelProvider#runViewUpdate(java.lang.Runnable)
     */
    public void runViewUpdate(Runnable runnable) {
        super.runViewUpdate(runnable);
    }

    /* (non-Javadoc)
     * @see org.eclipse.team.internal.ui.synchronize.AbstractSynchronizeModelProvider#createActionGroup()
     */
    protected SynchronizePageActionGroup createActionGroup() {
        return new ChangeSetActionGroup(this);
    }
    
    private ISynchronizeModelProvider createProviderRootedAt(ISynchronizeModelElement parent, SyncInfoTree set) {
        ISynchronizeModelProvider provider = createModelProvider(parent, getSubproviderId(), set);
        addProvider(provider);
        rootToProvider.put(parent, provider);
        return provider;
    }

    private ISynchronizeModelProvider getProviderRootedAt(ISynchronizeModelElement parent) {
        return (ISynchronizeModelProvider)rootToProvider.get(parent);
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.team.internal.ui.synchronize.CompositeModelProvider#removeProvider(org.eclipse.team.internal.ui.synchronize.AbstractSynchronizeModelProvider)
     */
    protected void removeProvider(ISynchronizeModelProvider provider) {
        rootToProvider.remove(provider.getModelRoot());
        super.removeProvider(provider);
    }
    
    /**
     * Return the id of the sub-provider used by the commit set provider.
     * @return the id of the sub-provider used by the commit set provider
     */
    public String getSubproviderId() {
        return subProvierId;
    }

    /**
     * Return the sorter associated with the sub-provider being used.
     * @return the sorter associated with the sub-provider being used
     */
    public ViewerSorter getEmbeddedSorter() {
        return embeddedSorter;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.team.internal.ui.synchronize.CompositeModelProvider#clearModelObjects(org.eclipse.team.ui.synchronize.ISynchronizeModelElement)
     */
    protected void clearModelObjects(ISynchronizeModelElement node) {
        super.clearModelObjects(node);
        if (node == getModelRoot()) {
            rootToProvider.clear();
            // Throw away the embedded sorter
            embeddedSorter = null;
            createRootProvider();
        }
    }

    /*
     * Create the root subprovider which is used to display resources
     * that are not in a commit set. This provider is created even if
     * it is empty so we can have access to the appropriate sorter 
     * and action group 
     */
    private void createRootProvider() {
        // Recreate the sub-provider at the root and use it's viewer sorter and action group
        SyncInfoTree tree;
        if (activeCollector != null && getConfiguration().getMode() == ISynchronizePageConfiguration.OUTGOING_MODE) {
            // When in outgoing mode, use the root set of the active change set collector at the root
            tree = activeCollector.getRootSet();
        } else {
            tree = new SyncInfoTree();
        }
        final ISynchronizeModelProvider provider = createProviderRootedAt(getModelRoot(), tree);
        embeddedSorter = provider.getViewerSorter();
        if (provider instanceof AbstractSynchronizeModelProvider) {
            SynchronizePageActionGroup actionGroup = ((AbstractSynchronizeModelProvider)provider).getActionGroup();
            if (actionGroup != null) {
                // This action group will be disposed when the provider is disposed
                getConfiguration().addActionContribution(actionGroup);
                provider.addPropertyChangeListener(new IPropertyChangeListener() {
                    public void propertyChange(PropertyChangeEvent event) {
                        if (event.getProperty().equals(P_VIEWER_SORTER)) {
                            embeddedSorter = provider.getViewerSorter();
                            ChangeSetModelProvider.this.firePropertyChange(P_VIEWER_SORTER, null, null);
                        }
                    }
                });
            }
        }
    }
    
    /*
     * Create a provider and node for the given change set
     */
    private ISynchronizeModelProvider createProvider(ChangeSet set) {
        ChangeSetDiffNode node = new ChangeSetDiffNode(getModelRoot(), set);
        return createProviderRootedAt(node, set.getSyncInfoSet());
    }
    
    /*
     * Find the root element for the given change set.
     * A linear search is used.
     */
    protected ISynchronizeModelElement getModelElement(ChangeSet set) {
        IDiffElement[] children = getModelRoot().getChildren();
        for (int i = 0; i < children.length; i++) {
            IDiffElement element = children[i];
            if (element instanceof ChangeSetDiffNode && ((ChangeSetDiffNode)element).getSet() == set) {
                return (ISynchronizeModelElement)element;
            }
        }
        return null;
    }

    /*
     * Return the change set capability
     */
    public ChangeSetCapability getChangeSetCapability() {
        return getConfiguration().getParticipant().getChangeSetCapability();
    }
    
    public void dispose() {
        if (checkedInCollector != null) {
	        checkedInCollector.removeListener(checkedInCollectorListener);
	        checkedInCollector.dispose();
        }
        if (activeCollector != null) {
            activeCollector.getActiveChangeSetManager().removeListener(activeChangeSetListener);
        }
        super.dispose();
    }
    
    
    public void waitUntilDone(IProgressMonitor monitor) {
        super.waitUntilDone(monitor);
        if (checkedInCollector != null) checkedInCollector.waitUntilDone(monitor);
    }
    
    private void syncExec(final Runnable runnable) {
		final Control ctrl = getViewer().getControl();
		if (ctrl != null && !ctrl.isDisposed()) {
			ctrl.getDisplay().syncExec(new Runnable() {
				public void run() {
					if (!ctrl.isDisposed()) {
					    runnable.run();
					}
				}
			});
		}
    }
    
    private void removeModelElementForSet(final ChangeSet set) {
        ISynchronizeModelElement node = getModelElement(set);
        if (node != null) {
            ISynchronizeModelProvider provider = getProviderRootedAt(node);
            clearModelObjects(node);
            removeProvider(provider);
        }
    }
    
    public void createActiveChangeSetModelElements() {
        ChangeSet[] sets = activeCollector.getActiveChangeSetManager().getSets();
        for (int i = 0; i < sets.length; i++) {
            ChangeSet set = sets[i];
            createActiveChangeSetModelElement(set);
        }
    }

    public void createActiveChangeSetModelElement(final ChangeSet set) {
        // Add the model element and provider for the set
        ISynchronizeModelElement node = getModelElement(set);
        ISynchronizeModelProvider provider = null;
        if (node != null) {
            provider = getProviderRootedAt(node);
        }
        if (provider == null) {
            provider = createActiveChangeSetProvider(set, activeCollector.getSyncInfoSet(set));
        }
        provider.prepareInput(null);
    }
    
    private ISynchronizeModelProvider createActiveChangeSetProvider(ChangeSet set, SyncInfoTree tree) {
        ChangeSetDiffNode node = new ChangeSetDiffNode(getModelRoot(), set);
        return createProviderRootedAt(node, tree);
    }
}
