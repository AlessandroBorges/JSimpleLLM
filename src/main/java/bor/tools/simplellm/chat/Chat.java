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
	 * Atualiza o timestamp do último acesso para o momento atual.
	 * Deve ser chamado sempre que houver uma interação com a sessão de chat.
	 */
	public void updateLastAccess() {
		this.lastAccess = LocalDateTime.now();
	}

	/**
	 * Adiciona uma nova mensagem à lista de mensagens do chat.
	 * Atualiza o timestamp do último acesso.
	 * 
	 * @param msg a mensagem a ser adicionada
	 */

	public Message addMessage(Message msg) {
		this.messages.add(msg);
		updateLastAccess();
		return msg;
	}

	/**
	 * Procura uma mensagem na lista pelo seu ID.
	 * 
	 * @param id o UUID da mensagem a ser encontrada
	 * 
	 * @return a mensagem se encontrada, ou null se não existir
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
	 * Retorna a mensagem na posição especificada da lista.
	 * Atualiza o timestamp do último acesso.
	 * 
	 * @param index o índice da mensagem a ser retornada
	 * 
	 * @return a mensagem na posição especificada
	 * 
	 * @throws IndexOutOfBoundsException se o índice estiver fora do intervalo
	 *                                   (index < 0 || index >= size())
	 */
	public Message getMessage(int index) {
		updateLastAccess();
		return this.messages.get(index);
	}

	/**
	 * Remove todas as mensagens da lista.
	 * Atualiza o timestamp do último acesso.
	 */
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

	public Message addSystemMessage(String system) {
		Message m = new Message(MessageRole.SYSTEM, system);
		return addMessage(m);
	}

	public Message addAssistantMessage(String assistant) {
		Message m = new Message(MessageRole.ASSISTANT, assistant);
		return addMessage(m);
	}

	public Message addDeveloperMessage(String developer) {
		Message m = new Message(MessageRole.DEVELOPER, developer);
		return addMessage(m);
	}

	public Message addUserMessage(String user) {
		Message m = new Message(MessageRole.USER, user);
		return addMessage(m);
	}

	public Message addUserMessage(String user, MapParam usage) {
		Message m = new Message(MessageRole.USER, user);
		m.setUsage(usage);
		return addMessage(m);
	}

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
