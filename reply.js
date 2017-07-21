TextMessage textMessage = new TextMessage("hello");
ReplyMessage replyMessage = new ReplyMessage(
        "<replyToken>",
        textMessage
);
Response<BotApiResponse> response =
        LineMessagingServiceBuilder
                .create("<channel access token>")
                .build()
                .replyMessage(replyMessage)
                .execute();
System.out.println(response.code() + " " + response.message());
