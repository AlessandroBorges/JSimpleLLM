package bor.tools.simplellm.chat;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.UUID;

import com.fasterxml.jackson.core.JsonProcessingException;

import bor.tools.simplellm.ContextManager;
import bor.tools.simplellm.MapParam;
import bor.tools.simplellm.ToolsConfig;
import bor.tools.simplellm.Utils;
import lombok.Data;

/**
 * Represents a chat session with a language model.
 * <p>
 * This class encapsulates all information related to a single conversation, including its unique identifier,
 * timestamps, the sequence of exchanged messages, and specific configurations for the model and tools.
 * </p>
 * <p>
 * The {@code @Data} annotation from Lombok automatically generates getters, setters, {@code toString()},
 * {@code equals()}, and {@code hashCode()} methods.
 * </p>
 * <p>
 * <b>Usage example:</b>
 * </p>
 * <pre>{@code
 * Chat chat = new Chat();
 * chat.setId(UUID.randomUUID().toString());
 * chat.setCreatedAt(LocalDateTime.now());
 * chat.setModel("gpt-4o");
 * chat.setMessages(new ArrayList<>());
 * }</pre>
 */
@Data
public class Chat {

	/**
	 * O identificador único para a sessão de chat.
	 * Geralmente, é um UUID gerado pela API na criação do chat.
	 */
	private String id;

	/**
	 * A data e hora em que a sessão de chat foi criada.
	 */
	private LocalDateTime createdAt;

	/**
	 * A data e hora do último acesso ou interação com a sessão de chat.
	 * Útil para gerenciamento do ciclo de vida da sessão (ex: timeouts).
	 */
	private LocalDateTime lastAccess;

	/**
	 * A lista de mensagens trocadas durante a sessão de chat.
	 * Esta lista mantém a ordem cronológica da conversa.
	 * 
	 * @see Message
	 */
	private LinkedList<Message> messages;

	/**
	 * Um mapa de parâmetros personalizados associados ao chat.
	 * Pode ser usado para armazenar metadados ou configurações específicas
	 * da sessão que não fazem parte da configuração padrão do modelo.
	 * 
	 * @see MapParam
	 */
	private MapParam mapParam;

	/**
	 * O modelo de linguagem específico a ser usado para esta sessão de chat
	 * (ex: "gpt-4o", "gemini-pro").
	 * Permite que diferentes chats utilizem diferentes modelos, sobrescrevendo
	 * a configuração padrão do sistema.
	 */
	private Object model;

	/**
	 * O gerenciador de contexto para esta sessão de chat.
	 */
	private ContextManager contextManage;

	/**
	 * A configuração das ferramentas (tools) que podem ser utilizadas pelo
	 * modelo de linguagem dentro deste chat.
	 * Define quais funções ou ferramentas externas o modelo tem permissão para
	 * invocar.
	 * 
	 * @see ToolsConfig
	 */
	private ToolsConfig toolsConfig;

	/**
	 * O mapeador JSON utilizado para serialização e desserialização.
	 * Inicializado com uma instância padrão do Jackson ObjectMapper.
	 */
	public Chat() {
		this.createdAt = LocalDateTime.now();
		this.lastAccess = this.createdAt;
		this.messages = new LinkedList<>();
	}

	public Chat(String id) {
		this();
		this.id = id;
	}

	public Chat(String id, ContextManager contextManage) {
		this(id);
		this.contextManage = contextManage;
	}
	
	
	/**
	 * Returns the language model used for this chat session.
	 * <p>
	 * If the {@code model} field is set, it is returned. Otherwise, if {@code mapParam}
	 * contains a model entry, that value is returned. Returns {@code null} if no model is set.
	 * </p>
	 * @return the model object or {@code null} if not set
	 */
	public Object getModel() {
		if (this.model != null) {
			return this.model;
		}
		if (this.mapParam != null && this.mapParam.containsKey(MapParam.MODEL)) {
			return this.mapParam.get(MapParam.MODEL);
		}
		return null;
	}

