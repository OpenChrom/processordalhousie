/*******************************************************************************
 * Copyright (c) 2018 Lablicate GmbH.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Dr. Philip Wenig - initial API and implementation
 *******************************************************************************/
package net.openchrom.xxd.processor.supplier.dalhousie.ui.swt;

import org.eclipse.chemclipse.support.ui.provider.ListContentProvider;
import org.eclipse.chemclipse.support.ui.swt.ExtendedTableViewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.widgets.Composite;

import net.openchrom.xxd.processor.supplier.dalhousie.ui.internal.provider.FileComparator;
import net.openchrom.xxd.processor.supplier.dalhousie.ui.internal.provider.FileLabelProvider;
import net.openchrom.xxd.processor.supplier.dalhousie.ui.internal.provider.FileListFilter;

public class FileListUI extends ExtendedTableViewer {

	private FileLabelProvider labelProvider = new FileLabelProvider();
	private FileComparator tableComparator = new FileComparator();
	private FileListFilter listFilter = new FileListFilter();

	public FileListUI(Composite parent, int style) {
		super(parent, style);
		createColumns();
	}

	public void setSearchText(String searchText, boolean caseSensitive) {

		listFilter.setSearchText(searchText, caseSensitive);
		refresh();
	}

	public void clear() {

		setInput(null);
	}

	private void createColumns() {

		createColumns(FileLabelProvider.TITLES, FileLabelProvider.BOUNDS);
		setLabelProvider(labelProvider);
		setContentProvider(new ListContentProvider());
		setComparator(tableComparator);
		setFilters(new ViewerFilter[]{listFilter});
	}
}
