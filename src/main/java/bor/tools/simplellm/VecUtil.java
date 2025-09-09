/**
 *
 */
package bor.tools.simplellm;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.List;

/**
 * Classe com funções de Vetores
 */
public class VecUtil {

	private static final DecimalFormat decimalFormat  = new DecimalFormat("0.0########E0");
	private static final DecimalFormat decimalFormatD = new DecimalFormat("0.0#####################E0");

	/**
	 * Evitar criação acidental
	 */
	private VecUtil() {}

	/**
	 * Calcula a soma do produto escalar de dois vetores
	 * 
	 * @param a - vetor a
	 * @param b - vetor b
	 * 
	 * @return Produto Escalar normalizado
	 */
	public static float[] soma(float[] a, float[] b) {
		int     len = Math.min(a.length, b.length);
		float[] res = new float[len];
		for (int i = 0; i < len; i++) {
			res[i] += a[i] + b[i];
		}
		res = normalize(res);
		return res;
	}

	/**
	 * Calcula a subtração do produto escalar de dois vetores.
	 * 
	 * <pre>
	 * c[i] = a[i] - b[i]
	 * </pre>
	 * 
	 * @param a - vetor a minuendo
	 * @param b - vetor b, subtraendo
	 * 
	 * @return Produto Escalar normalizado
	 */
	public static float[] subtracao(float[] a, float[] b) {
		int     len = Math.min(a.length, b.length);
		float[] res = new float[len];
		for (int i = 0; i < len; i++) {
			res[i] += a[i] - b[i];
		}
		res = normalize(res);
		return res;
	}

	/**
	 * Calcula o produto escalar de dois vetores.
	 * 
	 * @param a - vetor a
	 * @param b - vetor b
	 * 
	 * @return Produto Escalar
	 */
	public static final double dotProduct(float[] a, float[] b) {
		double sum = 0;
		int    len = Math.min(a.length, b.length);
		for (int i = 0; i < len; i++) {
			sum += a[i] * b[i];
		}
		return sum;
	}

	/**
	 * Calcula o produto escalar de dois vetores. <br>
	 *
	 * @param a - vetor a
	 * @param b - vetor b
	 * 
	 * @return Produto Escalar de <b>(a dot b)</b>, no comprimento do menor vetor
	 */
	public static double dotProduct(double[] a, double[] b) {
		double sum = 0;
		int    len = Math.min(a.length, b.length);
		for (int i = 0; i < len; i++) {
			sum += a[i] * b[i];
		}
		return sum;
	}

	/**
	 * Formata em modo compato
	 * 
	 * @param arr
	 * 
	 * @return
	 */
	public static String toString(float[] arr) {
		int           len = arr.length;
		StringBuilder bf  = new StringBuilder(len * (8) + 10);
		bf.append("[ ");
		for (int i = 0; i < len; i++) {
			float  f = arr[i];
			String s = decimalFormat.format(f);
			bf.append(s);
			if (i < (len - 2)) {
				bf.append(", ");
			}
		}
		bf.append("]");
		return bf.toString();
	}

	/**
	 * Normaliza o vetor "inplace", isto é, o mesmo vetor de input é o que retorna.
	 * 
	 * @param vector vetor de comprimento N
	 * 
	 * @return vetor normallizado
	 */
	public static float[] normalize(float[] vector) {
		float norm = (float) norm(vector);

		if (Math.abs(norm) <= 1.0e-6 || Math.abs(1.0f - norm) <= 1.0e-6) {
			return vector;
		}

		int     len              = vector.length;
		float[] normalizedVector = vector;// new float[len];
		for (int i = 0; i < len; i++) {
			normalizedVector[i] = vector[i] / norm;
		}
		return normalizedVector;
	}

	/**
	 * Converte um double[] em float[]
	 * 
	 * @param doubles
	 * 
	 * @return
	 */
	public static float[] castToFloats(double[] doubles) {
		int     len = doubles.length;
		float[] res = new float[len];
		for (int i = 0; i < doubles.length; i++) {
			res[i] = (float) doubles[i];
		}
		return res;
	}

	/**
	 * Calcula a norma de um vetor float[].
	 *
	 * @param a - vetor a ser normalizado
	 * 
	 * @return norma do vetor
	 *
	 * @see #normalize(double[])
	 */
	public static final double norm(float[] a) {
		return Math.sqrt(dotProduct(a, a));
	}

