package de.ollie.cube.gui.vaadin;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.core.context.SecurityContextHolder;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.Route;

import de.ollie.cube.core.model.Application;
import de.ollie.cube.core.model.User;
import de.ollie.cube.core.model.UserAuthorizationSO;
import de.ollie.cube.core.model.localization.LocalizationSO;
import de.ollie.cube.core.service.ApplicationService;
import de.ollie.cube.core.service.JWTService;
import de.ollie.cube.core.service.UserService;
import de.ollie.cube.core.service.impl.PasswordEncoder;
import de.ollie.cube.core.service.localization.ResourceManager;
import de.ollie.cube.gui.CubeConfiguration;
import de.ollie.cube.gui.security.SecurityService;
import de.ollie.cube.gui.vaadin.OwnUserDataDialog.OwnUserDataDialogObserver;
import de.ollie.cube.gui.vaadin.component.Button;
import de.ollie.cube.gui.vaadin.component.ButtonFactory;
import de.ollie.cube.gui.vaadin.component.ButtonGrid;
import de.ollie.cube.gui.vaadin.component.HeaderLayout;
import de.ollie.cube.gui.vaadin.component.HeaderLayout.HeaderLayoutMode;
import jakarta.annotation.security.PermitAll;
import lombok.RequiredArgsConstructor;

@Route(MainMenuLayout.URL)
@RequiredArgsConstructor
@PermitAll
public class MainMenuLayout extends VerticalLayout
		implements BeforeEnterObserver, HasUrlParameter<String>, OwnUserDataDialogObserver {

	public static final String URL = "cube/menu";

	private static final Logger LOGGER = LogManager.getLogger(MainMenuLayout.class);

	private final ApplicationService applicationService;
	private final CubeConfiguration cubeConfiguration;
	private final JWTService jwtService;
	private final PasswordEncoder passwordEncoder;
	private final ResourceManager resourceManager;
	private final SecurityService securityService;
	private final SessionData sessionData;
	private final UserService userService;

	@Override
	public void setParameter(BeforeEvent event, @OptionalParameter String parameter) {
		LOGGER.debug("setParameter");
	}

	@Override
	public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
		User user = getAuthenticatedUser();
		LOGGER.info("created");
		List<Button> buttons =
				applicationService.findAllApplicationsByUserId(user.getId()).stream().map(application -> {
					Button button = ButtonFactory.createButton(application.getName());
					button.addClickListener(event -> switchToApplication(application));
					button.setWidthFull();
					return button;
				}).collect(Collectors.toList());
		Button buttonUserOwnData =
				ButtonFactory
						.createButton(
								resourceManager.getLocalizedString("main-menu.button.ownData.text", LocalizationSO.DE));
		buttonUserOwnData.addClickListener(event -> updateUserOwnData());
		buttonUserOwnData.setWidth("20%");
		VerticalLayout buttonLayout = new VerticalLayout();
		buttonLayout.setMargin(false);
		buttonLayout.setWidthFull();
		ButtonGrid buttonGrid = new ButtonGrid(buttons.toArray(new Button[buttons.size()]));
		buttonGrid.setMargin(false);
		buttonGrid.setWidthFull();
		// buttonLayout.add(buttonGrid);
		setWidthFull();
		setMargin(false);
		getStyle().set("background-image", "url(Cube-Background.png)");
		getStyle().set("background-repeat", "no-repeat");
		getStyle().set("background-position", "center center");
		add(new HeaderLayout(ButtonFactory.createLogoutButton(resourceManager, () -> {
			LOGGER.info("user '{}' logged out.", sessionData.getUserAuthorization().getName());
			securityService.logout();
			sessionData.setUserAuthorization(null);
			UI.getCurrent().navigate(EntryView.URL);
		}, sessionData, LOGGER),
				buttonUserOwnData,
				resourceManager.getLocalizedString("commons.header.main-menu.label", LocalizationSO.DE),
				HeaderLayoutMode.PLAIN), buttonGrid);
		LOGGER.info("main menu view opened for user '{}'.", sessionData.getUserName());
	}

	private User getAuthenticatedUser() {
		String userName = SecurityContextHolder.getContext().getAuthentication().getName();
		User user =
				userService
						.findAll()
						.stream()
						.filter(u -> u.getName().equals(userName))
						.findFirst()
						.orElseThrow(() -> new NoSuchElementException("User with name does not exist; " + userName));
		if (sessionData.getUserAuthorization() == null) {
			sessionData
					.setUserAuthorization(
							new UserAuthorizationSO(
									user.getGlobalId(),
									user.getName(),
									user.getPassword(),
									user.getToken(),
									user.getId()));
		}
		return user;
	}

	private void switchToApplication(Application application) {
		String url =
				application.getBaseUrl() + "?jwt="
						+ this.jwtService
								.createJWT(
										this.sessionData.getUserAuthorization().getName(),
										this.sessionData.getUserAuthorization().getToken(),
										this.sessionData.getUserAuthorization().getGlobalId(),
										application.getGlobalId(),
										LocalDateTime.now().plusMinutes(1L),
										Arrays.asList("right1", "right2"));
		LOGGER.info("calling: " + url);
		this.getUI().ifPresent((ui) -> {
			ui.getPage().open(url, application.getName());
		});
	}

	private void updateUserOwnData() {
		userService
				.findById(getAuthenticatedUser().getId())
				.ifPresent(
						user -> new OwnUserDataDialog(
								user,
								this,
								resourceManager,
								sessionData,
								passwordEncoder,
								userService).open());
	}

	@Override
	public void userChanged(User user) {
		LOGGER.info("user password changed for user: " + user.getName() + " (" + user.getId() + ")");
	}

}
