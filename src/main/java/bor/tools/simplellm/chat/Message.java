package bor.tools.simplellm.chat;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

import bor.tools.simplellm.ContentType;
import bor.tools.simplellm.ContentWrapper;
import bor.tools.simplellm.LLMService;
import bor.tools.simplellm.MapParam;
import lombok.Data;

/**
 * Representa uma única mensagem dentro de uma sessão de chat.
 * <p>
 * Esta classe encapsula o conteúdo de uma mensagem, quem a enviou (o "papel" ou
 * "role"),
 * e metadados opcionais como parâmetros específicos e estatísticas de uso.
 * </p>
 * <p>
 * O conteúdo da mensagem é gerenciado pelo {@link ContentWrapper}, permitindo
 * suporte para dados multimodais (texto, imagens, etc.).
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
 * // Mensagem de texto simples do usuário
 * Message userMessage = new Message(MessageRole.USER, "Qual é a capital do Brasil?");
 *
 * // Mensagem de resposta do assistente
 * Message assistantMessage = new Message(MessageRole.ASSISTANT, "A capital do Brasil é Brasília.");
 * assistantMessage.setStatistics(new MapParam());
 * assistantMessage.getStatistics().put("token_count", 5);
 * }</pre>
 */
@Data
public class Message {

	/**
	 * unique UUID created for this message
	 */
	@JsonIgnore
	private UUID idMessage;

	/**
	 * O papel do autor da mensagem (ex: USER, ASSISTANT).
	 * Define a origem da mensagem dentro da conversa.
	 * 
	 * @see MessageRole
	 */
	private MessageRole role;

	/**
	 * O conteúdo da mensagem.
	 * Utiliza {@link ContentWrapper} para suportar diferentes tipos de conteúdo
	 * (texto, imagem, etc.), permitindo comunicação multimodal.
	 */
	private ContentWrapper content;

	/**
	 * Um mapa de parâmetros personalizados específicos para esta mensagem.
	 * Pode ser usado para passar informações adicionais que não fazem parte
	 * do conteúdo principal.
	 * 
	 * @see MapParam
	 */
	@JsonIgnore
	MapParam mapParam;

	/**
	 * Um mapa para armazenar estatísticas relacionadas a esta mensagem.
	 * Por exemplo, pode incluir contagem de tokens, tempo de processamento, etc.
	 * 
	 * @see MapParam
	 */
	MapParam usage;

	/**
	 * Construtor padrão.
	 * Cria uma instância de {@code Message} com valores nulos.
	 */
	public Message() {
		this(UUID.randomUUID());
	}

	/**
	 * Cria uma mensagem com um UUID
	 * 
	 * @param id
	 */
	private Message(UUID id) {
		this.idMessage = id;
	}

	/**
	 * Constrói uma mensagem com um papel e um conteúdo definidos.
	 *
	 * @param role    O papel do autor da mensagem (ex: USER, ASSISTANT).
	 * @param content O wrapper de conteúdo da mensagem.
	 */
	public Message(MessageRole role, ContentWrapper content) {
		this();
		this.role = role;
		this.content = content;
	}

	/**
	 * Constrói uma mensagem de texto simples com um papel definido.
	 * Este é um construtor de conveniência para mensagens puramente textuais.
	 * Se o texto for nulo, um valor padrão "continue..." será usado.
	 *
	 * @param role  O papel do autor da mensagem (ex: USER, ASSISTANT).
	 * @param texto A string de texto que compõe o conteúdo da mensagem.
	 */
	public Message(MessageRole role, String texto) {
		this();
		this.role = role;
		texto = texto != null ? texto.trim() : "continue...";
		this.content = new ContentWrapper(ContentType.TEXT, texto);
	}

	@Override
	public Message clone() {
		Message clone = new Message(this.idMessage);
		clone.content = this.content;
		clone.role = clone.role;
		clone.usage = this.usage == null ? null : (MapParam) this.usage.clone();
		clone.mapParam = this.mapParam;
		return clone;
	}

	/**
	 * Get current text
	 * 
	 * @return
	 */
	@JsonIgnore
	public String getText() { return content.getText(); }

	/**
	 * Set a new text
	 * 
	 * @param text
	 */
	public void setText(String text) { this.content = new ContentWrapper(ContentType.TEXT, text); }

	/**
	 * Conta o número de tokens usados nesta mensagem, se disponível nas
	 * estatísticas.
	 * 
	 * @return
	 */
	@JsonIgnore
	public int countTokens(LLMService service) {
		if (this.usage != null) {
			Object obj = this.usage.get("token_count");
			if (obj instanceof Integer) {
				return (Integer) obj;
			}
			if (obj instanceof Long) {
				return ((Long) obj).intValue();
			}
		}
		// calcula
		usage = usage == null ? new MapParam() : usage;

		if (service != null && this.content != null
		    && this.content.getType() == ContentType.TEXT
		    && this.content.getContent() instanceof String) {
			String text = (String) this.content.getContent();
			try {
				int count = service.tokenCount(text, null);
				this.usage.put("token_count", count);
				return count;
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			// try to estimate
			if (this.content != null && this.content.getType() == ContentType.TEXT
			    && this.content.getContent() instanceof String) {
				String text  = (String) this.content.getContent();
				int    count = text.length() / 4;
				this.usage.put("token_count", count);
				return count;
			}
		}
		return 0;
	}

}