	/**
	 * Updates the timestamp of the last access to the current time.
	 * <p>
	 * Should be called whenever there is an interaction with the chat session.
	 * </p>
	 */
	public void updateLastAccess() {
		this.lastAccess = LocalDateTime.now();
	}

	/**
	 * Adds a new message to the chat message list and updates the last access timestamp.
	 *
	 * @param msg the message to add
	 * @return the added message
	 */
	public Message addMessage(Message msg) {
		this.messages.add(msg);
		updateLastAccess();
		return msg;
	}

	/**
	 * Searches for a message in the list by its ID.
	 *
	 * @param id the UUID of the message to find
	 * @return the message if found, or {@code null} if not found
	 */
	public Message findMessage(UUID id) {
		for (Message m : this.messages) {
			if (id.equals(m.getIdMessage())) {
				return m;
			}
		}
		return null;
	}

	/**
	 * Returns the message at the specified index and updates the last access timestamp.
	 *
	 * @param index the index of the message to return
	 * @return the message at the specified index
	 * @throws IndexOutOfBoundsException if the index is out of range
	 */
	public Message getMessage(int index) {
		updateLastAccess();
		return this.messages.get(index);
	}

	/**
	 * Removes all messages from the chat and updates the last access timestamp.
	 */
	public void clearMessages() {
		updateLastAccess();
		if (this.messages != null) {
			this.messages.clear();
		}
	}

	/**
	 * Returns the number of messages in the chat.
	 *
	 * @return the message count
	 */
	public int messageCount() {
		if (this.messages == null) {
			return 0;
		}
		return this.messages.size();
	}

	/**
	 * Returns the last message in the chat, or {@code null} if the list is empty.
	 * Updates the last access timestamp.
	 *
	 * @return the last message or {@code null}
	 */
	public Message getLastMessage() {
		updateLastAccess();
		if (this.messages == null || this.messages.isEmpty()) {
			return null;
		}
		return this.messages.getLast();
	}
	
	/**
	 * Get the text from the last Message, if available.
	 * @return the text of the last message or null if not available
	 */
	public String getLastMessageText() {
		Message m = getLastMessage();
		if(m!=null) {
			return m.getText();
		}
		return null;
	}
	
	/**
	 * Get the reasoning from last Message, if available.	
	 * @return the reasoning text or null if not available
	 */
	public String getLastMessageReasoning() {
		Message m = getLastMessage();
		if(m!=null) {
			return m.getReasoning();
		}
		return null;
	}
	
	/**
	 * Returns the last system/developer message in the chat, or {@code null} if not found.
	 *
	 * @return the last system message, or {@code null} if not found
	 */	
	public Message getLastSystemMessage() {
		return getLastMessage(MessageRole.SYSTEM);
	}
	
	/**
	 * Returns the last message in the chat with the specified role, or {@code null} if not found.
	 *
	 * @param role the role of the message to find
	 * @return the last message with the specified role, or {@code null} if not found
	 */
	public Message getLastMessage(MessageRole role) {
		if (this.messages == null || this.messages.isEmpty()) {
			return null;
		}		
		for (int i = this.messages.size() -1; i >=0; i--) {
			Message m = this.messages.get(i);
			if (m.getRole() == role) {
				return m;
			}
		}
		// fall back for SYSTEM role to return last DEVELOPER message
		if(role==MessageRole.SYSTEM) {
			return getLastMessage(MessageRole.DEVELOPER);			
		}
		return null;
	}

	/**
	 * Returns the first message in the chat, or {@code null} if the list is empty.
	 * Updates the last access timestamp.
	 *
	 * @return the first message or {@code null}
	 */
	public Message getFirstMessage() {
		updateLastAccess();
		if (this.messages == null || this.messages.isEmpty()) {
			return null;
		}
		return this.messages.getFirst();
	}

	/**
	 * Removes and returns the last message in the chat, or {@code null} if the list is empty.
	 * Updates the last access timestamp.
	 *
	 * @return the last removed message or {@code null}
	 */
	public Message popMessage() {
		updateLastAccess();
		return removeLastMessage();
	}

