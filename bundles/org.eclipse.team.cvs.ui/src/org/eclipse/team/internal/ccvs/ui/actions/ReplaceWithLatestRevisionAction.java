/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.ui.actions;

import org.eclipse.team.internal.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.ui.operations.ReplaceOperation;


public class ReplaceWithLatestRevisionAction extends ReplaceWithTagAction {

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.ui.actions.ReplaceWithTagAction.getTag(ReplaceOperation)
	 */
	protected CVSTag getTag(ReplaceOperation replaceOperation) {
		return CVSTag.BASE;
	}
}