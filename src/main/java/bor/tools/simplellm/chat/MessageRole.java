package bor.tools.simplellm.chat;

/**
 * Define o papel (role) do autor de uma mensagem em uma sessão de chat.
 * <p>
 * Esta enumeração é usada para distinguir entre as mensagens enviadas pelo
 * usuário final, as instruções de sistema que configuram o comportamento do
 * assistente e as respostas geradas pelo próprio assistente.
 * </p>
 */
public enum MessageRole {
		/**
		 * Representa o usuário final que está interagindo com o modelo.
		 * Mensagens com este papel são as perguntas ou comandos do usuário.
		 */
		USER,

		/**
		 * Representa uma instrução de sistema ou um contexto inicial.
		 * Mensagens com este papel são usadas para definir o comportamento,
		 * a personalidade ou as diretrizes que o assistente deve seguir
		 * durante a conversa. Geralmente, é a primeira mensagem em uma
		 * lista de mensagens.
		 */
		SYSTEM,

		/**
		 * Representa o assistente de IA (o modelo de linguagem).
		 * Mensagens com este papel são as respostas geradas pelo modelo.
		 */
		ASSISTANT,

		/**
		 * Just like SYSTEM role, the DEVELOPER role are instructions provided by the
		 * application developer,
		 * prioritized ahead of user messages.
		 */
		DEVELOPER;

	public String getName() { return this.name().toLowerCase(); }

	@Override
	public String toString() {
		return this.name().toLowerCase();
	}

	/**
	 * Converte uma string em um valor correspondente de MessageRole.
	 * A comparação é feita de forma case-insensitive.
	 *
	 * @param role A string representando o papel (role) da mensagem.
	 * 
	 * @return O valor correspondente de MessageRole, ou null se a entrada for null.
	 * 
	 * @throws IllegalArgumentException se a string não corresponder a nenhum papel
	 *                                  conhecido.
	 */
	public static MessageRole fromString(String role) {
		if (role == null) {
			return null;
		}
		switch (role.toLowerCase()) {
			case "user":
				return USER;
			case "system":
				return SYSTEM;
			case "assistant":
				return ASSISTANT;
			case "developer":
				return DEVELOPER;
			default:
				throw new IllegalArgumentException("Unknown role: "
				            + role);
		}
	}
}
