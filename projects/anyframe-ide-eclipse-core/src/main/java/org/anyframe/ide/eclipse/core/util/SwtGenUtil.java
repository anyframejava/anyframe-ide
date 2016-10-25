/*   
 * Copyright 2008-2011 the original author or authors.   
 *   
 * Licensed under the Apache License, Version 2.0 (the "License");   
 * you may not use this file except in compliance with the License.   
 * You may obtain a copy of the License at   
 *   
 *      http://www.apache.org/licenses/LICENSE-2.0   
 *   
 * Unless required by applicable law or agreed to in writing, software   
 * distributed under the License is distributed on an "AS IS" BASIS,   
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.   
 * See the License for the specific language governing permissions and   
 * limitations under the License.   
 */
package org.anyframe.ide.eclipse.core.util;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;

/**
 * This is an SwtGenUtil class.
 * @author Soungmin Joo
 */
public class SwtGenUtil {

    public static ScrolledForm createScrolledForm(Composite parent, String messageCode) {
        
        FormToolkit toolkit = new FormToolkit(parent.getDisplay());
        
        ScrolledForm form = toolkit.createScrolledForm(parent);
        form.setText(MessageUtil.getMessage(messageCode));
        form.getBody().setLayout(new FillLayout());
        
        return form;
    }
    
    public static Composite createSectionAndGetInnerContainer(final ScrolledForm form, Composite parent, String titleCode, String descCode, String layoutData, boolean expanded) {
        
        FormToolkit toolkit = new FormToolkit(parent.getDisplay());
        
        int style = Section.TWISTIE | Section.EXPANDED  | Section.DESCRIPTION | Section.TITLE_BAR;
        if(!expanded) {
            style = Section.TWISTIE | Section.DESCRIPTION | Section.TITLE_BAR;
        }
        
        Section section = toolkit.createSection(parent, style);
        
        section.setText(MessageUtil.getMessage(titleCode));
        section.setDescription(MessageUtil.getMessage(descCode));
        section.setLayout(new FillLayout());
        section.setLayoutData(layoutData);
        
        section.addExpansionListener(new ExpansionAdapter() {
            public void expansionStateChanged(ExpansionEvent e) {
                form.reflow(true);
            }
        });
        
        Composite container = toolkit.createComposite(section, SWT.NONE);
        section.setClient(container);
        
        return container;
    }
    
    public static Text createLabelAndTextAndWrapLine(Composite container, String label) {
        createLabel(container, label);
        Text text = new Text(container, SWT.LEFT | SWT.BORDER);
        text.setLayoutData("grow, wrap");
        
        return text;
    }
    
    public static Button createButton(Composite container, String buttonLabel, String imageFile, SelectionListener listener) {
        Button button = new Button(container, SWT.PUSH);
        
        button.setText(MessageUtil.getMessage(buttonLabel));
        if(imageFile != null && !imageFile.equals("")) {
            Image imageSearch = new Image(container.getDisplay(), SwtGenUtil.class.getResourceAsStream(MessageUtil.getMessage(imageFile)));
            button.setImage(imageSearch);
        }
        
        if(listener != null) {
            button.addSelectionListener(listener);
        }
        
        return button;
    }

    public static Combo createCombo(Composite container, int style, String layoutData) {
        Combo combo = new Combo(container, style);
        combo.setLayoutData(layoutData);
        return combo;
    }

    public static Label createLabel(Composite container, String urlLabelCode) {
        Label urlText = new Label(container, SWT.NONE);
        urlText.setText(MessageUtil.getMessage(urlLabelCode));
        return urlText;
    }
}
