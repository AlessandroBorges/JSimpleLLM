package bor.tools.simplellm.chat;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.UUID;

import bor.tools.simplellm.ContextManager;
import bor.tools.simplellm.MapParam;
import bor.tools.simplellm.ToolsConfig;
import lombok.Data;

/**
 * Representa uma sessão de chat com o modelo de linguagem.
 * <p>
 * Esta classe encapsula todas as informações relacionadas a uma única
 * conversa, incluindo seu identificador único, timestamps, a sequência
 * de mensagens e configurações específicas para o modelo e ferramentas.
 * </p>
 * <p>
 * A anotação {@code @Data} do Lombok gera automaticamente os métodos
 * getters, setters, {@code toString()}, {@code equals()} e {@code hashCode()}.
 * </p>
 * <p>
 * <b>Exemplo de uso:</b>
 * </p>
 * 
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
	private String model;

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

	public void updateLastAccess() {
		this.lastAccess = LocalDateTime.now();
	}

	public void addMessage(Message msg) {
		this.messages.add(msg);
		updateLastAccess();
	}

	public Message findMessage(UUID id) {
		for (Message m : this.messages) {
			if (id.equals(m.getIdMessage())) {
				return m;
			}
		}
		return null;
	}

	public Message getMessage(int index) {
		updateLastAccess();
		return this.messages.get(index);
	}

	public void clearMessages() {
		updateLastAccess();
		if (this.messages != null) {
			this.messages.clear();
		}
	}

	public int messageCount() {
		if (this.messages == null) {
			return 0;
		}
		return this.messages.size();
	}

	public Message getLastMessage() {
		updateLastAccess();
		if (this.messages == null || this.messages.isEmpty()) {
			return null;
		}
		return this.messages.getLast();
	}

	public Message getFirstMessage() {
		updateLastAccess();
		if (this.messages == null || this.messages.isEmpty()) {
			return null;
		}
		return this.messages.getFirst();
	}

	/**
	 * @return A última mensagem removida da lista, ou null se a lista estiver
	 *         vazia.
	 */
	public Message popMessage() {
		updateLastAccess();
		return removeLastMessage();
	}

	public Message removeLastMessage() {
		updateLastAccess();
		if (this.messages == null || this.messages.isEmpty()) {
			return null;
		}
		return this.messages.removeLast();
	}

	public int size() {
		return messageCount();
	}

	public void addSystemMessage(String system) {
		Message m = new Message(MessageRole.SYSTEM, system);
		addMessage(m);
	}

	public void addAssistantMessage(String assistant) {
		Message m = new Message(MessageRole.ASSISTANT, assistant);
		addMessage(m);
	}

	public void addDeveloperMessage(String developer) {
		Message m = new Message(MessageRole.DEVELOPER, developer);
		addMessage(m);
	}

	public void addUserMessage(String user) {
		Message m = new Message(MessageRole.USER, user);
		addMessage(m);
	}

}
