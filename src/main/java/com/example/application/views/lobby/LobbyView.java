package com.example.application.views.lobby;

import com.example.application.chat.Channel;
import com.example.application.chat.ChatService;
import com.example.application.security.Roles;
import com.example.application.views.MainLayout;
import com.example.application.views.channel.ChannelView;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.virtuallist.VirtualList;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.spring.security.AuthenticationContext;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.PermitAll;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;

@PermitAll
@Route(value = "", layout = MainLayout.class)
@PageTitle(value = "Lobby")
public class LobbyView extends VerticalLayout {
  private final ChatService chatService;
  private final VirtualList<Channel> channels;
  private final TextField channelNameField;
  private final Button addChannelButton;

  public LobbyView(
      final ChatService chatService, final AuthenticationContext authenticationContext) {
    this.chatService = chatService;

    channels = new VirtualList<>();
    channels.setRenderer(new ComponentRenderer<>(this::createChannelComponent));
    channels.addClassNames(LumoUtility.Border.ALL, LumoUtility.Padding.SMALL, "channel-list");
    add(channels);
    expand(channels);

    channelNameField = new TextField();
    channelNameField.setPlaceholder("New channel name");

    addChannelButton = new Button("Add channel", event -> addChannel());
    addChannelButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
    addChannelButton.addClickShortcut(Key.ENTER);
    addChannelButton.setDisableOnClick(true);

    if (authenticationContext.hasRole(Roles.ADMIN)) {
      final HorizontalLayout toolbar = new HorizontalLayout(channelNameField, addChannelButton);
      toolbar.setWidthFull();
      toolbar.expand(channelNameField);
      add(toolbar);
    }
  }

  @Override
  protected void onAttach(AttachEvent attachEvent) {
    refreshChannels();
  }

  private void refreshChannels() {
    channels.setItems(chatService.channels());
  }

  private void addChannel() {
    try {
      final String nameOfNewChannel = channelNameField.getValue();
      if (!nameOfNewChannel.isBlank()) {
        chatService.createChannel(nameOfNewChannel);
        channelNameField.clear();
        refreshChannels();
      }
    } finally {
      addChannelButton.setEnabled(true);
    }
  }

  private Component createChannelComponent(final Channel channel) {
    final Div channelComponent = new Div();
    channelComponent.addClassNames("channel");

    final Avatar avatar = new Avatar(channel.name());
    avatar.setColorIndex(Math.abs(channel.hashCode() % 7));
    channelComponent.add(avatar);

    final Div contentDiv = new Div();
    contentDiv.addClassNames("content");
    channelComponent.add(contentDiv);

    final Div channelName = new Div();
    channelName.addClassNames("name");
    contentDiv.add(channelName);

    final RouterLink channelLink = new RouterLink(channel.name(), ChannelView.class, channel.id());
    channelName.add(channelLink);

    if (channel.lastMessage() != null) {
      final Span lastMessageTimestamp =
          new Span(fromInstant(channel.lastMessage().timestamp(), getLocale()));
      lastMessageTimestamp.addClassNames("last-message-timestamp");
      channelName.add(lastMessageTimestamp);
    }

    final Span lastMessage = new Span();
    lastMessage.addClassNames("last-message");
    contentDiv.add(lastMessage);
    if (channel.lastMessage() != null) {
      var author = new Span(channel.lastMessage().author());
      author.addClassNames("author");
      lastMessage.add(author, new Text(": " + truncateMessage(channel.lastMessage().message())));
    } else {
      lastMessage.setText("No messages yet");
    }

    return channelComponent;
  }

  private String fromInstant(final Instant instant, final Locale locale) {
    return DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
        .withLocale(locale)
        .format(ZonedDateTime.ofInstant(instant, ZoneId.systemDefault()));
  }

  private String truncateMessage(String msg) {
    return msg.length() > 50 ? msg.substring(0, 50) + "..." : msg;
  }
}
