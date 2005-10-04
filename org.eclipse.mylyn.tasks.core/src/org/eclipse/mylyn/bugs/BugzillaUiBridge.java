/*******************************************************************************
 * Copyright (c) 2004 - 2005 University Of British Columbia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     University Of British Columbia - initial API and implementation
 *******************************************************************************/
/*
 * Created on Apr 6, 2005
  */
package org.eclipse.mylar.bugs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.mylar.bugzilla.ui.BugzillaOpenStructure;
import org.eclipse.mylar.bugzilla.ui.ViewBugzillaAction;
import org.eclipse.mylar.bugzilla.ui.editor.AbstractBugEditor;
import org.eclipse.mylar.bugzilla.ui.outline.BugzillaOutlinePage;
import org.eclipse.mylar.bugzilla.ui.tasklist.BugzillaTaskEditor;
import org.eclipse.mylar.core.IMylarContextNode;
import org.eclipse.mylar.ui.IMylarUiBridge;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.Workbench;

public class BugzillaUiBridge implements IMylarUiBridge {

    protected BugzillaContextLabelProvider labelProvider = new BugzillaContextLabelProvider();
    
    public void open(IMylarContextNode node) {
        String handle = node.getElementHandle();
        String bugHandle = handle;
        String server =handle.substring(0, handle.indexOf(";"));
               
        handle = handle.substring(handle.indexOf(";") + 1);
        int next = handle.indexOf(";");
        
        int bugId;
        int commentNumer = -1;
        if(next == -1){
            bugId = Integer.parseInt(handle);
        }
        else{
            bugId = Integer.parseInt(handle.substring(0, handle.indexOf(";")));
            commentNumer = Integer.parseInt(handle.substring(handle.indexOf(";") + 1));
            bugHandle = bugHandle.substring(0, next);
        }
                
        List<BugzillaOpenStructure> l = new ArrayList<BugzillaOpenStructure>(1);
        l.add(new BugzillaOpenStructure(server, bugId, commentNumer));
        
//        ITask task= MylarTasklistPlugin.getTaskListManager().getTaskForHandle(bugHandle);
//        if (task != null && task instanceof BugzillaTask) {
//            BugzillaTask bugzillaTask = (BugzillaTask)task;
//            bugzillaTask.openTask(commentNumer);
//        } else {
            // open the bug in the editor
            ViewBugzillaAction viewBugs = new ViewBugzillaAction("Display bugs in editor", l);
            viewBugs.schedule();
//        }
    }
    
    public ILabelProvider getLabelProvider() {
        return labelProvider;
    }

    public void close(IMylarContextNode node) {
        IWorkbenchPage page = Workbench.getInstance().getActiveWorkbenchWindow().getActivePage();
        if (page != null) {
            IEditorReference[] references = page.getEditorReferences();
            for (int i = 0; i < references.length; i++) {
                IEditorPart part = references[i].getEditor(false);
                if (part != null) {
                    if (part instanceof AbstractBugEditor) {
                        ((AbstractBugEditor)part).close();
                    } else if(part instanceof BugzillaTaskEditor){
                        ((BugzillaTaskEditor)part).close();
                    }
                }
            }
        }
    }

    public boolean acceptsEditor(IEditorPart editorPart) {
        return editorPart instanceof AbstractBugEditor;
    }

    public List<TreeViewer> getTreeViewers(IEditorPart editor) {
        ArrayList<TreeViewer> outlines = new ArrayList<TreeViewer>(1);
        TreeViewer outline = getOutlineTreeViewer(editor);
        if (outline != null) {
            outlines.add(outline);
            return outlines;
        } else {
            return Collections.emptyList();
        }
    }
    
    protected TreeViewer getOutlineTreeViewer(IEditorPart editor) {
        if(editor instanceof AbstractBugEditor){
            AbstractBugEditor abe = (AbstractBugEditor)editor;
            BugzillaOutlinePage outline = abe.getOutline();
            if(outline != null) return outline.getOutlineTreeViewer();
        }
        return null;        
    }

    public void refreshOutline(Object element, boolean updateLabels, boolean setSelection) {
    	IEditorPart editorPart = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
        TreeViewer treeViewer = getOutlineTreeViewer(editorPart);
        if (treeViewer != null) {
        	treeViewer.refresh(true);

            treeViewer.expandAll();
        }
    }
}
