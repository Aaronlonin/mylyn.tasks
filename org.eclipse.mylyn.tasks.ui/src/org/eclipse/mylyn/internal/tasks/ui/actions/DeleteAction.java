/*******************************************************************************
* Copyright (c) 2004, 2008 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.internal.tasks.ui.actions;

import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.context.core.ContextCore;
import org.eclipse.mylyn.internal.tasks.core.AbstractTask;
import org.eclipse.mylyn.internal.tasks.core.AutomaticRepositoryTaskContainer;
import org.eclipse.mylyn.internal.tasks.core.RepositoryQuery;
import org.eclipse.mylyn.internal.tasks.core.TaskCategory;
import org.eclipse.mylyn.internal.tasks.core.UnmatchedTaskContainer;
import org.eclipse.mylyn.internal.tasks.core.UnsubmittedTaskContainer;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal;
import org.eclipse.mylyn.internal.tasks.ui.views.TaskListView;
import org.eclipse.mylyn.tasks.core.IRepositoryElement;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchImages;
import org.eclipse.ui.texteditor.IWorkbenchActionDefinitionIds;

/**
 * @author Mik Kersten
 */
public class DeleteAction extends Action {

	public static final String ID = "org.eclipse.mylyn.tasklist.actions.delete";

	public DeleteAction() {
		setText("Delete");
		setId(ID);
		setImageDescriptor(WorkbenchImages.getImageDescriptor(ISharedImages.IMG_TOOL_DELETE));
		setActionDefinitionId(IWorkbenchActionDefinitionIds.DELETE);
	}

	@Override
	public void run() {
		ISelection selection = TaskListView.getFromActivePerspective().getViewer().getSelection();
		doDelete(((IStructuredSelection) selection).toList());
	}

	protected void doDelete(List<?> toDelete) {
		String elements = "";
		int i = 0;
		for (Object object : toDelete) {
			if (object instanceof UnmatchedTaskContainer) {
				continue;
			}

			i++;
			if (i < 20) {
				// TODO this action should be based on the action enablement and check if the container is user managed or not
				if (object instanceof IRepositoryElement) {
					elements += "    " + ((IRepositoryElement) object).getSummary() + "\n";
				}
			} else {
				elements += "...";
				break;
			}
		}

		String message;

		if (toDelete.size() == 1) {
			Object object = toDelete.get(0);
			if (object instanceof ITask) {
				if (((AbstractTask) object).isLocal()) {
					message = "Permanently delete the task listed below?";
				} else {
					message = "Delete the planning information and context for the repository task?  The server"
							+ " copy will not be deleted and the task will remain in queries that match it.";
				}
			} else if (object instanceof TaskCategory) {
				message = "Permanently delete the category?  Local tasks will be moved to the Uncategorized folder. Repository tasks will be moved to the Unmatched folder.";
			} else if (object instanceof IRepositoryQuery) {
				message = "Permanently delete the query?  Contained tasks will be moved to the Unmatched folder.";
			} else if (object instanceof UnmatchedTaskContainer) {
				message = "Delete the planning information and context of all unmatched tasks?  The server"
						+ " copy of these tasks will not be deleted and the task will remain in queries that match it.";
			} else if (object instanceof UnsubmittedTaskContainer) {
				message = "Delete all of the unsubmitted tasks?";
			} else {
				message = "Permanently delete the element listed below?";
			}
		} else {
			message = "Delete the elements listed below?  If categories or queries are selected contained tasks"
					+ " will not be deleted.  Contexts will be deleted for selected tasks.";
		}

		message += "\n\n" + elements;

		boolean deleteConfirmed = MessageDialog.openQuestion(PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow()
				.getShell(), "Confirm Delete", message);
		if (!deleteConfirmed) {
			return;
		}

		performDeletion(toDelete);
	}

	protected void performDeletion(Collection<?> toDelete) {
		for (Object selectedObject : toDelete) {
			if (selectedObject instanceof ITask) {
				AbstractTask task = null;
				task = (AbstractTask) selectedObject;
				TasksUi.getTaskActivityManager().deactivateTask(task);
				TasksUiInternal.getTaskList().deleteTask(task);
				try {
					TasksUiPlugin.getTaskDataManager().deleteTaskData(task);
				} catch (CoreException e) {
					StatusHandler.log(new Status(IStatus.ERROR, TasksUiPlugin.ID_PLUGIN, "Failed to delete task data",
							e));
				}
				ContextCore.getContextManager().deleteContext(task.getHandleIdentifier());
				TasksUiInternal.closeEditorInActivePage(task, false);
			} else if (selectedObject instanceof IRepositoryQuery) {
				// boolean deleteConfirmed =
				// MessageDialog.openQuestion(PlatformUI.getWorkbench()
				// .getActiveWorkbenchWindow().getShell(), "Confirm delete",
				// "Delete the selected query? Task data will not be deleted.");
				// if (deleteConfirmed) {
				TasksUiInternal.getTaskList().deleteQuery((RepositoryQuery) selectedObject);
				// }
			} else if (selectedObject instanceof TaskCategory) {
				// boolean deleteConfirmed =
				// MessageDialog.openQuestion(PlatformUI.getWorkbench()
				// .getActiveWorkbenchWindow().getShell(), "Confirm Delete",
				// "Delete the selected category? Contained tasks will be moved
				// to the root.");
				// if (!deleteConfirmed)
				// return;
				TaskCategory cat = (TaskCategory) selectedObject;
				for (ITask task : cat.getChildren()) {
					ContextCore.getContextManager().deleteContext(task.getHandleIdentifier());
					TasksUiInternal.closeEditorInActivePage(task, false);
				}
				TasksUiInternal.getTaskList().deleteCategory(cat);
			} else if (selectedObject instanceof AutomaticRepositoryTaskContainer) {
				// support both the unmatched and the unsubmitted

				if (toDelete.size() == 1) {

					// loop to ensure that all subtasks are deleted as well
					while (((AutomaticRepositoryTaskContainer) selectedObject).getChildren().size() != 0) {
						performDeletion(((AutomaticRepositoryTaskContainer) selectedObject).getChildren());
					}
				}
			} else {
				MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
						"Delete failed", "Nothing selected.");
				return;
			}
		}
	}
}