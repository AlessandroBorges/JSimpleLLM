# 🚀 Typed Streaming Examples

This document shows different ways to use the new typed streaming interface in JSimpleLLM.

## Option 1: Full Typed Interface (Recommended for New Code)

```java
ResponseStream stream = new ResponseStream() {
    @Override
    public void onToken(String token, ResponseStream.ContentType type) {
        switch (type) {
            case TEXT -> {
                System.out.print(token);  // Display text immediately
                textContent.append(token);
            }
            case REASONING -> {
                System.out.print("[THINK]" + token);  // Show reasoning
                reasoningContent.append(token);
            }
            case TOOL_CALL -> {
                System.out.println("[TOOL]" + token);  // Log tool calls
                handleToolCall(token);
            }
            case METADATA -> {
                System.out.println("[META]" + token);  // Log metadata
                updateStats(token);
            }
        }
    }

    @Override
    public void onComplete() {
        System.out.println("\n✅ Stream completed");
    }

    @Override
    public void onError(Throwable error) {
        System.err.println("❌ Stream error: " + error.getMessage());
    }
};
```

## Option 2: Backward Compatibility (Existing Code Works)

```java
ResponseStream stream = new ResponseStream() {
    @Override
    public void onToken(String token) {
        // This method automatically treats all content as TEXT
        // Existing code continues to work without changes
        content.append(token);
        System.out.print(token);
    }

    @Override
    public void onComplete() {
        System.out.println("\nDone!");
    }

    @Override
    public void onError(Throwable error) {
        System.err.println("Error: " + error.getMessage());
    }
};
```

## Option 3: Convenience Methods Override

```java
ResponseStream stream = new ResponseStream() {
    @Override
    public void onTextToken(String token) {
        // Handle only text content
        textArea.append(token);
    }

    @Override
    public void onReasoningToken(String reasoning) {
        // Handle only reasoning content
        reasoningArea.append(reasoning);
        reasoningArea.setVisible(true);
    }

    @Override
    public void onComplete() {
        System.out.println("Streaming finished");
    }

    @Override
    public void onError(Throwable error) {
        showErrorDialog(error.getMessage());
    }
};
```

## Option 4: Selective Handling

```java
ResponseStream stream = new ResponseStream() {
    @Override
    public void onToken(String token, ResponseStream.ContentType type) {
        // Only handle specific content types you care about
        if (type == ResponseStream.ContentType.TEXT) {
            updateUI(token);
        }
        // Ignore reasoning, tool calls, metadata
    }

    @Override
    public void onComplete() {
        finalizeUI();
    }

    @Override
    public void onError(Throwable error) {
        handleError(error);
    }
};
```

## Real-World Usage Example

```java
public class ChatUI {
    private StringBuilder chatContent = new StringBuilder();
    private StringBuilder reasoning = new StringBuilder();
    private JTextArea chatArea;
    private JTextArea reasoningArea;

    public void startChat(String query) {
        ResponseStream stream = new ResponseStream() {
            @Override
            public void onToken(String token, ContentType type) {
                SwingUtilities.invokeLater(() -> {
                    switch (type) {
                        case TEXT -> {
                            chatContent.append(token);
                            chatArea.append(token);
                            chatArea.setCaretPosition(chatArea.getDocument().getLength());
                        }
                        case REASONING -> {
                            reasoning.append(token);
                            reasoningArea.append(token);
                            reasoningArea.setVisible(true);  // Show reasoning panel
                        }
                    }
                });
            }

            @Override
            public void onComplete() {
                SwingUtilities.invokeLater(() -> {
                    enableInput();
                    if (reasoning.length() > 0) {
                        showReasoningButton.setEnabled(true);
                    }
                });
            }

            @Override
            public void onError(Throwable error) {
                SwingUtilities.invokeLater(() -> {
                    showErrorMessage(error.getMessage());
                    enableInput();
                });
            }
        };

        // Start streaming
        llmService.chatCompletionStream(stream, chat, query, params);
    }
}
```

## Benefits

1. **🔄 Backward Compatible**: Existing code works unchanged
2. **🎯 Type Safety**: Know what kind of content you're receiving
3. **🚀 Real-Time**: True streaming, not fake buffered responses
4. **🎨 UI Friendly**: Separate reasoning from main content
5. **⚡ Performance**: Process different content types efficiently
6. **🛠 Flexible**: Multiple ways to handle the same interface

## Migration Guide

### From Old Interface
```java
// OLD WAY
void onToken(String token) {
    content.append(token);
}
```

### To New Interface
```java
// NEW WAY - Method 1: Automatic (no changes needed)
void onToken(String token) {
    content.append(token);  // Still works!
}

// NEW WAY - Method 2: Full typed
void onToken(String token, ContentType type) {
    if (type == ContentType.TEXT) {
        content.append(token);
    }
}

// NEW WAY - Method 3: Override specific types
void onTextToken(String token) {
    content.append(token);
}
```

The choice is yours! 🎉