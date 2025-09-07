package bor.tools.simplellm;

import lombok.Data;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;

import bor.tools.simplellm.chat.Message;

/**
 * Encapsula diferentes tipos de conteúdo para permitir comunicação multimodal com modelos de linguagem.
 * Esta classe pode conter texto, imagens (como byte[] ou URL), URLs, chamadas de ferramentas,
 * arquivos, áudio, vídeo ou outros tipos de dados.
 *
 * <p><b>Tipos de conteúdo suportados:</b></p>
 * <ul>
 *   <li>{@link ContentType#TEXT}: Conteúdo textual.</li>
 *   <li>{@link ContentType#IMAGE}: Conteúdo de imagem (byte[] ou String URL).</li>
 *   <li>{@link ContentType#URL}: Um link para um recurso externo.</li>
 *   <li>{@link ContentType#TOOL_CALL}: Uma chamada de ferramenta, geralmente em formato JSON.</li>
 *   <li>{@link ContentType#FILE}: Um arquivo, representado pelo seu caminho (String) ou conteúdo (byte[]).</li>
 *   <li>{@link ContentType#AUDIO}: Conteúdo de áudio (byte[]).</li>
 *   <li>{@link ContentType#VIDEO}: Conteúdo de vídeo (byte[]).</li>
 *   <li>{@link ContentType#UNKNOWN}: Tipo de conteúdo desconhecido ou não especificado.</li>
 * </ul>
 *
 * @see ContentType
 * @see Message
 */
@Data
public class ContentWrapper {
	/**
     * O tipo do conteúdo encapsulado.
     * É ignorado durante a serialização JSON para evitar redundância.
     * @see ContentType
     */
	@JsonIgnore
    private ContentType type;
	
    /**
     * O objeto de conteúdo real. Pode ser uma String para texto, um byte[] para dados binários
     * (como imagens ou áudio), uma java.net.URL, etc.
     */
    private Object content;
    
    /**
     * Metadados opcionais associados ao conteúdo.
     * Pode ser usado para armazenar informações adicionais, como nome do arquivo,
     * tipo MIME, etc. É ignorado durante a serialização JSON.
     */
    @JsonIgnore
    private Map<String, Object> metadata;
         
    
    /**
     * Constrói um ContentWrapper com um tipo e conteúdo especificados.
     *
     * @param type O tipo do conteúdo (ex: TEXT, IMAGE).
     * @param content O objeto de conteúdo (ex: String, byte[]).
     */
    public ContentWrapper(ContentType type, Object content) {
		this.type = type;
		this.content = content;
	}
    
    /**
     * Constrói um ContentWrapper inferindo o tipo a partir do objeto de conteúdo.
     * - {@code String} é mapeado para {@link ContentType#TEXT}.
     * - {@code byte[]} é mapeado para {@link ContentType#IMAGE} (suposição padrão).
     * - {@code java.net.URL} é mapeado para {@link ContentType#URL}.
     * - Outros tipos são mapeados para {@link ContentType#UNKNOWN}.
     *
     * @param content O objeto de conteúdo do qual o tipo será inferido.
     */
    public ContentWrapper(Object content) {
		if (content instanceof String) {
			this.type = ContentType.TEXT;
		} else if (content instanceof byte[]) {
			this.type = ContentType.IMAGE; // Supondo que byte[] seja uma imagem
		} else if (content instanceof java.net.URL) {
			this.type = ContentType.URL;
		} else {
			this.type = ContentType.UNKNOWN;
		}
		this.content = content;
	}

	/**
     * Retorna o conteúdo como uma String se o tipo for {@link ContentType#TEXT}.
     *
     * @return O conteúdo textual como uma String, ou {@code null} se o tipo não for TEXT
     *         ou o conteúdo não for uma String.
     */
	@JsonIgnore
	public String getText() {
		if (type == ContentType.TEXT && content instanceof String) {
			return (String) content;
		}
		return null;
	}
	
	
    /**
     * Uma subclasse especializada de {@link ContentWrapper} para encapsular conteúdo de imagem.
     * A imagem pode ser fornecida como uma URL (String) ou como dados brutos (byte[]).
     */
	public static class ImageContent extends ContentWrapper {

		/**
         * Cria um wrapper de conteúdo de imagem a partir de uma URL.
         *
         * @param url A URL da imagem como uma String.
         */
		public ImageContent(String url) {
			super(ContentType.IMAGE, url);
		}

		/**
         * Cria um wrapper de conteúdo de imagem a partir de um array de bytes.
         *
         * @param imageData Os dados da imagem como um array de bytes.
         */
		public ImageContent(byte[] imageData) {
			super(ContentType.IMAGE, imageData);
		}

		/**
         * Retorna a URL da imagem, se o conteúdo for uma String.
         *
         * @return A URL da imagem, ou {@code null} se o conteúdo não for uma String.
         */
		public String
		       getUrl() { return this.getContent() instanceof String ? (String) this.getContent() : null; }

		/**
         * Retorna os dados da imagem, se o conteúdo for um array de bytes.
         *
         * @return O array de bytes da imagem, ou {@code null} se o conteúdo não for um array de bytes.
         */
		public byte[]
		       getImageData() { return this.getContent() instanceof byte[] ? (byte[]) this.getContent() : null; }

	}
    
}
