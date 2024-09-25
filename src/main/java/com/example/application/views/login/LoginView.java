package com.example.application.views.login;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@Route(value = "login")
@PageTitle(value = "Chat Login")
@AnonymousAllowed
public class LoginView extends VerticalLayout implements BeforeEnterObserver {

  private final LoginForm loginForm;

  public LoginView() {
    loginForm = new LoginForm();

    setSizeFull();

    setAlignItems(Alignment.CENTER);
    setJustifyContentMode(JustifyContentMode.CENTER);

    loginForm.setAction("login");

    add(
        new H2("Vaadin Chat"),
        new Div(
            "You can log in as 'alice', 'bob' or 'admin'. The password for all of them is 'password'."),
        loginForm);
  }

  @Override
  public void beforeEnter(BeforeEnterEvent event) {
    if (event.getLocation().getQueryParameters().getParameters().containsKey("error")) {
      loginForm.setError(true);
    }
  }
}
