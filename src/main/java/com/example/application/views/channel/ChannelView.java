package com.example.application.views.channel;

import com.example.application.chat.ChatService;
import com.example.application.chat.Message;
import com.example.application.util.LimitedSortedAppendOnlyList;
import com.example.application.views.lobby.LobbyView;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.messages.MessageInput;
import com.vaadin.flow.component.messages.MessageList;
import com.vaadin.flow.component.messages.MessageListItem;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.security.AuthenticationContext;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.PermitAll;
import reactor.core.Disposable;

import java.util.Comparator;
import java.util.List;

@PermitAll
@Route(value = "channel")
public class ChannelView extends VerticalLayout
    implements HasUrlParameter<String>, HasDynamicTitle {

  private static final int HISTORY_SIZE = 20;

  private final ChatService chatService;

  private final MessageList messageList;

  private final LimitedSortedAppendOnlyList<Message> receivedMessages;

  private final String currentUserName;

  private String channelId;

  private String channelName;

  public ChannelView(
      final ChatService chatService, final AuthenticationContext authenticationContext) {
    this.chatService = chatService;
    this.currentUserName = authenticationContext.getPrincipalName().orElseThrow();

    receivedMessages =
        new LimitedSortedAppendOnlyList<>(
            HISTORY_SIZE, Comparator.comparing(Message::sequenceNumber));

    setSizeFull();

    messageList = new MessageList();
    messageList.setSizeFull();
    messageList.addClassNames(LumoUtility.Border.ALL);
    add(messageList);

    final MessageInput messageInput = new MessageInput(event -> sendMessage(event.getValue()));
    messageInput.setWidthFull();
    add(messageInput);
  }

  @Override
  protected void onAttach(AttachEvent attachEvent) {
    final Disposable subscription = subscribe();
    addDetachListener(event -> subscription.dispose());
  }

  @Override
  public void setParameter(BeforeEvent event, String channelId) {
    chatService
        .channel(channelId)
        .ifPresentOrElse(
            channel -> {
              this.channelName = channel.name();
              this.channelId = channelId;
            },
            () -> {
              event.forwardTo(LobbyView.class);
            });
  }

  private void sendMessage(final String message) {
    if (!message.isBlank()) {
      chatService.postMessage(channelId, message);
    }
  }

  private MessageListItem createMessageListItem(final Message message) {
    final MessageListItem messageListItem =
        new MessageListItem(message.message(), message.timestamp(), message.author());

    messageListItem.setUserColorIndex(Math.abs(message.author().hashCode() % 7));
    messageListItem.addClassNames(LumoUtility.Margin.SMALL, LumoUtility.BorderRadius.MEDIUM);
    if (message.author().equals(currentUserName)) {
      messageListItem.addClassNames(LumoUtility.Background.CONTRAST_5);
    }

    return messageListItem;
  }

  private void receivedMessages(List<Message> incomingMessages) {
    getUI()
        .ifPresent(
            ui ->
                ui.access(
                    () -> {
                      receivedMessages.addAll(incomingMessages);
                      messageList.setItems(
                          receivedMessages.stream().map(this::createMessageListItem).toList());
                    }));
  }

  private Disposable subscribe() {
    final Disposable subscription =
        chatService.liveMessages(channelId).subscribe(this::receivedMessages);

    final String lastSeenMessageId =
        receivedMessages.getLast().map(Message::messageId).orElse(null);
    receivedMessages(chatService.messageHistory(channelId, HISTORY_SIZE, lastSeenMessageId));

    return subscription;
  }

  @Override
  public String getPageTitle() {
    return channelName;
  }
}
