package com.example.application.views;

import com.example.application.views.lobby.LobbyView;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.spring.security.AuthenticationContext;
import com.vaadin.flow.theme.lumo.LumoUtility;

import java.awt.*;

public class MainLayout extends AppLayout {

  private final AuthenticationContext authenticationContext;

  private H2 viewTitle;

  public MainLayout(final AuthenticationContext authenticationContext) {
    this.authenticationContext = authenticationContext;

    setPrimarySection(Section.DRAWER);
    addNavbarContent();
    addDrawerContent();
  }

  @Override
  protected void afterNavigation() {
    super.afterNavigation();
    viewTitle.setText(getCurrentPageTitle());
  }

  private void addNavbarContent() {
    final DrawerToggle drawerToggle = new DrawerToggle();
    drawerToggle.setAriaLabel("Menu toggle");
    drawerToggle.setTooltipText("Menu toggle");

    viewTitle = new H2();
    viewTitle.addClassNames(
        LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE, LumoUtility.Flex.GROW);

    final Button logoutButton =
        new Button(
            "Logout " + authenticationContext.getPrincipalName().orElse(""),
            event -> authenticationContext.logout());

    final Header header = new Header(drawerToggle, viewTitle, logoutButton);
    header.addClassNames(
        LumoUtility.Display.FLEX,
        LumoUtility.AlignItems.CENTER,
        LumoUtility.Padding.End.MEDIUM,
        LumoUtility.Width.FULL);

    addToNavbar(false, header);
  }

  private void addDrawerContent() {
    final Span appName = new Span("Vaadin Chat");
    appName.addClassNames(
        LumoUtility.Display.FLEX,
        LumoUtility.AlignItems.CENTER,
        LumoUtility.Height.XLARGE,
        LumoUtility.Padding.Horizontal.MEDIUM,
        LumoUtility.FontSize.LARGE,
        LumoUtility.FontWeight.SEMIBOLD);
    addToDrawer(appName, new Scroller(createSideNav()));
  }

  private SideNav createSideNav() {
    final SideNav sideNav = new SideNav();
    sideNav.addItem(new SideNavItem("Lobby", LobbyView.class, VaadinIcon.BUILDING.create()));
    return sideNav;
  }

  private String getCurrentPageTitle() {
    if (getContent() == null) {
      return "";
    }

    if (getContent() instanceof HasDynamicTitle titleHolder) {
      return titleHolder.getPageTitle();
    }

    final PageTitle pageTitle = getContent().getClass().getAnnotation(PageTitle.class);

    return pageTitle == null ? "" : pageTitle.value();
  }
}
