package de.ollie.cube.gui.vaadin;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@Route(EntryView.URL)
@PageTitle("Cube")
@AnonymousAllowed
public class EntryView extends VerticalLayout {

	public static final String URL = "";

	@Override
	protected void onAttach(AttachEvent attachEvent) {
		UI.getCurrent().getPage().setLocation(MainMenuLayout.URL);
	}

}
