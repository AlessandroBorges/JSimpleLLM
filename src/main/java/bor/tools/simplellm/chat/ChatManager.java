package bor.tools.simplellm.chat;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;

import bor.tools.simplellm.LLMProvider;
import lombok.Data;

/**
 * Manages multiple chat sessions, allowing for their creation, retrieval, and
 * deletion.
 * Inactive chats are subject to automatic cleanup.
 * This class is thread-safe, using a {@link ConcurrentHashMap} for managing
 * chats.
 */
@Data
public class ChatManager {

	/**
	 * Default timeout for inactive chats (24 hours).
	 */
	private static final long CHAT_TIMEOUT_MS = 24 * 60 * 60 * 1000; // 24 hours

	/**
	 * A thread-safe map to store active chat sessions, with chat IDs as keys.
	 */
	private final Map<String, Chat>  chats = new ConcurrentHashMap<>();
	/**
	 * The executor service for scheduling periodic cleanup of inactive chats.
	 */
	private ScheduledExecutorService scheduler; // For automatic cleanup

	/**
	 * The persistence mechanism for saving and loading chats.
	 */
	private ChatPersistence chatPersistence; // For optional persistence

	/**
	 * A flag to enable or disable chat persistence.
	 */
	private boolean persistenceEnabled = false; // Flag to enable/disable persistence

	/**
	 * The maximum number of chat sessions to retain in memory.
	 */
	private int maxChats = 100; // Maximum number of chats to retain

	/**
	 * The interval, in minutes, for the periodic cleanup of inactive chats.
	 */
	private int        cleanupIntervalMinutes = 10; // Interval for periodic cleanup
	/**
	 * A reference to the LLM service, providing context for chat interactions.
	 */
	private LLMProvider llmService; // Reference to LLM service for context
	/**
	 * The timeout, in milliseconds, for marking a chat as inactive.
	 */
	private long       timeoutMs              = CHAT_TIMEOUT_MS; // Default timeout for inactive chats

	/**
	 * Default constructor for ChatManager.
	 * Initializes without scheduler or persistence.
	 */
	public ChatManager() {

	}

	/**
	 * Constructs a new ChatManager.
	 * 
	 * @param scheduler       The executor service for scheduling periodic cleanup
	 *                        of inactive chats.
	 * @param chatPersistence The persistence mechanism for saving and loading
	 *                        chats.
	 */
	public ChatManager(ScheduledExecutorService scheduler,
	                   ChatPersistence chatPersistence) 
	{
		this.chatPersistence = chatPersistence;
		this.scheduler = scheduler;

	}

	/**
	 * Creates a new chat session with a unique ID.
	 * The new chat is stored in the manager.
	 * 
	 * @return The newly created {@link Chat} instance.
	 */
	public Chat createChat() {
		var  uuid = UUID.randomUUID().toString();
		Chat chat = new Chat(uuid);
		chats.put(uuid, chat);
		return chat;
	}

	/**
	 * Retrieves a chat session by its unique ID.
	 * 
	 * @param chatId The unique identifier of the chat to retrieve.
	 * 
	 * @return The {@link Chat} instance corresponding to the given ID, or
	 *         {@code null} if not found.
	 */
	public Chat getChat(String chatId) {
		return chats.get(chatId);
	}

	/**
	 * Deletes a chat session from the manager.
	 * 
	 * @param chatId The unique identifier of the chat to delete.
	 */
	public void deleteChat(String chatId) {
		chats.remove(chatId);
	}

	/**
	 * Returns a view of all current chat sessions.
	 * 
	 * @return A {@link Map} containing all chat IDs and their corresponding
	 *         {@link Chat} instances.
	 */
	public Map<String, Chat> listChats() {
		return chats;
	}
}
