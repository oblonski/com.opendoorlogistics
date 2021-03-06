/*******************************************************************************
 * Copyright (c) 2014 Open Door Logistics (www.opendoorlogistics.com)
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3
 * which accompanies this distribution, and is available at http://www.gnu.org/licenses/lgpl.txt
 ******************************************************************************/
package com.opendoorlogistics.studio.dialogs;

import java.awt.Dimension;
import java.io.InputStream;
import java.io.StringWriter;

import javax.swing.JFrame;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;

import com.opendoorlogistics.core.AppConstants;
import com.opendoorlogistics.core.utils.ui.TextInformationDialog;

final public class AboutBoxDialog extends TextInformationDialog {

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			AboutBoxDialog dialog = new AboutBoxDialog(null,true);
			dialog.setVisible(true);
		} catch (Throwable e) {
		//	e.printStackTrace();
		}
	}

	/**
	 * Create the dialog.
	 */
	public AboutBoxDialog(JFrame parent, boolean showLicenses) {
		super(parent, "About " + AppConstants.ORG_NAME, info(showLicenses), true, false,true);
		setPreferredSize(new Dimension(600, 300));
	//	scrollToTop();
		pack();
		//scrollToTop();
	}

//	public void scrollToTop() {
//		areaScrollPane.scrollRectToVisible(new Rectangle(0,0,areaScrollPane.getWidth(), areaScrollPane.getHeight()));
//		areaScrollPane.getViewport().setViewPosition(new Point(0, 0));
//		areaScrollPane.getVerticalScrollBar().setValue(areaScrollPane.getVerticalScrollBar().getMinimum());
//	}

	private static String info(boolean showLicenses) {

		InputStream is = Object.class.getResourceAsStream(
				showLicenses? "/resources/Licences.html":"/resources/About.html"  );
		StringWriter writer = new StringWriter();
		try {
			IOUtils.copy(is, writer, Charsets.UTF_8);			
			is.close();
		} catch (Throwable e) {
		}

		
		String s = writer.toString();
		
		s = s.replace("VERSION_NUMBER", AppConstants.getAppVersion().toString());
		
		return s;
	}

}