	/**
	 * Calcula a similaridade de cossenos entre dois vetores.<br>
	 * Se os vetores a e b forem normalizados, prefira
	 * {@link #dotProduct(float[], float[])}
	 *
	 * @param a - vetor a
	 * @param b - vetor b
	 * 
	 * @return similaridade entre a e b
	 *
	 * @see #dotProduct(float[], float[])
	 */
	public static final double cosineSimilarity(float[] a, float[] b) {
		return dotProduct(a, b) / (norm(a) * norm(b));
	}

	/**
	 * Record para similaridade.<br>
	 * Campos:<br>
	 * 
	 * @param valor1       - String
	 * @param valor2       - String
	 * @param similaridade - valor de similaridade
	 */
	public record Similaridade(String valor1, String valor2, double similaridade) {
		@Override
		public String toString() {
			return "Similaridade: \""
			            + valor1
			            + "\" vs \""
			            + valor2
			            + "\": \t"
			            + similaridade;
		}

	}

	/**
	 * Calcula similaridade de uma frase contra array de frases, usando float[]
	 *
	 * @param valor_referencia - frase referencial
	 * @param embedding_valor  - embedding de valor_referencial
	 * @param valores          - array de frases serem testados contra
	 *                         valor_referencial
	 * @param embeddings       - array com embeddings dos valores
	 * 
	 * @return lista ordenada de Similaridade, ordenada da maior para a menor
	 *         similiridade
	 *
	 * @see Similaridade
	 */
	public static Collection<Similaridade> calculaSimilaridade(String valor_referencia,
	                                                           float[] embedding_valor,
	                                                           String[] valores,
	                                                           List<float[]> embeddings) {
		List<Similaridade> lista = new ArrayList<>();
		for (int i = 0; i < valores.length; i++) {
			float[] b     = embeddings.get(i);
			double  simil = VecUtil.dotProduct(embedding_valor, b);// VecUtil.dotProduct(a, b);
			lista.add(new Similaridade(valor_referencia, valores[i], simil));
		}

		lista.sort((a, b) -> (a.similaridade > b.similaridade ? -1 : 1));
		return lista;
	}

	/**
	 * Calcular similaridade, usando float[].
	 * 
	 * @param valores    - valores a serem confrontados
	 * @param embeddings - embeddings dos valores
	 * @param sortIt     - ordena por similaridade decrescente
	 * 
	 * @return
	 */
	public static Collection<Similaridade> calculaSimilaridade(String[] valores,
	                                                           List<float[]> embeddings,
	                                                           boolean sortIt) {
		List<Similaridade> lista = new ArrayList<>();
		String[]           copy  = valores;
		for (int i = 0; i < valores.length; i++) {
			for (int j = 0; j < valores.length; j++) {
				if (i != j && i < j) {
					float[] a     = embeddings.get(i);
					float[] b     = embeddings.get(j);
					double  simil = VecUtil.dotProduct(a, b);// VecUtil.dotProduct(a, b);
					lista.add(new Similaridade(valores[i], copy[j], simil));
				}
			}
		}
		if (sortIt) {
			lista.sort((a, b) -> (a.similaridade > b.similaridade ? -1 : 1));
		}
		return lista;
	}

	/**
	 * Método utilitário.<br>
	 * Converte embeddings de base64 para float[].<br>
	 * <h3>Atenção! Tenha certeza que os dados de entrada foram criados com
	 * dados do tipo float ou float32 (4 bytes)</h3>
	 *
	 * @param base64String - String base64 de um array float[] previamente
	 *                     compactado.
	 * 
	 * @return array de valores float.
	 * 
	 * @throws IllegalArgumentException - Se o número de bytes não for um múltiplo
	 *                                  de 4
	 * 
	 * @see #base64ToDoubleArray(String)
	 */
	public static float[] base64ToFloatArray(String base64String) {
		// Decodifica a string Base64 para bytes
		byte[] bytes = Base64.getDecoder().decode(base64String);
		// Verifica se a quantidade de bytes é um múltiplo de 4 (tamanho de um float)
		if (bytes.length % 4 != 0) {
			throw new IllegalArgumentException("Número inválido de bytes para conversão em float");
		}
		// Cria um ByteBuffer a partir dos bytes
		ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
		byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
		// Converte o ByteBuffer em FloatBuffer para facilitar a leitura dos floats
		FloatBuffer floatBuffer = byteBuffer.asFloatBuffer();

		// Cria um array de floats para armazenar os dados convertidos
		float[] floats = new float[bytes.length / 4];
		// Lê os floats do FloatBuffer para o array
		floatBuffer.get(floats);

		return floats;
	}

}