	/**
	 * Removes and returns the last message in the chat, or {@code null} if the list is empty.
	 * Updates the last access timestamp.
	 *
	 * @return the last removed message or {@code null}
	 */
	public Message removeLastMessage() {
		updateLastAccess();
		if (this.messages == null || this.messages.isEmpty()) {
			return null;
		}
		return this.messages.removeLast();
	}

	/**
	 * Returns the number of messages in the chat.
	 *
	 * @return the message count
	 */
	public int size() {
		return messageCount();
	}

	/**
	 * Adds a system message to the chat.
	 *
	 * @param system the system message content
	 * @return the added message
	 */
	public Message addSystemMessage(String system) {
		Message m = new Message(MessageRole.SYSTEM, system);
		return addMessage(m);
	}

	/**
	 * Adds an assistant message to the chat.
	 *
	 * @param assistant the assistant message content
	 * @return the added message
	 */
	public Message addAssistantMessage(String assistant) {
		Message m = new Message(MessageRole.ASSISTANT, assistant);
		return addMessage(m);
	}
	
	/**
	 * Adds an assistant message with reasoning to the chat.
	 *
	 * @param text the assistant message content
	 * @param reasoning the reasoning for the response
	 * @return the added message
	 */
	public Message addAssistantMessage(String text, String reasoning) {
		Message m = new Message(MessageRole.ASSISTANT, text);
		m.setReasoning(reasoning);
		return addMessage(m);
	}

	/**
	 * Adds an assistant message with search metadata to the chat.
	 * <p>
	 * This method is useful when adding responses from web search-enabled models
	 * (e.g., Perplexity AI) that include citations, search results, and related questions.
	 * </p>
	 *
	 * @param text the assistant message content
	 * @param searchMetadata the search metadata (citations, search results, etc.)
	 * @return the added message
	 */
	public Message addAssistantMessage(String text, SearchMetadata searchMetadata) {
		Message m = new Message(MessageRole.ASSISTANT, text);
		m.setSearchMetadata(searchMetadata);
		return addMessage(m);
	}

	/**
	 * Adds an assistant message with both reasoning and search metadata to the chat.
	 * <p>
	 * This method is useful when adding responses from reasoning-enabled web search models
	 * (e.g., Perplexity's sonar-reasoning models) that provide both reasoning traces and
	 * search results.
	 * </p>
	 *
	 * @param text the assistant message content
	 * @param reasoning the reasoning for the response
	 * @param searchMetadata the search metadata (citations, search results, etc.)
	 * @return the added message
	 */
	public Message addAssistantMessage(String text, String reasoning, SearchMetadata searchMetadata) {
		Message m = new Message(MessageRole.ASSISTANT, text);
		m.setReasoning(reasoning);
		m.setSearchMetadata(searchMetadata);
		return addMessage(m);
	}

	/**
	 * Adds a developer message to the chat.
	 *
	 * @param text the developer message content
	 * @return the added message
	 */
	public Message addDeveloperMessage(String text) {
		Message m = new Message(MessageRole.DEVELOPER, text);
		return addMessage(m);
	}

	/**
	 * Adds a user message to the chat.
	 *
	 * @param text the user message content
	 * @return the added message
	 */
	public Message addUserMessage(String text) {
		Message m = new Message(MessageRole.USER, text);
		return addMessage(m);
	}

	/**
	 * Adds a user message to the chat with usage parameters.
	 *
	 * @param text the user message content
	 * @param usage the usage parameters
	 * @return the added message
	 */
	public Message addUserMessage(String text, MapParam usage) {
		Message m = new Message(MessageRole.USER, text);
		m.setUsage(usage);
		return addMessage(m);
	}

	/**
	 * Returns a JSON string representation of the chat.
	 * If serialization fails, returns a simple string representation.
	 *
	 * @return the string representation of the chat
	 */
	@Override
	public String toString() {
		try {
			return Utils.createJsonMapper().writeValueAsString(this);
		} catch (JsonProcessingException e) {
			// Fallback to simple string representation if JSON processing fails
			return "Chat{id="
			            + id
			            + ", model="
			            + model
			            + ", messageCount="
			            + messageCount()
			            + "}";
		}
	}

	

}