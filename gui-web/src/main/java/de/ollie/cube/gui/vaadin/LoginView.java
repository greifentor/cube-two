package de.ollie.cube.gui.vaadin;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

import de.ollie.cube.core.service.AppConfiguration;
import lombok.RequiredArgsConstructor;

@Route("login")
@PageTitle("Cube Login")
@AnonymousAllowed
@RequiredArgsConstructor
public class LoginView extends VerticalLayout implements BeforeEnterObserver {

	private static final String ERROR_KEY = "error";

	private final AppConfiguration appConfiguration;

	private LoginForm login = new LoginForm();

	@Override
	protected void onAttach(AttachEvent attachEvent) {
		super.onAttach(attachEvent);
		addClassName("login-view");
		setAlignItems(Alignment.CENTER);
		setJustifyContentMode(JustifyContentMode.CENTER);
		setSizeFull();
		login.setAction("login");
		login.setForgotPasswordButtonVisible(false);
		add(new H1(appConfiguration.getName() + " (" + appConfiguration.getVersion() + ")"), login);
	}

	@Override
	public void beforeEnter(BeforeEnterEvent event) {
		if (event.getLocation().getQueryParameters().getParameters().containsKey(ERROR_KEY)) {
			login.setError(true);
		} else {
			login.setError(false);
		}
	}

}
